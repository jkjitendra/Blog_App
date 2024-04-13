package com.jk.blog.service;

import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.PostRequestBody;
import com.jk.blog.dto.PostResponseBody;
import com.jk.blog.dto.ProfileResponseBody;

import java.util.List;
import java.util.Map;

public interface PostService {

    PostResponseBody createPost(Long userId, PostRequestBody postRequestBody);

    PageableResponse<PostResponseBody> getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);

    PostResponseBody getPostById(Long postId);

    PostResponseBody updatePost(PostRequestBody postRequestBody, Long postId);

    PostResponseBody patchPost(PostRequestBody postRequestBody, Long postId);

    void togglePostVisibility(Long postId, boolean isLive);

    void deletePost(Long postId);

    List<PostResponseBody> getPostsByUser(Long userId);

    List<PostResponseBody> getPostsByCategory(Long categoryId);

    List<PostResponseBody> searchPostsByTitle(String keyword);

    void deactivatePost(Long postId);

    void activatePost(Long postId);

}
