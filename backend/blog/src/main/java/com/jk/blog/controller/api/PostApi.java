package com.jk.blog.controller.api;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.post.PostResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PostApi {

    @Operation(summary = "Create Post", description = "Allows authorized users to create a new post.")
    @ApiResponse(responseCode = "201", description = "Post created successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid post data provided")
    ResponseEntity<APIResponse<PostResponseBody>> createPost(
            @RequestPart("post") String postRequestBody,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "video", required = false) MultipartFile video) throws IOException;

    @Operation(summary = "Fetch All Posts", description = "Retrieves a paginated list of all posts.")
    @ApiResponse(responseCode = "200", description = "Posts fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getAllPost(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection);

    @Operation(summary = "Fetch Post by ID", description = "Retrieves details of a specific post.")
    @ApiResponse(responseCode = "200", description = "Post fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post not found")
    ResponseEntity<APIResponse<PostResponseBody>> getPostById(@PathVariable Long postId);

    @Operation(summary = "Update Post", description = "Allows authorized users to update their post.")
    @ApiResponse(responseCode = "200", description = "Post updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid post data provided")
    ResponseEntity<APIResponse<Void>> updatePost(
            @PathVariable Long postId,
            @RequestPart("post") String postRequestBody,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "video", required = false) MultipartFile video) throws IOException;

    @Operation(summary = "Patch Update Post", description = "Partially updates a post by allowing specific fields to be modified.")
    @ApiResponse(responseCode = "200", description = "Post updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid post data provided")
    ResponseEntity<APIResponse<Void>> patchPost(
            @PathVariable Long postId,
            @RequestPart("post") String updatesJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "video", required = false) MultipartFile video) throws IOException;

    @Operation(summary = "Toggle Post Visibility", description = "Sets the visibility of a post (e.g., live or hidden).")
    @ApiResponse(responseCode = "200", description = "Post visibility updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<Void>> togglePostVisibility(
            @PathVariable Long postId, @RequestParam boolean isLive);

    @Operation(summary = "Set Post as Member Post", description = "Marks an existing post as a member-only post.")
    @ApiResponse(responseCode = "200", description = "Post marked as member post successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PostResponseBody>> toggleMemberPostVisibility(@PathVariable Long postId, @RequestParam boolean visible);

    @Operation(summary = "Archive Post", description = "Archives a post so it is no longer publicly visible.")
    @ApiResponse(responseCode = "200", description = "Post archived successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PostResponseBody>> archivePost(@PathVariable Long postId);

    @Operation(summary = "Get Archived Posts", description = "Retrieves a list of all archived posts.")
    @ApiResponse(responseCode = "200", description = "Archived posts fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getArchivedPosts(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection);

    @Operation(summary = "Unarchive Post", description = "Restores an archived post back to visibility.")
    @ApiResponse(responseCode = "200", description = "Post unarchived successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PostResponseBody>> unarchivePost(@PathVariable Long postId);

    @Operation(summary = "Delete Post", description = "Allows authorized users to delete their post.")
    @ApiResponse(responseCode = "200", description = "Post deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not the owner of the post")
    ResponseEntity<APIResponse<Void>> deletePost(@PathVariable Long postId);

    @Operation(summary = "Deactivate Post", description = "Temporarily deactivates a post, making it unavailable.")
    @ApiResponse(responseCode = "200", description = "Post deactivated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PostResponseBody>> patchPostDeactivate(@PathVariable Long postId) throws IOException;

    @Operation(summary = "Activate Post", description = "Restores a deactivated post, making it visible again.")
    @ApiResponse(responseCode = "200", description = "Post activated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PostResponseBody>> patchPostActivate(@PathVariable Long postId) throws IOException;

    @Operation(summary = "Get Posts by User", description = "Retrieves all posts created by a specific user.")
    @ApiResponse(responseCode = "200", description = "User posts fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getPostsByUser(
            @PathVariable String username,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection);

    @Operation(summary = "Get Posts by Category", description = "Retrieves all posts belonging to a specific category.")
    @ApiResponse(responseCode = "200", description = "Category posts fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getPostsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection);

    @Operation(summary = "Search Posts", description = "Search posts by title keyword.")
    @ApiResponse(responseCode = "200", description = "Posts fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> getPostsByTitleSearch(
            @PathVariable String searchKey,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection);

    @Operation(summary = "Upload Post Image", description = "Uploads an image for a post.")
    @ApiResponse(responseCode = "200", description = "Image uploaded successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<String>> uploadPostImage(@RequestPart("image") MultipartFile image) throws IOException;

    @Operation(summary = "Upload Post Video", description = "Uploads a video for a post.")
    @ApiResponse(responseCode = "200", description = "Video uploaded successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ResponseEntity<APIResponse<String>> uploadPostVideo(@RequestPart("video") MultipartFile video) throws IOException;

    @Operation(summary = "Download Post Image", description = "Downloads an image associated with a post.")
    @ApiResponse(responseCode = "200", description = "Image fetched successfully")
    @ApiResponse(responseCode = "404", description = "Image not found")
    void downloadImage(@PathVariable String imageName, HttpServletResponse httpServletResponse) throws IOException;

}