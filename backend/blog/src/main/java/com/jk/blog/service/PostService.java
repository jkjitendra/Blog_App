package com.jk.blog.service;

import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.post.PostRequestBody;
import com.jk.blog.dto.post.PostResponseBody;

import java.util.List;

public interface PostService {

    /**
     * Creates a new post for the given user.
     * @param userId The ID of the user creating the post.
     * @param postRequestBody The request body containing post details.
     * @return The created post as a response body.
     * @throws UnAuthorizedException if the user is not authenticated.
     * @throws ResourceNotFoundException if the user or category is not found.
     */
    PostResponseBody createPost(Long userId, PostRequestBody postRequestBody);

    /**
     * Fetches all posts with optional pagination and sorting.
     * @param pageNumber The page number to fetch.
     * @param pageSize The number of items per page.
     * @param sortBy The field to sort by.
     * @param sortDirection The direction of sorting ("asc" or "desc").
     * @return A pageable response containing the list of posts.
     */
    PageableResponse<PostResponseBody> getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);

    /**
     * Fetches a post by its ID.
     * @param postId The ID of the post to fetch.
     * @return The post details as a response body.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws UnAuthorizedException if the user is not authorized to view the post.
     */
    PostResponseBody getPostById(Long postId);

    /**
     * Updates an existing post by replacing all fields with the provided values.
     * @param postRequestBody The request body containing updated post details.
     * @param postId The ID of the post to update.
     * @return The updated post as a response body.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws UnAuthorizedException if the user is not authorized to update the post.
     */
    PostResponseBody updatePost(PostRequestBody postRequestBody, Long postId);

    /**
     * Partially updates an existing post by modifying only specified fields.
     * @param postRequestBody The request body containing fields to update.
     * @param postId The ID of the post to patch.
     * @return The patched post as a response body.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws UnAuthorizedException if the user is not authorized to update the post.
     */
    PostResponseBody patchPost(PostRequestBody postRequestBody, Long postId);

    /**
     * Archives a post by marking it as archived.
     * @param postId The ID of the post to archive.
     * @return The updated post response.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws UnAuthorizedException if the user is not authorized to archive the post.
     */
    PostResponseBody archivePost(Long postId);

    /**
     * Unarchives a post by marking it as active.
     * @param postId The ID of the post to unarchive.
     * @return The updated post response.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws InvalidPostStateException if the post is not archived.
     * @throws UnAuthorizedException if the user is not authorized to unarchive the post.
     */
    PostResponseBody unarchivePost(Long postId);

    /**
     * Fetches all archived posts with optional pagination and sorting.
     * @param pageNumber The page number to fetch.
     * @param pageSize The number of items per page.
     * @param sortBy The field to sort by (e.g., "postCreatedDate").
     * @param sortDirection The direction of sorting ("asc" or "desc").
     * @return A pageable response containing the list of archived posts.
     */
    PageableResponse<PostResponseBody> getArchivedPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);

    /**
     * Toggles the visibility of a post by setting its `isLive` status.
     * @param postId The ID of the post to toggle visibility for.
     * @param isLive The new visibility status (true for live, false for not live).
     * @return The updated post response.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws UnAuthorizedException if the user is not authorized to update the post visibility.
     */
    PostResponseBody togglePostVisibility(Long postId, boolean isLive);

    /**
     * Marks an existing post as a member-only post.
     * @param postId The ID of the post to mark as a member post.
     * @return The updated post response.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws UnAuthorizedException if the user is not authorized to mark the post as a member post.
     */
    PostResponseBody setAsMemberPost(Long postId);

    /**
     * Deletes a post permanently from the system.
     * @param postId The ID of the post to delete.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws UnAuthorizedException if the user is not authorized to delete the post.
     */
    void deletePost(Long postId);

    /**
     * Fetches all posts created by a specific user with optional pagination and sorting.
     * @param userId The ID of the user whose posts are to be fetched.
     * @param pageNumber The page number to fetch.
     * @param pageSize The number of items per page.
     * @param sortBy The field to sort by.
     * @param sortDirection The direction of sorting ("asc" or "desc").
     * @return A pageable response containing the list of posts by the user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    PageableResponse<PostResponseBody> getPostsByUser(Long userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);

    /**
     * Fetches all posts belonging to a specific category with optional pagination and sorting.
     * @param categoryId The ID of the category.
     * @param pageNumber The page number to fetch.
     * @param pageSize The number of items per page.
     * @param sortBy The field to sort by.
     * @param sortDirection The direction of sorting ("asc" or "desc").
     * @return A pageable response containing the list of posts in the category.
     * @throws ResourceNotFoundException if the category is not found.
     */
    PageableResponse<PostResponseBody> getPostsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);

    /**
     * Searches for posts by title using a keyword with optional pagination and sorting.
     * @param keyword The keyword to search for in the title.
     * @param pageNumber The page number to fetch.
     * @param pageSize The number of items per page.
     * @param sortBy The field to sort by.
     * @param sortDirection The direction of sorting ("asc" or "desc").
     * @return A pageable response containing the list of matching posts.
     */
    PageableResponse<PostResponseBody> searchPostsByTitle(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);

    /**
     * Deactivates a post by marking it as deleted without permanently removing it.
     * @param postId The ID of the post to deactivate.
     * @return The updated post response.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws UnAuthorizedException if the user is not authorized to deactivate the post.
     */
    PostResponseBody deactivatePost(Long postId);

    /**
     * Reactivates a previously deactivated post.
     * @param postId The ID of the post to reactivate.
     * @return The updated post response.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws UnAuthorizedException if the user is not authorized to activate the post.
     */
    PostResponseBody activatePost(Long postId);

}
