package com.jk.blog.controller.api;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.comment.CommentRequestBody;
import com.jk.blog.dto.comment.CommentResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

public interface CommentApi {

    @Operation(summary = "Create a comment", description = "Allows authenticated users to create a comment on a post.")
    @ApiResponse(responseCode = "201", description = "Comment created successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<CommentResponseBody>> createComment(@RequestBody CommentRequestBody commentRequestBody, @PathVariable Long postId);

    @Operation(summary = "Get all comments for a post", description = "Fetches all comments for a specific post.")
    @ApiResponse(responseCode = "200", description = "Comments fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post not found")
    ResponseEntity<APIResponse<List<CommentResponseBody>>> getAllComment(@PathVariable Long postId);

    @Operation(summary = "Get comment by ID", description = "Fetches a comment by its ID.")
    @ApiResponse(responseCode = "200", description = "Comment fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "404", description = "Comment not found")
    ResponseEntity<APIResponse<CommentResponseBody>> getCommentById(@PathVariable Long commentId);

    @Operation(summary = "Update a comment", description = "Allows the comment owner to update the comment.")
    @ApiResponse(responseCode = "200", description = "Comment updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not the owner of the comment")
    ResponseEntity<APIResponse<CommentResponseBody>> updateComment(@Valid @RequestBody CommentRequestBody commentRequestBody, @PathVariable Long commentId) throws IOException;

    @Operation(summary = "Delete a comment", description = "Allows the comment owner to delete the comment.")
    @ApiResponse(responseCode = "200", description = "Comment deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not the owner of the comment")
    ResponseEntity<APIResponse<String>> deleteComment(@PathVariable Long commentId);

    @Operation(summary = "Delete multiple comments", description = "Allows post owners or admins to delete multiple comments on a post.")
    @ApiResponse(responseCode = "200", description = "Comments deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User lacks necessary permissions")
    @ApiResponse(responseCode = "404", description = "Post or comments not found")
    ResponseEntity<APIResponse<String>> deleteMultipleComments(
            @PathVariable Long postId, @RequestBody List<Long> commentIds);

    @Operation(summary = "Deactivate a comment", description = "Allows the owner of the comment to deactivate it.")
    @ApiResponse(responseCode = "200", description = "Comment deactivated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not the owner of the comment")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    ResponseEntity<APIResponse<String>> patchCommentDeactivate(@PathVariable Long commentId) throws IOException;

    @Operation(summary = "Activate a comment", description = "Allows the owner of the comment to activate it.")
    @ApiResponse(responseCode = "200", description = "Comment activated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not the owner of the comment")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    ResponseEntity<APIResponse<String>> patchCommentActivate(@PathVariable Long commentId) throws IOException;


}
