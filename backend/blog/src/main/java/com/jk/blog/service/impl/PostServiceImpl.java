package com.jk.blog.service.impl;

import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.PostRequestBody;
import com.jk.blog.dto.PostResponseBody;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    public PostResponseBody createPost(PostRequestBody postRequestBody) {

        User user = userRepository
                    .findById(postRequestBody.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "userId", postRequestBody.getUserId()));
        Category category = categoryRepository.findById(postRequestBody.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", postRequestBody.getCategoryId()));
        Post post = this.modelMapper.map(postRequestBody, Post.class);

        post.setUser(user);
        post.setCategory(category);
/*
        Optional if using @PrePersist and @PreUpdate
        post.setCreatedDate(new Date());
        post.setLastUpdatedDate(new Date());
*/
        Post savedPost = this.postRepository.save(post);
        return this.modelMapper.map(savedPost, PostResponseBody.class);
    }

    @Override
    public PostResponseBody getPostById(Long postId) {
        Post post = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        return this.modelMapper.map(post, PostResponseBody.class);

    }

    @Override
    public PageableResponse<PostResponseBody> getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Post> pagePost = this.postRepository.findAll(pageable);
        List<Post> postList = pagePost.getContent();

        List<PostResponseBody> postResponseBodyList = postList.stream()
                                                       .map((eachPost) -> this.modelMapper.map(eachPost, PostResponseBody.class))
                                                       .collect(Collectors.toList());

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
    public PostResponseBody updatePost(PostRequestBody postRequestBody, Long postId) {
        Post existingPost = this.postRepository
                        .findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
    /*
         post.setPostTitle(postDTO.getTitle());
         post.setPostContent(postDTO.getContent());
         post.setImageUrl(postDTO.getImageUrl());
         post.setVideoUrl(postDTO.getVideoUrl());
         post.setLive(postDTO.getIsLive());
    */
        // above is not needed if used below 82 to 85
        this.modelMapper.typeMap(PostRequestBody.class, Post.class).addMappings(mapper -> {
            mapper.skip(Post::setUser);
        });
        this.modelMapper.map(postRequestBody, existingPost);

        Post updatedPost = this.postRepository.save(existingPost);
        return this.modelMapper.map(updatedPost, PostResponseBody.class);
    }

    @Override
    public void deletePost(Long postId) {
        Post existingPost = this.postRepository
                                .findById(postId)
                                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        this.postRepository.delete(existingPost);
    }

    @Override
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
    public List<PostResponseBody> searchPostsByTitle(String keyword) {
        List<Post> existingPostList = this.postRepository.findByTitleContainingIgnoreCase(keyword);
        return existingPostList.stream()
                .map((eachPost) -> this.modelMapper.map(eachPost, PostResponseBody.class))
                .collect(Collectors.toList());
    }
}
