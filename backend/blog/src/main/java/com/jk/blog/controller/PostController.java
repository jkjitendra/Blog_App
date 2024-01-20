package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.PostDTO;
import com.jk.blog.service.PostService;
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
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO) {
        PostDTO createdPostDTO = this.postService.createPost(postDTO);
        return new ResponseEntity<>(createdPostDTO, HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<List<PostDTO>> getAllPost(@RequestBody PostDTO postDTO) {
        List<PostDTO> postDTOList = this.postService.getAllPost();
        return new ResponseEntity<>(postDTOList, HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long postId) {
        PostDTO existingPostDTO = this.postService.getPostById(postId);
        return new ResponseEntity<>(existingPostDTO, HttpStatus.OK);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDTO> updatePost(@RequestBody PostDTO postDTO, @PathVariable Long postId) {
        PostDTO updatePost = this.postService.updatePost(postDTO, postId);
        return new ResponseEntity<>(updatePost, HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<APIResponse> deleteUser(@PathVariable Long postId) {
        this.postService.deletePost(postId);
        return new ResponseEntity<>(new APIResponse("Post Deleted Successfully", true), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<List<PostDTO>> getPostsByUser(@PathVariable Long userId) {
        List<PostDTO> postDTOSByUser = this.postService.getPostsByUser(userId);
        return new ResponseEntity<>(postDTOSByUser, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}/posts")
    public ResponseEntity<List<PostDTO>> getPostsByCategory(@PathVariable Long categoryId) {
        List<PostDTO> postDTOSByCategory = this.postService.getPostsByCategory(categoryId);
        return new ResponseEntity<>(postDTOSByCategory, HttpStatus.OK);
    }

    @GetMapping("/search/{searchKey}")
    public ResponseEntity<List<PostDTO>> getPostsByTitleSearch(@PathVariable String searchKey) {
        List<PostDTO> postDTOSBySearchKey = this.postService.searchPostsByTitle(searchKey);
        return new ResponseEntity<>(postDTOSBySearchKey, HttpStatus.OK);
    }
}
