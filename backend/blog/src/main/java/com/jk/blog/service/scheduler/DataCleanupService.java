package com.jk.blog.service.scheduler;

import com.jk.blog.entity.User;
import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataCleanupService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @PersistenceContext
    private EntityManager entityManager; // Injecting EntityManager for batch processing

    @Transactional
    public void cleanupDeactivatedUserAccounts() {
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        // Fetch all deactivated users
        List<Long> deactivatedUserIds = userRepository.findDeactivatedUsersBefore(cutoff)
                .stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        if (deactivatedUserIds.isEmpty()) {
            return; // No deactivated users found, exit
        }

        markPostsAndCommentsForDeactivatedUsers(deactivatedUserIds);
    }

    private void markPostsAndCommentsForDeactivatedUsers(List<Long> userIds) {
        Instant now = Instant.now();

        // ✅ Bulk update posts instead of fetching them
        Query postUpdateQuery = entityManager.createQuery(
                "UPDATE Post p SET p.postDeleted = true, p.postDeletionTimestamp = :now WHERE p.user.userId IN :userIds"
        );
        postUpdateQuery.setParameter("now", now);
        postUpdateQuery.setParameter("userIds", userIds);
        int updatedPosts = postUpdateQuery.executeUpdate();
        System.out.println("Updated Posts Count: " + updatedPosts);

        // ✅ Bulk update comments instead of fetching them
        Query commentUpdateQuery = entityManager.createQuery(
                "UPDATE Comment c SET c.commentDeleted = true, c.commentDeletionTimestamp = :now WHERE c.user.userId IN :userIds"
        );
        commentUpdateQuery.setParameter("now", now);
        commentUpdateQuery.setParameter("userIds", userIds);
        int updatedComments = commentUpdateQuery.executeUpdate();
        System.out.println("Updated Comments Count: " + updatedComments);
    }
}
