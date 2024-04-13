package com.jk.blog.service.scheduler;

import com.jk.blog.entity.Comment;
import com.jk.blog.entity.Post;
import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PostMaintenanceScheduler {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Scheduled(cron = "0 0 1 * * *") // Run daily at 1 AM
    public void permanentlyDeleteMarkedItems() {
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        // For posts
        List<Post> postsToDelete = postRepository.findPostsEligibleForPermanentDeletion(cutoff);
        postRepository.deleteAll(postsToDelete);

        // For comments
        List<Comment> commentsToDelete = commentRepository.findCommentsEligibleForPermanentDeletion(cutoff);
        commentRepository.deleteAll(commentsToDelete);
    }
}
