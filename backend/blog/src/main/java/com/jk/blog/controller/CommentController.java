package com.jk.blog.controller;

import com.jk.blog.dto.*;
import com.jk.blog.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/post/{postId}/comments")
    public ResponseEntity<CommentResponseBody> createComment(@RequestBody CommentRequestBody commentRequestBody, @PathVariable Long postId) {
        CommentResponseBody commentResponseBody = this.commentService.createComment(commentRequestBody, postId);
        return new ResponseEntity<>(commentResponseBody, HttpStatus.CREATED);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseBody>> getAllComment(@PathVariable Long postId) {
        List<CommentResponseBody> commentList = this.commentService.getAllComments(postId);
        return new ResponseEntity<>(commentList, HttpStatus.OK);
    }

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<CommentResponseBody> getCommentById(@PathVariable Long commentId) {
        CommentResponseBody commentResponseBody = this.commentService.getCommentById(commentId);
        return new ResponseEntity<>(commentResponseBody, HttpStatus.OK);
    }

    @PutMapping("/comment/{commentId}")
    public ResponseEntity<CommentResponseBody> updateComment(@Valid @RequestBody CommentRequestBody commentRequestBody,
                                                       @PathVariable Long commentId) throws IOException {

        CommentResponseBody updatedComment = this.commentService.updateComment(commentRequestBody, commentId);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<APIResponse> deleteComment(@PathVariable Long commentId) {
        this.commentService.deleteComment(commentId);
        return new ResponseEntity<>(new APIResponse("Comment deleted Successfully", true), HttpStatus.OK);
    }


    @PatchMapping(value = "/comment/{commentId}/deactivate")
    public ResponseEntity<APIResponse> patchCommentDeactivate(@PathVariable Long commentId) throws IOException {
        this.commentService.deactivateComment(commentId);
        return new ResponseEntity<>(new APIResponse("Comment Deactivated Successfully", true), HttpStatus.OK);
    }

    @PatchMapping(value = "/post/{postId}/activate")
    public ResponseEntity<APIResponse> patchCommentActivate(@PathVariable Long commentId) throws IOException {
        this.commentService.activateComment(commentId);
        return new ResponseEntity<>(new APIResponse("Comment Activated Successfully", true), HttpStatus.OK);
    }
}
