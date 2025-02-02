package com.jk.blog.controller.api;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.reaction.ReactionRequest;
import com.jk.blog.dto.reaction.ReactionSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface ReactionApi {

    @Operation(summary = "React to a post", description = "Allows users to react to a post.")
    @ApiResponse(responseCode = "200", description = "Reaction added/updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<String>> reactToPost(@PathVariable Long postId, @RequestBody ReactionRequest request);

    @Operation(summary = "React to a comment", description = "Allows users to react to a comment.")
    @ApiResponse(responseCode = "200", description = "Reaction added/updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<String>> reactToComment(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody ReactionRequest request);

    @Operation(summary = "Get reactions for a post", description = "Fetches all reactions for a specific post.")
    @ApiResponse(responseCode = "200", description = "Post reactions fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post not found")
    ResponseEntity<APIResponse<ReactionSummaryResponse>> getReactionCountsForPost(@PathVariable Long postId);

    @Operation(summary = "Get reactions for a comment", description = "Fetches all reactions for a specific comment.")
    @ApiResponse(responseCode = "200", description = "Comment reactions fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "404", description = "Comment not found")
    ResponseEntity<APIResponse<ReactionSummaryResponse>> getReactionCountsForComment(@PathVariable Long commentId);


}
