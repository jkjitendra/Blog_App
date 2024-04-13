package com.jk.blog.service.scheduler;

import com.jk.blog.entity.Comment;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class DataCleanupService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Transactional
    public void cleanupDeactivatedUserAccounts() {
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        List<User> deactivatedUsers = this.userRepository.findDeactivatedUsersBefore(cutoff);

        for (User user : deactivatedUsers) {
            markPostsAndCommentsForDeactivatedUserAsDeactivated(user);
        }
    }

    private void markPostsAndCommentsForDeactivatedUserAsDeactivated(User user) {
        Instant now = Instant.now();

        // Mark posts of user as deleted
        List<Post> posts = this.postRepository.findByUser(user);
        for (Post post : posts) {
            post.setPostDeleted(true);
            post.setPostDeletionTimestamp(now);
        }
        this.postRepository.saveAll(posts);

        // Mark comments of user as deleted
        List<Comment> commentsList = this.commentRepository.findByUserId(user.getUserId());
        for (Comment comment : commentsList) {
            comment.setCommentDeleted(true);
            comment.setCommentDeletionTimestamp(now);
        }
        this.commentRepository.saveAll(commentsList);
    }
}
