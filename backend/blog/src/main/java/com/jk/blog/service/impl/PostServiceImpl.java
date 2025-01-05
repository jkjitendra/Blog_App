package com.jk.blog.service.impl;

import com.jk.blog.constants.AppConstants;
import com.jk.blog.dto.*;
import com.jk.blog.dto.comment.CommentResponseBody;
import com.jk.blog.dto.post.PostRequestBody;
import com.jk.blog.dto.post.PostResponseBody;
import com.jk.blog.entity.*;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.*;
import com.jk.blog.service.PostService;
import com.jk.blog.utils.AuthUtil;
import com.jk.blog.utils.DateTimeUtil;
import com.jk.blog.dto.post.PostMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

    @Override
    @Transactional
    public PostResponseBody createPost(Long userId, PostRequestBody postRequestBody) {

        // Get the authenticated user
        User user = AuthUtil.getAuthenticatedUser();
        if (user == null) {
            throw new UnAuthorizedException("User must be logged in to create a post.");
        }
        Category category = this.categoryRepository
                                .findById(postRequestBody.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", postRequestBody.getCategoryId()));

        Post post = PostMapper.postRequestBodyToPost(postRequestBody, user, category);

        // 🔹 Set member post status based on user role
        post.setMemberPost(AuthUtil.userHasRole(user, "SUBSCRIBER"));

        if (postRequestBody.getTagNames() != null && !postRequestBody.getTagNames().isEmpty()) {
            Set<Tag> tags = postRequestBody.getTagNames().stream()
                                           .map(tagName -> this.tagRepository.findByTagName(tagName)
                                           .orElseGet(() -> this.tagRepository.save(new Tag(tagName))))
                                           .collect(Collectors.toSet());
            post.setTags(tags);
        }

        post.setPostCreatedDate(Instant.now());
        Post savedPost = this.postRepository.save(post);
        PostResponseBody postResponseBody = this.modelMapper.map(savedPost, PostResponseBody.class);

        postResponseBody.setIsLive(savedPost.isLive());
        postResponseBody.setMemberPost(savedPost.isMemberPost());

        Set<String> tagNames = savedPost.getTags().stream()
                                        .map(Tag::getTagName)
                                        .collect(Collectors.toSet());
        postResponseBody.setTagNames(tagNames);

        return postResponseBody;
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseBody getPostById(Long postId) {
        Post existingPost = this.postRepository
                                .findByPostIdAndIsLiveTrueAndIsPostDeletedFalse(postId)
                                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

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

        Sort sort = sortDirection.equalsIgnoreCase(AppConstants.SORT_DIR) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Get authenticated user
        User user = AuthUtil.getAuthenticatedUser();

        Page<Post> pagePost;
        if (user != null && AuthUtil.userHasRole(user, "SUBSCRIBER")) {
            // If the user is a subscriber, fetch all posts
            pagePost = this.postRepository.findByIsLiveTrueAndIsPostDeletedFalse(pageable);
        } else {
            // If the user is not a subscriber, fetch only public posts
            pagePost = this.postRepository.findPublicPosts(pageable);
        }

        List<Post> postList = pagePost.getContent();
        List<PostResponseBody> postResponseBodyList = postList.stream().map(existingPost -> {
            PostResponseBody postResponse = PostMapper.postToPostResponseBody(existingPost);

            // Fetch a limited number of comments for each post, for example, the two most recent comments
            List<Comment> comments = this.commentRepository.findTop2ByPost_PostIdOrderByCommentCreatedDateDesc(existingPost.getPostId());
            List<CommentResponseBody> commentResponses = comments.stream()
                    .map(comment -> modelMapper.map(comment, CommentResponseBody.class))
                    .collect(Collectors.toList());
            postResponse.setComments(commentResponses);

            return postResponse;
        }).collect(Collectors.toList());

        PageableResponse<PostResponseBody> pageableResponse = new PageableResponse<>();
        pageableResponse.setContent(postResponseBodyList);
        pageableResponse.setPageNumber(pagePost.getNumber());
        pageableResponse.setSize(pagePost.getSize());
        pageableResponse.setTotalElements(pagePost.getTotalElements());
        pageableResponse.setTotalPages(pagePost.getTotalPages());
        pageableResponse.setLastPage(pagePost.isLast());
        return pageableResponse;
    }

    @Override
    @Transactional
    public PostResponseBody updatePost(PostRequestBody postRequestBody, Long postId) {

        Post existingPost = this.postRepository
                                .findById(postId)
                                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        Category category = this.categoryRepository
                                .findById(postRequestBody.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", postRequestBody.getCategoryId()));
        // Fetch or create new tags and update the association
        Set<Tag> updatedTags = postRequestBody.getTagNames().stream()
                                              .map(tagName -> tagRepository.findByTagName(tagName)
                                              .orElseGet(() -> tagRepository.save(new Tag(tagName))))
                                              .collect(Collectors.toSet());
        // Set the updated tags to the post
        existingPost.getTags().clear(); // Clear existing tags first
        existingPost.getTags().addAll(updatedTags); // Add all the updated tags

        existingPost.setPostTitle(postRequestBody.getTitle());
        existingPost.setPostContent(postRequestBody.getContent());
        existingPost.setImageUrl(postRequestBody.getImageUrl());
        existingPost.setVideoUrl(postRequestBody.getVideoUrl());
        existingPost.setPostLastUpdatedDate(Instant.now());
        existingPost.setCategory(category);
        Post updatedPost = this.postRepository.save(existingPost);

        return PostMapper.postToPostResponseBody(updatedPost);
    }

    @Override
    @Transactional
    public PostResponseBody patchPost(PostRequestBody postRequestBody, Long postId) {
        Post existingPost = this.postRepository
                                .findById(postId)
                                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        Category category = null;
        if (postRequestBody.getCategoryId() != null) {
            category = this.categoryRepository
                    .findById(postRequestBody.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", postRequestBody.getCategoryId()));

            existingPost.setCategory(category);
        }
        if (postRequestBody.getTagNames() != null && !postRequestBody.getTagNames().isEmpty()) {
            // Fetch or create new tags and update the association
            Set<Tag> updatedTags = postRequestBody.getTagNames().stream()
                    .map(tagName -> tagRepository.findByTagName(tagName)
                            .orElseGet(() -> tagRepository.save(new Tag(tagName))))
                    .collect(Collectors.toSet());
            // Set the updated tags to the post
            existingPost.getTags().clear(); // Clear existing tags first
            existingPost.getTags().addAll(updatedTags); // Add all the updated tags
        }
        if (postRequestBody.getTitle() != null && !postRequestBody.getTitle().isEmpty())
            existingPost.setPostTitle(postRequestBody.getTitle());
        if (postRequestBody.getContent() != null && !postRequestBody.getContent().isEmpty())
            existingPost.setPostContent(postRequestBody.getContent());
        if (postRequestBody.getImageUrl() != null)
            existingPost.setImageUrl(postRequestBody.getImageUrl());
        if (postRequestBody.getVideoUrl() != null)
            existingPost.setVideoUrl(postRequestBody.getVideoUrl());
        existingPost.setPostLastUpdatedDate(Instant.now());
        Post updatedPost = this.postRepository.save(existingPost);

        return PostMapper.postToPostResponseBody(updatedPost);
    }

    @Override
    @Transactional
    public PostResponseBody togglePostVisibility(Long postId, boolean isLive) {
        Post post = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        post.setLive(isLive);
        post.setPostLastUpdatedDate(Instant.now());
        Post updatedPost = this.postRepository.save(post);
        return PostMapper.postToPostResponseBody(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post existingPost = this.postRepository
                                .findById(postId)
                                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        this.postRepository.delete(existingPost);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseBody> getPostsByUser(Long userId) {
        User existingUser = this.userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        List<Post> existingPostList = this.postRepository.findByUser(existingUser);
        return existingPostList.stream()
                               .map((eachPost) -> this.modelMapper.map(eachPost, PostResponseBody.class))
                               .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseBody> getPostsByCategory(Long categoryId) {
        Category existingCategory = this.categoryRepository
                                        .findById(categoryId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        List<Post> existingPostList = this.postRepository.findByCategory(existingCategory);
        return existingPostList.stream()
                               .map((eachPost) -> this.modelMapper.map(eachPost, PostResponseBody.class))
                               .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseBody> searchPostsByTitle(String keyword) {
        List<Post> existingPostList = this.postRepository.searchKeyOnTitle("%" + keyword + "%");
        return existingPostList.stream()
                .map((eachPost) -> this.modelMapper.map(eachPost, PostResponseBody.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostResponseBody deactivatePost(Long postId) {
        Post existingPost = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

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
        Post existingPost = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        if (existingPost.isPostDeleted() && existingPost.getPostDeletionTimestamp().isAfter(cutoff)) {
            existingPost.setPostDeleted(false);
            existingPost.setPostDeletionTimestamp(null);
            existingPost.getComments().forEach(comment -> {
                if (comment.isCommentDeleted() && comment.getCommentDeletionTimestamp() != null && comment.getCommentDeletionTimestamp().isAfter(cutoff)) {
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
}
