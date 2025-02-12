package com.jk.blog.service.scheduler;

import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PostMaintenanceScheduler {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Scheduled(cron = "0 0 1 * * *") // Run daily at 1 AM
    @Transactional
    @Modifying
    public void permanentlyDeleteMarkedItems() {
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        // Batch delete instead of fetching into memory
        postRepository.deletePostsEligibleForPermanentDeletion(cutoff);
        commentRepository.deleteCommentsEligibleForPermanentDeletion(cutoff);
    }
}
