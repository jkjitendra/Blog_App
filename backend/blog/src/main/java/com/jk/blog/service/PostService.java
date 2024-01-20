package com.jk.blog.service;

import com.jk.blog.dto.PostDTO;
import com.jk.blog.entity.Post;

import java.util.List;

public interface PostService {
    PostDTO createPost(PostDTO postDTO);
    PostDTO updatePost(PostDTO postDTO, Long postId);
    void deletePost(Long postId);
    List<PostDTO> getAllPost();
    PostDTO getPostById(Long postId);
    List<PostDTO> getPostsByUser(Long userId);
    List<PostDTO> getPostsByCategory(Long categoryId);
    List<PostDTO> searchPostsByTitle(String keyword);

}
