package com.jk.blog.service;

import com.jk.blog.dto.CommentRequestBody;
import com.jk.blog.dto.CommentResponseBody;

import java.util.List;

public interface CommentService {

    CommentResponseBody createComment(CommentRequestBody commentRequestBody, Long postId);

    List<CommentResponseBody> getAllComments(Long postId);

    CommentResponseBody getCommentById(Long commentId);

    CommentResponseBody updateComment(CommentRequestBody commentRequestBody, Long commentId);

    void deleteComment(Long commentId);

    void deactivateComment(Long commentId);

    void activateComment(Long commentId);

}
