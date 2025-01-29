package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.reaction.ReactionSummaryResponse;
import com.jk.blog.dto.reaction.ReactionRequest;
import com.jk.blog.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reactions")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @PreAuthorize("hasAuthority('POST_WRITE')")
    @PostMapping("/post/{postId}")
    public ResponseEntity<APIResponse<String>> reactToPost(
            @PathVariable Long postId,
            @RequestBody ReactionRequest request) {

        if (request == null || request.getEmoji() == null) {
            return ResponseEntity.badRequest().body(new APIResponse<>(false, "Invalid reaction data"));
        }

        this.reactionService.reactToPost(postId, request.getEmoji());
        return ResponseEntity.ok(new APIResponse<>(true, "Reaction added/updated successfully"));
    }

    @PreAuthorize("hasAuthority('COMMENT_WRITE')")
    @PostMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity<APIResponse<String>> reactToComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody ReactionRequest request) {

        if (request == null || request.getEmoji() == null) {
            return ResponseEntity.badRequest().body(new APIResponse<>(false, "Invalid reaction data"));
        }

        this.reactionService.reactToComment(postId, commentId, request.getEmoji());
        return ResponseEntity.ok(new APIResponse<>(true, "Reaction added/updated successfully"));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post/{postId}/counts")
    public ResponseEntity<APIResponse<ReactionSummaryResponse>> getReactionCountsForPost(@PathVariable Long postId) {
        ReactionSummaryResponse counts = this.reactionService.getReactionCountsForPost(postId);
        return ResponseEntity.ok(new APIResponse<>(true, "Post reactions fetched successfully", counts));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/comment/{commentId}/counts")
    public ResponseEntity<APIResponse<ReactionSummaryResponse>> getReactionCountsForComment(@PathVariable Long commentId) {
        ReactionSummaryResponse counts = this.reactionService.getReactionCountsForComment(commentId);
        return ResponseEntity.ok(new APIResponse<>(true, "Comment reactions fetched successfully", counts));
    }
}

