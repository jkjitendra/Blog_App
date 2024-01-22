package com.jk.blog.service;

import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.PostRequestBody;
import com.jk.blog.dto.PostResponseBody;

import java.util.List;

public interface PostService {
    PostResponseBody createPost(PostRequestBody postRequestBody);
    PostResponseBody updatePost(PostRequestBody postRequestBody, Long postId);
    void deletePost(Long postId);
    PageableResponse<PostResponseBody> getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    PostResponseBody getPostById(Long postId);
    List<PostResponseBody> getPostsByUser(Long userId);
    List<PostResponseBody> getPostsByCategory(Long categoryId);
    List<PostResponseBody> searchPostsByTitle(String keyword);

}
