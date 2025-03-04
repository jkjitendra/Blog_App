package com.jk.blog.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.constants.AppConstants;
import com.jk.blog.constants.SecurityConstants;
import com.jk.blog.controller.api.PostApi;
import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.post.PostRequestBody;
import com.jk.blog.dto.post.PostResponseBody;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.FileService;
import com.jk.blog.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_NAME)
@Tag(name = "Post Management", description = "APIs for managing blog posts")
public class PostController implements PostApi {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    @Qualifier("localFileService")
//    @Qualifier("s3FileService")
    private FileService fileService;

    @Value("${aws.s3.bucket.post}")
    private String postBucketPath;

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<PostResponseBody>> createPost(
                                                       @Valid @RequestPart("post") String postRequestBody,
                                                       @RequestPart(value = "image", required = false) MultipartFile image,
                                                       @RequestPart(value = "video", required = false) MultipartFile video) throws IOException {

        PostRequestBody postJSON = new ObjectMapper().readValue(postRequestBody, PostRequestBody.class);

        PostResponseBody createdPostResponseBody = this.postService.createPost(postJSON, image, video);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse<>(true, "Post created successfully"));
    }

    @GetMapping("/")
    public ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getAllPost(
                            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
                            @RequestParam(value = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
                            ) {
        PageableResponse<PostResponseBody> pageableResponse = this.postService.getAllPost(pageNumber, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(new APIResponse<>(true, "Posts fetched successfully", pageableResponse));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<APIResponse<PostResponseBody>> getPostById(@PathVariable Long postId) {
        PostResponseBody existingPostRequestBody = this.postService.getPostById(postId);
        return ResponseEntity.ok(new APIResponse<>(true, "Post fetched successfully", existingPostRequestBody));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<Void>> updatePost(
                                                       @PathVariable Long postId,
                                                       @Valid @RequestPart("post") String postRequestBody,
                                                       @RequestPart(value = "image", required = false) MultipartFile image,
                                                       @RequestPart(value = "video", required = false) MultipartFile video) throws IOException {

        PostRequestBody postJSON = new ObjectMapper().readValue(postRequestBody, PostRequestBody.class);

        PostResponseBody updatedPost = this.postService.updatePost(postId, postJSON, image, video);
        return ResponseEntity.ok(new APIResponse<>(true, "Post updated successfully"));
    }

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<Void>> patchPost(
                                                       @PathVariable Long postId,
                                                       @RequestPart("post") String updatesJson,
                                                       @RequestPart(value = "image", required = false) MultipartFile image,
                                                       @RequestPart(value = "video", required = false) MultipartFile video) throws IOException {

        PostRequestBody postJSON = new ObjectMapper().readValue(updatesJson, new TypeReference<>() {});

        // Need to check whether we are going to store all images and videos
        // or delete existing one and store the new one
        // or do something else

        PostResponseBody updatedPost = this.postService.patchPost(postId, postJSON, image, video);
        return ResponseEntity.ok(new APIResponse<>(true, "Post updated successfully"));
    }

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PatchMapping("/post/{postId}/visibility")
    public ResponseEntity<APIResponse<Void>> togglePostVisibility(@PathVariable Long postId, @RequestBody Map<String, Boolean> visibility) {
        boolean isLive = visibility.getOrDefault("isLive", false);
        PostResponseBody postResponseBody = this.postService.togglePostVisibility(postId, isLive);
        return ResponseEntity.ok(new APIResponse<>(true, "isLive updated Successfully"));
    }

    @PreAuthorize("hasAuthority('SUBSCRIBER')")
    @PatchMapping("/post/{postId}/member-post")
    public ResponseEntity<APIResponse<PostResponseBody>> setExistingPostAsMemberPost(@PathVariable Long postId) {
        PostResponseBody postResponseBody = postService.setAsMemberPost(postId);
        return ResponseEntity.ok(new APIResponse<>(true, "Post marked as a member post successfully", postResponseBody));
    }

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PatchMapping("/post/{postId}/archive")
    public ResponseEntity<APIResponse<PostResponseBody>> archivePost(@PathVariable Long postId) {
        PostResponseBody archivedPost = postService.archivePost(postId);
        return ResponseEntity.ok(new APIResponse<>(true, "Post archived successfully", archivedPost));
    }

    @PreAuthorize("hasAuthority('POST_READ')")
    @GetMapping("/archived")
    public ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getArchivedPosts(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection) {
        PageableResponse<PostResponseBody> archivedPosts = postService.getArchivedPosts(pageNumber, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(new APIResponse<>(true, "Archived posts fetched successfully", archivedPosts));
    }

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PatchMapping("/post/{postId}/unarchive")
    public ResponseEntity<APIResponse<PostResponseBody>> unarchivePost(@PathVariable Long postId) {
        PostResponseBody unarchivedPost = postService.unarchivePost(postId);
        return ResponseEntity.ok(new APIResponse<>(true, "Post unarchived successfully", unarchivedPost));
    }

    @PreAuthorize("hasAuthority('POST_DELETE')")
    @DeleteMapping("/{postId}")
    public ResponseEntity<APIResponse<Void>> deletePost(@PathVariable Long postId) {

        this.postService.deletePost(postId);
        return ResponseEntity.ok(new APIResponse<>(true, "Post Deleted Successfully"));
    }

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PatchMapping(value = "/post/{postId}/deactivate")
    public ResponseEntity<APIResponse<PostResponseBody>> patchPostDeactivate(@PathVariable Long postId) throws IOException {
        PostResponseBody postResponseBody = this.postService.deactivatePost(postId);
        return ResponseEntity.ok(new APIResponse<>(true, "Post Deactivated Successfully"));
    }

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PatchMapping(value = "/post/{postId}/activate")
    public ResponseEntity<APIResponse<PostResponseBody>> patchPostActivate(@PathVariable Long postId) throws IOException {
        PostResponseBody postResponseBody = this.postService.activatePost(postId);
        return ResponseEntity.ok(new APIResponse<>(true, "Post Activated Successfully"));
    }

    @PreAuthorize("hasAuthority('POST_READ')")
    @GetMapping("/user/{username}")
    public ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getPostsByUser(
            @PathVariable String username,
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
    ) {
        PageableResponse<PostResponseBody> postDTOSByUser = this.postService.getPostsByUser(username, pageNumber, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(new APIResponse<>(true, "User's posts fetched successfully", postDTOSByUser));
    }

    @PreAuthorize("hasAuthority('POST_READ')")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getPostsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
    ) {
        PageableResponse<PostResponseBody> postDTOSByCategory = this.postService.getPostsByCategory(categoryId, pageNumber, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(new APIResponse<>(true, "Category posts fetched successfully", postDTOSByCategory));
    }

    @PreAuthorize("hasAuthority('POST_READ')")
    @GetMapping("/search/{searchKey}")
    public ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getPostsByTitleSearch(
            @PathVariable String searchKey,
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
    ) {
        PageableResponse<PostResponseBody> postResponseBodyList = this.postService.searchPostsByTitle(searchKey, pageNumber, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(new APIResponse<>(true, "Posts fetched successfully", postResponseBodyList));
    }

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PostMapping("/image/upload")
    public ResponseEntity<APIResponse<String>> uploadPostImage(@RequestPart("image") MultipartFile image) throws IOException {
        String imageFileName = this.fileService.uploadImage(postBucketPath + "/images_file/", image);
        return ResponseEntity.ok(new APIResponse<>(true, "Image uploaded successfully", imageFileName)); // Consider returning a full URL or a reference ID
    }

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PostMapping("/video/upload")
    public ResponseEntity<APIResponse<String>> uploadPostVideo(@RequestPart("video") MultipartFile video) throws IOException {
        String videoFileName = this.fileService.uploadVideo(postBucketPath + "/videos_file/", video);
        return ResponseEntity.ok(new APIResponse<>(true, "Video uploaded successfully", videoFileName)); // Consider returning a full URL or a reference ID
    }


    @PreAuthorize("hasAuthority('POST_READ')")
    @GetMapping(value = "/image/{imageName}", produces = {MediaType.IMAGE_JPEG_VALUE,
                                                MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE})
    public void downloadImage(@PathVariable String imageName, HttpServletResponse httpServletResponse) throws IOException {
        InputStream resource = this.fileService.getResource(postBucketPath + "/images_file/", imageName);
        httpServletResponse.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(resource, httpServletResponse.getOutputStream());
    }
}
