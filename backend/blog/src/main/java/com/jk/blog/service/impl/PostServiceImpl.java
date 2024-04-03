package com.jk.blog.service.impl;

import com.jk.blog.constants.AppConstants;
import com.jk.blog.dto.*;
import com.jk.blog.entity.*;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.*;
import com.jk.blog.service.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.modelmapper.TypeMap;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        post.setUser(user);
        post.setCategory(category);

        if (postRequestBody.getTagNames() != null && !postRequestBody.getTagNames().isEmpty()) {
            Set<Tag> tags = postRequestBody.getTagNames().stream()
                                           .map(tagName -> this.tagRepository.findByTagName(tagName)
                                           .orElseGet(() -> this.tagRepository.save(new Tag(tagName))))
                                           .collect(Collectors.toSet());
            post.setTags(tags);
        }
/*
        Optional if using @PrePersist and @PreUpdate
        post.setCreatedDate(new Date());
        post.setLastUpdatedDate(new Date());
*/
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
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        PostResponseBody responseBody = this.modelMapper.map(post, PostResponseBody.class);


        List<CommentResponseBody> commentResponses = post.getComments().stream()
                                                         .map(comment -> this.modelMapper.map(comment, CommentResponseBody.class))
                                                         .collect(Collectors.toList());
        responseBody.setComments(commentResponses);
        responseBody.setIsLive(post.isLive());
        responseBody.setLastUpdatedDate(post.getLastUpdatedDate());
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
        Page<Post> pagePost = this.postRepository.findAll(pageable);
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
            postResponse.setLastUpdatedDate(post.getLastUpdatedDate());
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

        existingPost.setPostTitle(postRequestBody.getTitle());
        existingPost.setPostContent(postRequestBody.getContent());
        if (postRequestBody.getImageUrl() != null) {
            existingPost.setImageUrl(postRequestBody.getImageUrl());
        }
        if (postRequestBody.getVideoUrl() != null) {
            existingPost.setVideoUrl(postRequestBody.getVideoUrl());
        }
//        existingPost.setLive(true);
        existingPost.setCategory(category);
        Post updatedPost = this.postRepository.save(existingPost);
        PostResponseBody postResponseBody = this.modelMapper.map(updatedPost, PostResponseBody.class);

        postResponseBody.setIsLive(updatedPost.isLive());
        postResponseBody.setLastUpdatedDate(updatedPost.getLastUpdatedDate());

        Set<String> tagNames = updatedPost.getTags().stream()
                .map(Tag::getTagName)
                .collect(Collectors.toSet());
        postResponseBody.setTagNames(tagNames);

        return postResponseBody;
    }

    @Override
    @Transactional
    public PostResponseBody patchPost(Map<String, Object> updates, Long postId) {
        Post post = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Post.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, post, value);
            }
        });

        Post updatedPost = this.postRepository.save(post);
        return this.modelMapper.map(updatedPost, PostResponseBody.class);
    }

    @Override
    @Transactional
    public void togglePostVisibility(Long postId, boolean isLive) {
        Post post = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        post.setLive(isLive);
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
}
