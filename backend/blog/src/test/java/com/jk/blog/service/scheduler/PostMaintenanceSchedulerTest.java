package com.jk.blog.service.scheduler;

import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionSystemException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostMaintenanceSchedulerTest {

    @InjectMocks
    private PostMaintenanceScheduler postMaintenanceScheduler;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_permanentlyDeleteMarkedItems_SuccessfulDeletion() {
        // Run the method
        postMaintenanceScheduler.permanentlyDeleteMarkedItems();

        // Verify that deletion queries are called with any Instant argument
        verify(postRepository, times(1)).deletePostsEligibleForPermanentDeletion(any(Instant.class));
        verify(commentRepository, times(1)).deleteCommentsEligibleForPermanentDeletion(any(Instant.class));
    }

    @Test
    void test_permanentlyDeleteMarkedItems_NoEligiblePostsOrComments() {
        // Run the method
        postMaintenanceScheduler.permanentlyDeleteMarkedItems();

        // Verify delete methods were still called even if there were no eligible posts/comments
        verify(postRepository, times(1)).deletePostsEligibleForPermanentDeletion(any(Instant.class));
        verify(commentRepository, times(1)).deleteCommentsEligibleForPermanentDeletion(any(Instant.class));
    }

    @Test
    void test_permanentlyDeleteMarkedItems_WhenPostRepositoryThrowsException() {
        doThrow(new DataAccessException("Database Error") {}).when(postRepository)
                .deletePostsEligibleForPermanentDeletion(any(Instant.class));

        assertThrows(DataAccessException.class, () -> postMaintenanceScheduler.permanentlyDeleteMarkedItems());

        verify(postRepository, times(1)).deletePostsEligibleForPermanentDeletion(any(Instant.class));
        verify(commentRepository, never()).deleteCommentsEligibleForPermanentDeletion(any());  // Ensure comment deletion is skipped
    }

    @Test
    void test_permanentlyDeleteMarkedItems_WhenCommentRepositoryThrowsException() {
        doThrow(new TransactionSystemException("Transaction Error")).when(commentRepository)
                .deleteCommentsEligibleForPermanentDeletion(any(Instant.class));

        assertThrows(TransactionSystemException.class, () -> postMaintenanceScheduler.permanentlyDeleteMarkedItems());

        verify(postRepository, times(1)).deletePostsEligibleForPermanentDeletion(any(Instant.class));
        verify(commentRepository, times(1)).deleteCommentsEligibleForPermanentDeletion(any(Instant.class));
    }
}