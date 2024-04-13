package com.jk.blog.service.impl;

import com.jk.blog.constants.AppConstants;
import com.jk.blog.dto.*;
import com.jk.blog.entity.*;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.*;
import com.jk.blog.service.PostService;
import com.jk.blog.utils.DateTimeUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        Category category = this.categoryRepository
                                .findById(postRequestBody.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", postRequestBody.getCategoryId()));

        Post post = new Post();
        post.setPostTitle(postRequestBody.getTitle());
        post.setPostContent(postRequestBody.getContent());
        post.setImageUrl(postRequestBody.getImageUrl());
        post.setVideoUrl(postRequestBody.getVideoUrl());
        post.setLive(true);
        post.setCreatedDate(Instant.now());
        post.setUser(user);
        post.setCategory(category);

        if (postRequestBody.getTagNames() != null && !postRequestBody.getTagNames().isEmpty()) {
            Set<Tag> tags = postRequestBody.getTagNames().stream()
                                           .map(tagName -> this.tagRepository.findByTagName(tagName)
                                           .orElseGet(() -> this.tagRepository.save(new Tag(tagName))))
                                           .collect(Collectors.toSet());
            post.setTags(tags);
        }

        Post savedPost = this.postRepository.save(post);
        PostResponseBody postResponseBody = this.modelMapper.map(savedPost, PostResponseBody.class);

        postResponseBody.setIsLive(savedPost.isLive());

        Set<String> tagNames = savedPost.getTags().stream()
                .map(Tag::getTagName)
                .collect(Collectors.toSet());
        postResponseBody.setTagNames(tagNames);

        return postResponseBody;
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseBody getPostById(Long postId) {
        Post post = this.postRepository
                        .findByPostIdAndIsLiveTrueAndIsPostDeletedFalse(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        PostResponseBody responseBody = this.modelMapper.map(post, PostResponseBody.class);


        List<CommentResponseBody> commentResponses = post.getComments().stream()
                                                         .map(comment -> this.modelMapper.map(comment, CommentResponseBody.class))
                                                         .collect(Collectors.toList());
        responseBody.setComments(commentResponses);
        responseBody.setIsLive(post.isLive());
        responseBody.setCreatedDate(DateTimeUtil.formatInstantToIsoString(post.getCreatedDate()));
        responseBody.setLastUpdatedDate(DateTimeUtil.formatInstantToIsoString(post.getLastUpdatedDate()));
        // Convert tag names to a set of strings
        Set<String> tagNames = post.getTags().stream()
                .map(Tag::getTagName)
                .collect(Collectors.toSet());
        responseBody.setTagNames(tagNames);

        return responseBody;
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<PostResponseBody> getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(AppConstants.SORT_DIR) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Post> pagePost = this.postRepository.findByIsLiveTrueAndIsPostDeletedFalse(pageable);
        List<Post> postList = pagePost.getContent();
        List<PostResponseBody> postResponseBodyList = postList.stream().map(post -> {
            PostResponseBody postResponse = modelMapper.map(post, PostResponseBody.class);

            // Fetch a limited number of comments for each post, for example, the two most recent comments
            List<Comment> comments = this.commentRepository.findTop2ByPost_PostIdOrderByCreatedDateDesc(post.getPostId());
            List<CommentResponseBody> commentResponses = comments.stream()
                    .map(comment -> modelMapper.map(comment, CommentResponseBody.class))
                    .collect(Collectors.toList());
            postResponse.setComments(commentResponses);
            postResponse.setIsLive(post.isLive());
            postResponse.setCreatedDate(DateTimeUtil.formatInstantToIsoString(post.getCreatedDate()));
            postResponse.setLastUpdatedDate(DateTimeUtil.formatInstantToIsoString(post.getLastUpdatedDate()));
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
        existingPost.setLastUpdatedDate(Instant.now());
        existingPost.setCategory(category);
        Post updatedPost = this.postRepository.save(existingPost);

        PostResponseBody postResponseBody = this.modelMapper.map(updatedPost, PostResponseBody.class);
        postResponseBody.setComments(new ArrayList<>());
        postResponseBody.setIsLive(updatedPost.isLive());
        postResponseBody.setCreatedDate(DateTimeUtil.formatInstantToIsoString(updatedPost.getCreatedDate()));
        postResponseBody.setLastUpdatedDate(DateTimeUtil.formatInstantToIsoString(updatedPost.getLastUpdatedDate()));

        Set<String> tagNames = updatedPost.getTags().stream()
                .map(Tag::getTagName)
                .collect(Collectors.toSet());
        postResponseBody.setTagNames(tagNames);

        return postResponseBody;
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
        existingPost.setLastUpdatedDate(Instant.now());
        Post updatedPost = this.postRepository.save(existingPost);

        PostResponseBody patchedPost = this.modelMapper.map(updatedPost, PostResponseBody.class);
        patchedPost.setPostId(updatedPost.getPostId());
        patchedPost.setComments(new ArrayList<>());
        patchedPost.setIsLive(updatedPost.isLive());
        patchedPost.setCreatedDate(DateTimeUtil.formatInstantToIsoString(updatedPost.getCreatedDate()));
        patchedPost.setLastUpdatedDate(DateTimeUtil.formatInstantToIsoString(updatedPost.getLastUpdatedDate()));

        Set<String> tagNames = updatedPost.getTags().stream()
                .map(Tag::getTagName)
                .collect(Collectors.toSet());
        patchedPost.setTagNames(tagNames);
        return patchedPost;
    }

    @Override
    @Transactional
    public void togglePostVisibility(Long postId, boolean isLive) {
        Post post = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        post.setLive(isLive);
        post.setLastUpdatedDate(Instant.now());
        this.postRepository.save(post);
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
    public void deactivatePost(Long postId) {
        Post post = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        Instant currentTimestamp = Instant.now();
        post.setPostDeleted(true);
        post.setPostDeletionTimestamp(currentTimestamp);
        post.getComments().forEach(comment -> {
            comment.setCommentDeleted(true);
            comment.setCommentDeletionTimestamp(currentTimestamp);
        });

        this.postRepository.save(post);
    }

    @Override
    @Transactional
    public void activatePost(Long postId) {
        Post post = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        if (post.isPostDeleted() && post.getPostDeletionTimestamp().isAfter(cutoff)) {
            post.setPostDeleted(false);
            post.setPostDeletionTimestamp(null);
            post.getComments().forEach(comment -> {
                if (comment.isCommentDeleted() && comment.getCommentDeletionTimestamp() != null && comment.getCommentDeletionTimestamp().isAfter(cutoff)) {
                    comment.setCommentDeleted(false);
                    comment.setCommentDeletionTimestamp(null);
                }
            });
            this.postRepository.save(post);
        }
    }
}
