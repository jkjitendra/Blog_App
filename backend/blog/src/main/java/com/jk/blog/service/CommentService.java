package com.jk.blog.service;

import com.jk.blog.dto.comment.CommentRequestBody;
import com.jk.blog.dto.comment.CommentResponseBody;

import java.util.List;

public interface CommentService {

    CommentResponseBody createComment(CommentRequestBody commentRequestBody, Long postId);

    List<CommentResponseBody> getAllComments(Long postId);

    CommentResponseBody getCommentById(Long commentId);

    Long getCommentUserId(Long commentId);

    CommentResponseBody updateComment(CommentRequestBody commentRequestBody, Long commentId);

    void deleteComment(Long commentId);

    /**
     * Checks if the user has permission to delete a specific comment.
     */
    boolean canDeleteComment(Long userId, Long commentId);

    /**
     * Checks if the user has permission to delete multiple comments on a specific post.
     */
    boolean canBulkDelete(Long userId, Long postId);

    /**
     * Deletes multiple comments from a specific post.
     */
    void deleteMultipleComments(Long postId, List<Long> commentIds);


    void deactivateComment(Long commentId);

    void activateComment(Long commentId);

}
