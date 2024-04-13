package com.jk.blog.service.impl;

import com.jk.blog.dto.CategoryDTO;
import com.jk.blog.dto.CommentRequestBody;
import com.jk.blog.dto.CommentResponseBody;
import com.jk.blog.dto.ProfileResponseBody;
import com.jk.blog.entity.Comment;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.CommentService;
import com.jk.blog.utils.DateTimeUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ModelMapper modelMapper;

//    @Override
//    @Transactional
//    public CommentResponseBody createComment(CommentRequestBody commentRequestBody, Long postId) {
//        Post post = this.postRepository
//                        .findById(postId)
//                        .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
//        User user = this.userRepository
//                        .findById(commentRequestBody.getUserId())
//                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", commentRequestBody.getUserId()));
//
//        Comment comment = new Comment();
//        comment.setCommentDesc(commentRequestBody.getCommentDesc());
//        comment.setUser(user);
////        post.addComment(comment); // Assuming addComment handles setting both sides of the relationship
//        this.postRepository.save(post);
//
//        return this.modelMapper.map(comment, CommentResponseBody.class);
//    }

    @Override
    @Transactional
    public CommentResponseBody createComment(CommentRequestBody commentRequestBody, Long postId) {
        Post post = this.postRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        User user = this.userRepository
                .findById(commentRequestBody.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", commentRequestBody.getUserId()));

        Comment comment = new Comment();
        comment.setCommentDesc(commentRequestBody.getCommentDesc());
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedDate(Instant.now());

        Comment savedComment = this.commentRepository.save(comment);
        return this.modelMapper.map(savedComment, CommentResponseBody.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseBody> getAllComments(Long postId) {
        List<Comment> commentList = this.commentRepository
                                        .findByPost_PostId(postId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Comment", "postId", postId));
        return commentList.stream()
                          .map((eachComment) -> this.modelMapper.map(eachComment, CommentResponseBody.class))
                          .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseBody getCommentById(Long commentId) {
        Comment comment = this.commentRepository
                              .findById(commentId)
                              .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));
        return this.modelMapper.map(comment, CommentResponseBody.class);
    }

    @Override
    @Transactional
    public CommentResponseBody updateComment(CommentRequestBody commentRequestBody, Long commentId) {
        Comment existingComment = this.commentRepository
                                      .findById(commentId)
                                      .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));

        existingComment.setCommentDesc(commentRequestBody.getCommentDesc());
        existingComment.setLastUpdatedDate(Instant.now());
        Comment updatedComment = this.commentRepository.save(existingComment);
        CommentResponseBody commentResponseBody = this.modelMapper.map(updatedComment, CommentResponseBody.class);
        commentResponseBody.setLastUpdatedDate(DateTimeUtil.formatInstantToIsoString(updatedComment.getLastUpdatedDate()));
        return commentResponseBody;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = this.commentRepository
                              .findById(commentId)
                              .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));
        this.commentRepository.delete(comment);
    }


    @Override
    @Transactional
    public void deactivateComment(Long commentId) {
        Comment comment = this.commentRepository
                .findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));
        comment.setCommentDeleted(true);
        comment.setCommentDeletionTimestamp(Instant.now());
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void activateComment(Long commentId) {
        Comment comment = this.commentRepository
                .findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));
        if (comment.isCommentDeleted() && comment.getCommentDeletionTimestamp().isAfter(Instant.now().minus(90, ChronoUnit.DAYS))) {
            comment.setCommentDeleted(false);
            comment.setCommentDeletionTimestamp(null);
            commentRepository.save(comment);
        }
    }
}
