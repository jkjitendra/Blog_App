package com.jk.blog.service.impl;

import com.jk.blog.constants.AppConstants;
import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.comment.CommentResponseBody;
import com.jk.blog.dto.post.PostMapper;
import com.jk.blog.dto.post.PostRequestBody;
import com.jk.blog.dto.post.PostResponseBody;
import com.jk.blog.entity.Category;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.Tag;
import com.jk.blog.entity.User;
import com.jk.blog.exception.InvalidPostStateException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.*;
import com.jk.blog.security.AuthenticationFacade;
import com.jk.blog.service.FileService;
import com.jk.blog.service.PostService;
import com.jk.blog.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    @Qualifier("localFileService")
    private FileService fileService;

    @Value("${aws.s3.bucket.post}")
    private String postBucketPath;

    @Override
    @Transactional
    public PostResponseBody createPost(PostRequestBody postRequestBody, MultipartFile image, MultipartFile video) throws IOException {
        // Get the authenticated user
        User user = AuthUtil.getAuthenticatedUser();
        if (user == null) {
            throw new UnAuthorizedException("User must be logged in to create a post.");
        }
        Category category = fetchCategoryById(postRequestBody.getCategoryId());

        Post post = PostMapper.postRequestBodyToPost(postRequestBody, user, category);

        handleMediaUpload(post, image, video);

//        post.setImageUrl(postRequestBody.getImageUrl());
//        post.setVideoUrl(postRequestBody.getVideoUrl());
        post.setMemberPost(authenticationFacade.hasAnyRole("ROLE_SUBSCRIBER", "ROLE_MODERATOR", "ROLE_ADMIN"));
        post.setLive(true);
        post.setArchived(false);
        post.setPostCreatedDate(Instant.now());
        post.setPostLastUpdatedDate(Instant.now());

        if (postRequestBody.getTagNames() != null) {
            Set<Tag> tags = fetchOrCreateTags(postRequestBody.getTagNames());
            post.setTags(tags);
        }

        Post savedPost = this.postRepository.save(post);
        return PostMapper.postToPostResponseBody(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseBody getPostById(Long postId) {
        Post existingPost = this.postRepository
                                .findByPostIdAndIsLiveTrueAndIsPostDeletedFalse(postId)
                                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        if (existingPost.isMemberPost() && !authenticationFacade.hasAnyRole("ROLE_SUBSCRIBER", "ROLE_MODERATOR", "ROLE_ADMIN")) {
            throw new UnAuthorizedException("You are not authorized to view this post.");
        }

        PostResponseBody responseBody = PostMapper.postToPostResponseBody(existingPost);

        List<CommentResponseBody> commentResponses = existingPost.getComments().stream()
                                                         .map(comment -> this.modelMapper.map(comment, CommentResponseBody.class))
                                                         .collect(Collectors.toList());
        responseBody.setComments(commentResponses);

        return responseBody;
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<PostResponseBody> getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, resolveSort(sortBy, sortDirection));

        Page<Post> pagePost = authenticationFacade.hasAnyRole("SUBSCRIBER")
                ? postRepository.findByIsLiveTrueAndIsPostDeletedFalse(pageable)
                : postRepository.findPublicPosts(pageable);

        List<PostResponseBody> postResponseBodyList = pagePost.getContent()
                .stream()
                .map(existingPost -> {
                    PostResponseBody postResponse = PostMapper.postToPostResponseBody(existingPost);

                    // Fetch a limited number of comments for each post, for example, the two most recent comments
                    postResponse.setComments(fetchTopComments(existingPost));

                    return postResponse;
        }).toList();

        return buildPageableResponse(pagePost, postResponseBodyList);
    }

    @Override
    @Transactional
    public PostResponseBody updatePost( Long postId, PostRequestBody postRequestBody, MultipartFile image, MultipartFile video) throws IOException {

        Post existingPost = fetchUnArchivedAndLivePostById(postId);

        validateModificationAuthorization(existingPost, "update");

        // Update all properties
        updatePostFields(existingPost, postRequestBody);
        handleMediaUpload(existingPost, image, video);
        existingPost.setPostLastUpdatedDate(Instant.now());
        
        Post updatedPost = this.postRepository.save(existingPost);

        return PostMapper.postToPostResponseBody(updatedPost);
    }

    @Override
    @Transactional
    public PostResponseBody patchPost(Long postId, PostRequestBody postRequestBody, MultipartFile image, MultipartFile video) throws IOException {

        Post existingPost = fetchUnArchivedAndLivePostById(postId);

        validateModificationAuthorization(existingPost, "patch");

        patchPostFields(existingPost, postRequestBody);
        handleMediaUpload(existingPost, image, video);
        existingPost.setPostLastUpdatedDate(Instant.now());

        Post updatedPost = this.postRepository.save(existingPost);

        return PostMapper.postToPostResponseBody(updatedPost);
    }

    @Override
    @Transactional
    public PostResponseBody archivePost(Long postId) {

        Post existingPost = fetchUnArchivedAndLivePostById(postId);
        validateModificationAuthorization(existingPost, "archive");

        existingPost.setArchived(true);
        existingPost.setPostLastUpdatedDate(Instant.now());

        Post archivedPost = this.postRepository.save(existingPost);
        return PostMapper.postToPostResponseBody(archivedPost);
    }

    @Override
    @Transactional
    public PostResponseBody unarchivePost(Long postId) {

        Post post = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        // Ensure the post is archived before unarchiving
        if (!post.isArchived()) {
            throw new InvalidPostStateException("Post is not archived and cannot be unarchived.");
        }

        // Allow unarchiving if the user owns the post or has admin/moderator role
        validateModificationAuthorization(post, "unarchive");

        post.setArchived(false);
        post.setPostLastUpdatedDate(Instant.now());

        Post unarchivedPost = this.postRepository.save(post);
        return PostMapper.postToPostResponseBody(unarchivedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<PostResponseBody> getArchivedPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {

        User authenticatedUser = AuthUtil.getAuthenticatedUser();

        boolean isAdminOrModerator = authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, resolveSort(sortBy, sortDirection));

        Page<Post> pagePost = this.postRepository.findActiveAndArchivedPosts(pageable);

        // Filter posts: Allow access only if the user is the owner or has privileged roles and map filtered posts to response DTOs
        List<PostResponseBody> filteredPosts = pagePost.getContent().stream()
                .filter(post -> authenticatedUser != null && post.getUser().getUserId().equals(authenticatedUser.getUserId()) || isAdminOrModerator)
                .map(PostMapper::postToPostResponseBody)
                .toList();

        return buildPageableResponse(pagePost, filteredPosts);
    }

    @Override
    @Transactional
    public PostResponseBody togglePostVisibility(Long postId, boolean isLive) {
        Post post = fetchPostById(postId);

        validateModificationAuthorization(post, "toggle visibility of");

        post.setLive(isLive);
        post.setPostLastUpdatedDate(Instant.now());

        Post updatedPost = this.postRepository.save(post);
        return PostMapper.postToPostResponseBody(updatedPost);
    }

    @Override
    @Transactional
    public PostResponseBody setAsMemberPost(Long postId) {
        User authenticatedUser = AuthUtil.getAuthenticatedUser();

        Post existingPost = fetchUnArchivedAndLivePostById(postId);

        // Ensure the post belongs to the authenticated user
        if (authenticatedUser != null && !existingPost.getUser().getUserId().equals(authenticatedUser.getUserId())) {
            throw new UnAuthorizedException("You can only mark your own posts as member posts.");
        }

        // Mark the existingPost as a member post
        existingPost.setMemberPost(true);
        existingPost.setPostLastUpdatedDate(Instant.now());

        Post updatedPost = this.postRepository.save(existingPost);
        return PostMapper.postToPostResponseBody(updatedPost);
    }
    
    @Override
    @Transactional
    public void deletePost(Long postId) {

        User authenticatedUser = AuthUtil.getAuthenticatedUser();

        if (authenticatedUser == null) {
            throw new UnAuthorizedException("You are not authorized to delete this post.");
        }

        Post existingPost = fetchPostById(postId);

        // Allow deletion if the user owns the post OR has privileged roles
        validateModificationAuthorization(existingPost, "delete");

        // Additional Check: Ensure the post is not already deleted (soft delete case)
        if (existingPost.isPostDeleted()) {
            throw new ResourceNotFoundException("Post", "postId", postId);
        }

        this.postRepository.delete(existingPost);

//        log.info("Post with ID {} deleted by user {}", postId, authenticatedUser.getUserId());

    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<PostResponseBody> getPostsByUser(Long userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        User existingUser = this.userRepository
                                .findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        Pageable pageable = PageRequest.of(pageNumber, pageSize, resolveSort(sortBy, sortDirection));

        Page<Post> pagePost = this.postRepository.findByUser(existingUser, pageable);

        List<PostResponseBody> postResponseBodyList = pagePost.getContent().stream()
                .map(post -> this.modelMapper.map(post, PostResponseBody.class))
                .toList();

        return buildPageableResponse(pagePost, postResponseBodyList);
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<PostResponseBody> getPostsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Category existingCategory = fetchCategoryById(categoryId);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, resolveSort(sortBy, sortDirection));

        Page<Post> pagePost = this.postRepository.findByCategory(existingCategory, pageable);

        List<PostResponseBody> postResponseBodyList = pagePost.stream()
                               .map((eachPost) -> this.modelMapper.map(eachPost, PostResponseBody.class))
                               .collect(Collectors.toList());

        return buildPageableResponse(pagePost, postResponseBodyList);
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<PostResponseBody> searchPostsByTitle(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, resolveSort(sortBy, sortDirection));

        Page<Post> pagePost = this.postRepository.searchKeyOnTitle("%" + keyword + "%", pageable);

        List<PostResponseBody> postResponseBodyList = pagePost.stream()
                .map((eachPost) -> this.modelMapper.map(eachPost, PostResponseBody.class))
                .collect(Collectors.toList());

        return buildPageableResponse(pagePost, postResponseBodyList);
    }

    @Override
    @Transactional
    public PostResponseBody deactivatePost(Long postId) {

        Post existingPost = fetchPostById(postId);

        validateModificationAuthorization(existingPost, "deactivate");

        Instant currentTimestamp = Instant.now();
        existingPost.setPostDeleted(true);
        existingPost.setPostDeletionTimestamp(currentTimestamp);
        existingPost.getComments().forEach(comment -> {
            comment.setCommentDeleted(true);
            comment.setCommentLastUpdatedDate(Instant.now());
            comment.setCommentDeletionTimestamp(currentTimestamp);
        });
        existingPost.setPostLastUpdatedDate(Instant.now());

        Post updatedPost = this.postRepository.save(existingPost);
        return PostMapper.postToPostResponseBody(updatedPost);
    }

    @Override
    @Transactional
    public PostResponseBody activatePost(Long postId) {

        Post existingPost = fetchPostById(postId);

        validateModificationAuthorization(existingPost, "activate");
        
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        if (existingPost.isPostDeleted() && existingPost.getPostDeletionTimestamp().isAfter(cutoff)) {
            existingPost.setPostDeleted(false);
            existingPost.setPostDeletionTimestamp(null);
            existingPost.getComments().forEach(comment -> {
                if (comment.isCommentDeleted() 
                    && comment.getCommentDeletionTimestamp() != null 
                    && comment.getCommentDeletionTimestamp().isAfter(cutoff)) {
                        comment.setCommentDeleted(false);
                        comment.setCommentLastUpdatedDate(Instant.now());
                        comment.setCommentDeletionTimestamp(null);
                }
            });
        }
        existingPost.setPostLastUpdatedDate(Instant.now());

        Post updatedPost = this.postRepository.save(existingPost);
        return PostMapper.postToPostResponseBody(updatedPost);
    }

    // ===================================== PRIVATE HELPERS =====================================

    private Category fetchCategoryById(Long categoryId) {
        return this.categoryRepository
                   .findById(categoryId)
                   .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    private Post fetchPostById(Long postId) {
        return this.postRepository
                   .findById(postId)
                   .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
    }

    private Post fetchUnArchivedAndLivePostById(Long postId) {
        return this.postRepository
                   .findActiveAndUnarchivedPostsById(postId)
                   .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
    }



    private List<CommentResponseBody> fetchTopComments(Post post) {
        return this.commentRepository.findTop2ByPost_PostIdOrderByCommentCreatedDateDesc(post.getPostId()).stream()
                .map(comment -> this.modelMapper.map(comment, CommentResponseBody.class))
                .collect(Collectors.toList());
    }

    private Sort resolveSort(String sortBy, String sortDirection) {
        return sortDirection.equalsIgnoreCase(AppConstants.SORT_DIR)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
    }

    private Set<Tag> fetchOrCreateTags(Set<String> tagNames) {
        return tagNames.stream()
                .map(tagName -> this.tagRepository.findByTagName(tagName)
                        .orElseGet(() -> this.tagRepository.save(new Tag(tagName))))
                .collect(Collectors.toSet());
    }

    private void validateModificationAuthorization(Post post, String action) {
        User authenticatedUser = AuthUtil.getAuthenticatedUser();
        boolean isAdminOrModerator = authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR");

        if (authenticatedUser == null || (!post.getUser().getUserId().equals(authenticatedUser.getUserId()) && !isAdminOrModerator)) {
            throw new UnAuthorizedException("You do not have permission to %s this post.", action);
        }
    }

    private void updatePostFields(Post post, PostRequestBody postRequestBody) {
        post.setPostTitle(postRequestBody.getTitle());
        post.setPostContent(postRequestBody.getContent());
        post.setImageUrl(postRequestBody.getImageUrl());
        post.setVideoUrl(postRequestBody.getVideoUrl());

        if (postRequestBody.getCategoryId() != null) {
            Category category = fetchCategoryById(postRequestBody.getCategoryId());
            post.setCategory(category);
        }

        if (postRequestBody.getTagNames() != null) {
            Set<Tag> tags = fetchOrCreateTags(postRequestBody.getTagNames());
            post.setTags(tags);
        }
    }

    private void patchPostFields(Post post, PostRequestBody postRequestBody) {
        if (postRequestBody.getTitle() != null) post.setPostTitle(postRequestBody.getTitle());
        if (postRequestBody.getContent() != null) post.setPostContent(postRequestBody.getContent());
        if (postRequestBody.getImageUrl() != null) post.setImageUrl(postRequestBody.getImageUrl());
        if (postRequestBody.getVideoUrl() != null) post.setVideoUrl(postRequestBody.getVideoUrl());

        if (postRequestBody.getCategoryId() != null) {
            Category category = fetchCategoryById(postRequestBody.getCategoryId());
            post.setCategory(category);
        }

        if (postRequestBody.getTagNames() != null) {
            Set<Tag> tags = fetchOrCreateTags(postRequestBody.getTagNames());
            post.setTags(tags);
        }
    }

    private <T> PageableResponse<T> buildPageableResponse(Page<?> page, List<T> content) {
        return new PageableResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private void handleMediaUpload(Post post, MultipartFile image, MultipartFile video) throws IOException {
        if (image != null && !image.isEmpty()) {
            String imageUrl = this.fileService.uploadImage(postBucketPath + "/images_file/", image);
            post.setImageUrl(imageUrl);
        }
        if (video != null && !video.isEmpty()) {
            String videoUrl = this.fileService.uploadVideo(postBucketPath + "/videos_file/", video);
            post.setVideoUrl(videoUrl);
        }
    }

}
