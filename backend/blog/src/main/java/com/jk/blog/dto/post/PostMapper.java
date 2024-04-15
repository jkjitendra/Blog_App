package com.jk.blog.dto.post;


import com.jk.blog.dto.post.PostRequestBody;
import com.jk.blog.dto.post.PostResponseBody;
import com.jk.blog.entity.Category;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.Tag;
import com.jk.blog.entity.User;
import com.jk.blog.utils.DateTimeUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class PostMapper {

    public static Post postRequestBodyToPost(PostRequestBody postRequestBody, User user, Category category) {
        Post post = new Post();
        post.setPostTitle(postRequestBody.getTitle());
        post.setPostContent(postRequestBody.getContent());
        post.setImageUrl(postRequestBody.getImageUrl());
        post.setVideoUrl(postRequestBody.getVideoUrl());
        post.setLive(true);
        post.setUser(user);
        post.setCategory(category);
        return post;
    }

    public static PostResponseBody postToPostResponseBody(Post updatedPost) {
        PostResponseBody patchedPost = new PostResponseBody();
        patchedPost.setPostId(updatedPost.getPostId());
        patchedPost.setTitle(updatedPost.getPostTitle());
        patchedPost.setContent(updatedPost.getPostContent());
        patchedPost.setImageUrl(updatedPost.getImageUrl());
        patchedPost.setVideoUrl(updatedPost.getVideoUrl());
        patchedPost.setPostCreatedDate(DateTimeUtil.formatInstantToIsoString(updatedPost.getPostCreatedDate()));
        patchedPost.setPostLastUpdatedDate(DateTimeUtil.formatInstantToIsoString(updatedPost.getPostLastUpdatedDate()));
        patchedPost.setPostDeleted(updatedPost.isPostDeleted());
        patchedPost.setPostDeletionTimestamp(DateTimeUtil.formatInstantToIsoString(updatedPost.getPostDeletionTimestamp()));
        patchedPost.setUserId(updatedPost.getUser().getUserId());
        patchedPost.setCategoryId(updatedPost.getCategory().getCategoryId());
        patchedPost.setIsLive(updatedPost.isLive());

        patchedPost.setComments(new ArrayList<>());

        // Convert tag names to a set of strings
        Set<String> tagNames = updatedPost.getTags().stream()
                                          .map(Tag::getTagName)
                                          .collect(Collectors.toSet());
        patchedPost.setTagNames(tagNames);

        return patchedPost;
    }
}