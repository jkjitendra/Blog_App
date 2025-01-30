package com.jk.blog.service.impl;

import com.jk.blog.dto.comment.CommentRequestBody;
import com.jk.blog.dto.comment.CommentResponseBody;
import com.jk.blog.entity.Comment;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.CommentService;
import com.jk.blog.utils.AuthUtil;
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

    @Override
    @Transactional
    public CommentResponseBody createComment(CommentRequestBody commentRequestBody, Long postId) {

        User user = AuthUtil.getAuthenticatedUser();
        validateAuthenticatedUser(user);

        Post post = fetchPostById(postId);

        // 🔹 If the post is a "member-only" post, ensure the user is a subscriber
        validatePostAccessibility(post, user);

        Comment comment = new Comment();
        comment.setCommentDesc(commentRequestBody.getCommentDesc());
        comment.setUser(user);
        comment.setPost(post);
        comment.setMemberComment(determineMemberComment(user));

        comment.setCommentCreatedDate(Instant.now());

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
        Comment comment = fetchCommentById(commentId);
        return this.modelMapper.map(comment, CommentResponseBody.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCommentUserId(Long commentId) {
        return fetchCommentById(commentId).getUser().getUserId();
    }

    @Override
    @Transactional
    public CommentResponseBody updateComment(CommentRequestBody commentRequestBody, Long commentId) {
        Comment existingComment = fetchCommentById(commentId);

        validateOwnership(existingComment.getUser().getUserId(), "update");

        existingComment.setCommentDesc(commentRequestBody.getCommentDesc());
        existingComment.setCommentLastUpdatedDate(Instant.now());

        Comment updatedComment = this.commentRepository.save(existingComment);

        CommentResponseBody commentResponseBody = this.modelMapper.map(updatedComment, CommentResponseBody.class);
        commentResponseBody.setCommentLastUpdatedDate(DateTimeUtil.formatInstantToIsoString(updatedComment.getCommentLastUpdatedDate()));
        return commentResponseBody;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = fetchCommentById(commentId);

        validateOwnership(comment.getUser().getUserId(), "delete");

        this.commentRepository.delete(comment);
    }

    @Override
    public void deleteMultipleComments(Long postId, List<Long> commentIds) {

        commentIds.forEach(commentId -> {
            Comment comment = fetchCommentById(commentId);

            if (!comment.getPost().getPostId().equals(postId)) {
                throw new UnAuthorizedException("Comment does not belong to the specified post.");
            }

            validateOwnershipOrRoleBasedAccess(comment);
            commentRepository.delete(comment);
        });

    }

    @Override
    public boolean canDeleteComment(Long userId, Long commentId) {
        Comment comment = fetchCommentById(commentId);
        User authenticatedUser = AuthUtil.getAuthenticatedUser();

        return isAdmin(authenticatedUser)
                || isModerator(authenticatedUser, comment.getPost())
                || isPostOwner(authenticatedUser, comment.getPost())
                || isCommentOwner(authenticatedUser, comment);
    }

    @Override
    public boolean canBulkDelete(Long userId, Long postId) {
        Post post = fetchPostById(postId);
        User authenticatedUser = AuthUtil.getAuthenticatedUser();

        return isAdmin(authenticatedUser)
                || isModerator(authenticatedUser, post)
                || isPostOwner(authenticatedUser, post);
    }

    @Override
    @Transactional
    public void deactivateComment(Long commentId) {
        Comment comment = fetchCommentById(commentId);

        validateOwnership(comment.getUser().getUserId(), "deactivate");

        comment.setCommentDeleted(true);
        comment.setCommentDeletionTimestamp(Instant.now());
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void activateComment(Long commentId) {
        Comment comment = fetchCommentById(commentId);

        validateOwnership(comment.getUser().getUserId(), "activate");

        if (comment.isCommentDeleted() && comment.getCommentDeletionTimestamp().isAfter(Instant.now().minus(90, ChronoUnit.DAYS))) {
            comment.setCommentDeleted(false);
            comment.setCommentDeletionTimestamp(null);
            commentRepository.save(comment);
        } else {
            throw new UnAuthorizedException("Comment cannot be activated as it is permanently deleted or outside the activation window.");
        }
    }

    // Utility Methods
    private Comment fetchCommentById(Long commentId) {
        return this.commentRepository
                .findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));
    }

    private Post fetchPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
    }

    private void validateAuthenticatedUser(User user) {
        if (user == null) {
            throw new UnAuthorizedException("User must be logged in to perform this action.");
        }
    }

    private void validatePostAccessibility(Post post, User user) {
        if (post.isMemberPost() && !determineMemberComment(user)) {
            throw new UnAuthorizedException("You are not authorized to comment on this post.");
        }
    }

    private void validateOwnership(Long userId, String action) {
        User authenticatedUser = AuthUtil.getAuthenticatedUser();
        if (authenticatedUser == null || !authenticatedUser.getUserId().equals(userId)) {
            throw new UnAuthorizedException("You do not have permission to " + action + " this comment.");
        }
    }

    private void validateOwnershipOrRoleBasedAccess(Comment comment) {
        User authenticatedUser = AuthUtil.getAuthenticatedUser();
        boolean isOwner = authenticatedUser != null && comment.getUser().getUserId().equals(authenticatedUser.getUserId());
        boolean isAdminOrModerator = isAdmin(authenticatedUser) || isModerator(authenticatedUser, comment.getPost());

        if (!isOwner && !isAdminOrModerator) {
            throw new UnAuthorizedException("You do not have permission to delete this comment.");
        }
    }

    private boolean isAdmin(User user) {
        return AuthUtil.userHasRole(user, "ROLE_ADMIN");
    }

    private boolean isModerator(User user, Post post) {
        return AuthUtil.userHasRole(user, "ROLE_MODERATOR") && post.getUser().getUserId().equals(user.getUserId());
    }

    private boolean isPostOwner(User user, Post post) {
        return user != null && post.getUser().getUserId().equals(user.getUserId());
    }

    private boolean isCommentOwner(User user, Comment comment) {
        return user != null && comment.getUser().getUserId().equals(user.getUserId());
    }

    private boolean determineMemberComment(User user) {
        return isAdmin(user) || AuthUtil.userHasRole(user, "ROLE_SUBSCRIBER") || AuthUtil.userHasRole(user, "ROLE_MODERATOR");
    }
}
