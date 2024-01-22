package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.PostRequestBody;
import com.jk.blog.dto.PostResponseBody;
import com.jk.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/")
    public ResponseEntity<PostResponseBody> createPost(@Valid @RequestBody PostRequestBody postRequestBody) {
        PostResponseBody createdPostRequestBody = this.postService.createPost(postRequestBody);
        return new ResponseEntity<>(createdPostRequestBody, HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<PageableResponse<PostResponseBody>> getAllPost(
                            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
                            @RequestParam(value = "sortBy", defaultValue = "postId", required = false) String sortBy,
                            @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection
                            ) {
        PageableResponse<PostResponseBody> pageableResponse = this.postService.getAllPost(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(pageableResponse, HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseBody> getPostById(@PathVariable Long postId) {
        PostResponseBody existingPostRequestBody = this.postService.getPostById(postId);
        return new ResponseEntity<>(existingPostRequestBody, HttpStatus.OK);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseBody> updatePost(@Valid @RequestBody PostRequestBody postRequestBody, @PathVariable Long postId) {
        PostResponseBody updatePost = this.postService.updatePost(postRequestBody, postId);
        return new ResponseEntity<>(updatePost, HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<APIResponse> deleteUser(@PathVariable Long postId) {
        this.postService.deletePost(postId);
        return new ResponseEntity<>(new APIResponse("Post Deleted Successfully", true), HttpStatus.OK);
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
        List<PostResponseBody> postDTOSBySearchKey = this.postService.searchPostsByTitle(searchKey);
        return new ResponseEntity<>(postDTOSBySearchKey, HttpStatus.OK);
    }
}
