package com.jk.blog.controller;

import com.jk.blog.constants.SecurityConstants;
import com.jk.blog.controller.api.CommentApi;
import com.jk.blog.dto.*;
import com.jk.blog.dto.comment.CommentRequestBody;
import com.jk.blog.dto.comment.CommentResponseBody;
import com.jk.blog.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_NAME)
@Tag(name = "Comment Management", description = "APIs for managing comments on blog posts")
public class CommentController implements CommentApi {

    @Autowired
    private CommentService commentService;

    /**
     * Create a comment for a specific post.
     * Only authenticated users can create comments.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}")
    public ResponseEntity<APIResponse<CommentResponseBody>> createComment(@RequestBody CommentRequestBody commentRequestBody, @PathVariable Long postId) {
        CommentResponseBody commentResponseBody = this.commentService.createComment(commentRequestBody, postId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse<>(true, "Comment created successfully", commentResponseBody));
    }

    /**
     * Fetch all comments for a specific post.
     * Accessible to any authenticated user.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post/{postId}")
    public ResponseEntity<APIResponse<List<CommentResponseBody>>> getAllComment(@PathVariable Long postId) {
        List<CommentResponseBody> commentList = this.commentService.getAllComments(postId);
        return ResponseEntity.ok(new APIResponse<>(true, "Comments fetched successfully", commentList));
    }

    /**
     * Fetch a specific comment by its ID.
     * Accessible to any authenticated user.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<APIResponse<CommentResponseBody>> getCommentById(@PathVariable Long commentId) {
        CommentResponseBody commentResponseBody = this.commentService.getCommentById(commentId);
        return ResponseEntity.ok(new APIResponse<>(true, "Comment fetched successfully", commentResponseBody));
    }

    /**
     * Update a specific comment by its ID.
     * Only the owner of the comment can update it.
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/comment/{commentId}")
    public ResponseEntity<APIResponse<CommentResponseBody>> updateComment(@Valid @RequestBody CommentRequestBody commentRequestBody,
                                                       @PathVariable Long commentId) throws IOException {

        CommentResponseBody updatedComment = this.commentService.updateComment(commentRequestBody, commentId);
        return ResponseEntity.ok(new APIResponse<>(true, "Comment updated successfully", updatedComment));
    }

    /**
     * Delete a specific comment by its ID.
     * Applies role-based constraints for deletion.
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<APIResponse<String>> deleteComment(@PathVariable Long commentId) {
        this.commentService.deleteComment(commentId);
        return ResponseEntity.ok(new APIResponse<>(true, "Comment deleted successfully"));
    }

    /**
     * Delete multiple comments from a post.
     * Only post owners or privileged users (Admin/Moderator) can perform this action.
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/post/{postId}/bulk-delete")
    public ResponseEntity<APIResponse<String>> deleteMultipleComments(
            @PathVariable Long postId, @RequestBody List<Long> commentIds) {
        this.commentService.deleteMultipleComments(postId, commentIds);
        return ResponseEntity.ok(new APIResponse<>(true, "Comments deleted successfully"));
    }

    /**
     * Deactivate a specific comment by its ID.
     * Only the owner of the comment can deactivate it.
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping(value = "/comment/{commentId}/deactivate")
    public ResponseEntity<APIResponse<String>> patchCommentDeactivate(@PathVariable Long commentId) throws IOException {
        this.commentService.deactivateComment(commentId);
        return ResponseEntity.ok(new APIResponse<>(true, "Comment deactivated successfully"));
    }

    /**
     * Activate a specific comment by its ID.
     * Only the owner of the comment can activate it.
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping(value = "/post/{postId}/activate")
    public ResponseEntity<APIResponse<String>> patchCommentActivate(@PathVariable Long commentId) throws IOException {
        this.commentService.activateComment(commentId);
        return ResponseEntity.ok(new APIResponse<>(true, "Comment activated successfully"));
    }
}
