package com.jk.blog.service.impl;

import com.jk.blog.dto.PostDTO;
import com.jk.blog.entity.Category;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.CategoryRepository;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
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
    private ModelMapper modelMapper;

    @Override
    public PostDTO createPost(PostDTO postDTO) {

        User user = userRepository
                    .findById(postDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "userId", postDTO.getUserId()));
        Category category = categoryRepository.findById(postDTO.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", postDTO.getCategoryId()));
        Post post = this.modelMapper.map(postDTO, Post.class);

        post.setUser(user);
        post.setCategory(category);
/*
        Optional if using @PrePersist and @PreUpdate
        post.setCreatedDate(new Date());
        post.setLastUpdatedDate(new Date());
*/
        Post savedPost = this.postRepository.save(post);
        return this.modelMapper.map(savedPost, PostDTO.class);
    }

    @Override
    public PostDTO getPostById(Long postId) {
        Post post = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        return this.modelMapper.map(post, PostDTO.class);

    }

    @Override
    public List<PostDTO> getAllPost() {
        List<Post> postList = this.postRepository.findAll();
        return postList.stream()
                       .map((eachPost) -> this.modelMapper.map(eachPost, PostDTO.class))
                       .collect(Collectors.toList());
    }

    @Override
    public PostDTO updatePost(PostDTO postDTO, Long postId) {
        Post existingPost = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
    /*
         post.setPostTitle(postDTO.getTitle());
         post.setPostContent(postDTO.getContent());
         post.setImageName(postDTO.getImageName());
         post.setVideoName(postDTO.getVideoName());
         post.setLive(postDTO.getIsLive());
    */
        // above is not needed if used below 82 to 85
        this.modelMapper.typeMap(PostDTO.class, Post.class).addMappings(mapper -> {
            mapper.skip(Post::setUser);
        });
        this.modelMapper.map(postDTO, existingPost);

        Post updatedPost = this.postRepository.save(existingPost);
        return this.modelMapper.map(updatedPost, PostDTO.class);
    }

    @Override
    public void deletePost(Long postId) {
        Post existingPost = this.postRepository
                                .findById(postId)
                                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        this.postRepository.delete(existingPost);
    }

    @Override
    public List<PostDTO> getPostsByUser(Long userId) {
        User existingUser = this.userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        List<Post> existingPostList = this.postRepository.findByUser(existingUser);
        return existingPostList.stream()
                .map((eachPost) -> this.modelMapper.map(eachPost, PostDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPostsByCategory(Long categoryId) {
        Category existingCategory = this.categoryRepository
                                        .findById(categoryId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        List<Post> existingPostList = this.postRepository.findByCategory(existingCategory);
        return existingPostList.stream()
                               .map((eachPost) -> this.modelMapper.map(eachPost, PostDTO.class))
                               .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> searchPostsByTitle(String keyword) {
        List<Post> existingPostList = this.postRepository.findByTitleContainingIgnoreCase(keyword);
        return existingPostList.stream()
                .map((eachPost) -> this.modelMapper.map(eachPost, PostDTO.class))
                .collect(Collectors.toList());
    }
}
