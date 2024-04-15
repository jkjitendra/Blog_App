package com.jk.blog.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.constants.AppConstants;
import com.jk.blog.dto.*;
import com.jk.blog.dto.post.PostRequestBody;
import com.jk.blog.dto.post.PostResponseBody;
import com.jk.blog.dto.post.PostStatusResponse;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.FileService;
import com.jk.blog.service.PostService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("localFileService")
//    @Qualifier("s3FileService")
    private FileService fileService;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${project.files}")
    private String path;

    @PostMapping(value = "/user/{userId}/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseBody> createPost(
                                                       @PathVariable Long userId,
                                                       @Valid @RequestPart("post") String postRequestBody,
                                                       @RequestPart(value = "image", required = false) MultipartFile image,
                                                       @RequestPart(value = "video", required = false) MultipartFile video) throws IOException {
        this.userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Post", "userId", userId));
        PostRequestBody postJSON = new ObjectMapper().readValue(postRequestBody, PostRequestBody.class);
        postJSON.setUserId(userId);
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileService.uploadImage(path, image);
            postJSON.setImageUrl(imageUrl);
        }
        if (video != null && !video.isEmpty()) {
            String videoUrl = fileService.uploadVideo(path, video);
            postJSON.setVideoUrl(videoUrl);
        }
        PostResponseBody createdPostResponseBody = this.postService.createPost(userId, postJSON);
        System.out.println("createdPostResponseBody " + createdPostResponseBody);
        return new ResponseEntity<>(createdPostResponseBody, HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<PageableResponse<PostResponseBody>> getAllPost(
                            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
                            @RequestParam(value = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
                            ) {
        PageableResponse<PostResponseBody> pageableResponse = this.postService.getAllPost(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(pageableResponse, HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseBody> getPostById(@PathVariable Long postId) {
        PostResponseBody existingPostRequestBody = this.postService.getPostById(postId);
        return new ResponseEntity<>(existingPostRequestBody, HttpStatus.OK);
    }

    @PutMapping(value = "/user/{userId}/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseBody> updatePost(@PathVariable Long userId,
                                                       @PathVariable Long postId,
                                                       @Valid @RequestPart("post") String postRequestBody,
                                                       @RequestPart(value = "image", required = false) MultipartFile image,
                                                       @RequestPart(value = "video", required = false) MultipartFile video) throws IOException {

        this.userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Post", "userId", userId));
        PostRequestBody postJSON = new ObjectMapper().readValue(postRequestBody, PostRequestBody.class);

        if (image != null && !image.isEmpty()) {
            String imageUrl = fileService.uploadImage(path, image);
            postJSON.setImageUrl(imageUrl);
        }
        if (video != null && !video.isEmpty()) {
            String videoUrl = fileService.uploadVideo(path, video);
            postJSON.setVideoUrl(videoUrl);
        }
        PostResponseBody updatePost = this.postService.updatePost(postJSON, postId);
        return new ResponseEntity<>(updatePost, HttpStatus.OK);
    }

    @PatchMapping(value = "/user/{userId}/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseBody> patchPost( @PathVariable Long userId,
                                                       @PathVariable Long postId,
                                                       @RequestPart("post") String updatesJson,
                                                       @RequestPart(value = "image", required = false) MultipartFile image,
                                                       @RequestPart(value = "video", required = false) MultipartFile video) throws IOException {

        this.userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Post", "userId", userId));
        PostRequestBody postJSON = new ObjectMapper().readValue(updatesJson, new TypeReference<>() {});
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileService.uploadImage(path, image);
            postJSON.setImageUrl(imageUrl);
        }
        if (video != null && !video.isEmpty()) {
            String videoUrl = fileService.uploadVideo(path, video);
            postJSON.setVideoUrl(videoUrl);
        }
        PostResponseBody updatePost = this.postService.patchPost(postJSON, postId);
        return new ResponseEntity<>(updatePost, HttpStatus.OK);
    }

    @PatchMapping("/post/{postId}/visibility")
    public ResponseEntity<APIResponse> togglePostVisibility(@PathVariable Long postId, @RequestBody Map<String, Boolean> visibility) {
        boolean isLive = visibility.getOrDefault("isLive", false);
        this.postService.togglePostVisibility(postId, isLive);
        return new ResponseEntity<>(new APIResponse("isLive updated Successfully", true), HttpStatus.OK);
    }

    @DeleteMapping("/user/{userId}/posts/{postId}")
    public ResponseEntity<APIResponse> deletePost(@PathVariable Long userId, @PathVariable Long postId) {
        this.userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Post", "userId", userId));
        this.postService.deletePost(postId);
        return new ResponseEntity<>(new APIResponse("Post Deleted Successfully", true), HttpStatus.OK);
    }

    @PatchMapping(value = "/post/{postId}/deactivate")
    public ResponseEntity<PostStatusResponse> patchPostDeactivate(@PathVariable Long postId) throws IOException {
        PostResponseBody postResponseBody = this.postService.deactivatePost(postId);
        APIResponse apiResponse = new APIResponse("Post Deactivated Successfully", true);
        PostStatusResponse response = new PostStatusResponse(apiResponse, postResponseBody);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/post/{postId}/activate")
    public ResponseEntity<PostStatusResponse> patchPostActivate(@PathVariable Long postId) throws IOException {
        PostResponseBody postResponseBody = this.postService.activatePost(postId);
        APIResponse apiResponse = new APIResponse("Post Activated Successfully", true);
        PostStatusResponse response = new PostStatusResponse(apiResponse, postResponseBody);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<List<PostResponseBody>> getPostsByUser(@PathVariable Long userId) {
        List<PostResponseBody> postDTOSByUser = this.postService.getPostsByUser(userId);
        return new ResponseEntity<>(postDTOSByUser, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}/posts")
    public ResponseEntity<List<PostResponseBody>> getPostsByCategory(@PathVariable Long categoryId) {
        List<PostResponseBody> postDTOSByCategory = this.postService.getPostsByCategory(categoryId);
        return new ResponseEntity<>(postDTOSByCategory, HttpStatus.OK);
    }

    @GetMapping("/search/{searchKey}")
    public ResponseEntity<List<PostResponseBody>> getPostsByTitleSearch(@PathVariable String searchKey) {
        List<PostResponseBody> postResponseBodyList = this.postService.searchPostsByTitle(searchKey);
        return new ResponseEntity<>(postResponseBodyList, HttpStatus.OK);
    }

//    @PostMapping("/image/upload/{postId}")
//    public ResponseEntity<PostResponseBody> uploadPostImage(@RequestParam("image") MultipartFile image,
//                                                         @PathVariable Long postId) throws IOException {
//        PostResponseBody postResponseBody = this.postService.getPostById(postId);
//
    //        String imageFileName = this.fileService.uploadImage(path, image);
//
//        PostRequestBody postRequestBody = this.modelMapper.map(postResponseBody, PostRequestBody.class);
//        postRequestBody.setImageUrl(imageFileName);
//
//        PostResponseBody updatedResponse = this.postService.updatePost(postRequestBody, postId);
//        return new ResponseEntity<>(updatedResponse, HttpStatus.OK);
//    }

    @PostMapping("/image/upload")
    public ResponseEntity<String> uploadPostImage(@RequestParam("image") MultipartFile image) throws IOException {
        String imageFileName = this.fileService.uploadImage(path, image);
        return new ResponseEntity<>(imageFileName, HttpStatus.OK); // Consider returning a full URL or a reference ID
    }

    @PostMapping("/video/upload")
    public ResponseEntity<String> uploadPostVideo(@RequestParam("video") MultipartFile video) throws IOException {
        String videoFileName = this.fileService.uploadVideo(path, video);
        return new ResponseEntity<>(videoFileName, HttpStatus.OK); // Consider returning a full URL or a reference ID
    }


    @GetMapping(value = "/posts/image/{imageName}", produces = {MediaType.IMAGE_JPEG_VALUE,
                                                MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE})
    public void downloadImage(@PathVariable String imageName, HttpServletResponse httpServletResponse) throws IOException {
        InputStream resource = this.fileService.getResource(path, imageName);
        httpServletResponse.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(resource, httpServletResponse.getOutputStream());
    }
}
