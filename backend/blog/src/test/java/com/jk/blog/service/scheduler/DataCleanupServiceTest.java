package com.jk.blog.service.scheduler;

import com.jk.blog.entity.Comment;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataCleanupServiceTest {

    @InjectMocks
    private DataCleanupService dataCleanupService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query postUpdateQuery;

    @Mock
    private Query commentUpdateQuery;

    private User user;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(1L);
        user.setUserName("user1");

        lenient().when(entityManager.createQuery(anyString())).thenReturn(postUpdateQuery, commentUpdateQuery);
        lenient().when(postUpdateQuery.setParameter(anyString(), any())).thenReturn(postUpdateQuery);
        lenient().when(commentUpdateQuery.setParameter(anyString(), any())).thenReturn(commentUpdateQuery);
        lenient().when(postUpdateQuery.executeUpdate()).thenReturn(1);
        lenient().when(commentUpdateQuery.executeUpdate()).thenReturn(1);
    }


    @Test
    void test_cleanupDeactivatedUserAccounts_WhenDeactivatedUsersExist_ReturnPostsAndCommentsUpdated() {

        when(userRepository.findDeactivatedUsersBefore(any())).thenReturn(List.of(user));
        when(entityManager.createQuery(anyString())).thenReturn(postUpdateQuery, commentUpdateQuery);
        when(postUpdateQuery.setParameter(anyString(), any())).thenReturn(postUpdateQuery);
        when(commentUpdateQuery.setParameter(anyString(), any())).thenReturn(commentUpdateQuery);
        when(postUpdateQuery.executeUpdate()).thenReturn(1);
        when(commentUpdateQuery.executeUpdate()).thenReturn(1);

        dataCleanupService.cleanupDeactivatedUserAccounts();

        verify(userRepository, times(1)).findDeactivatedUsersBefore(any());
        verify(entityManager, times(2)).createQuery(anyString());
        verify(postUpdateQuery, times(1)).executeUpdate();
        verify(commentUpdateQuery, times(1)).executeUpdate();
    }

    @Test
    void test_cleanupDeactivatedUserAccounts_WhenNoDeactivatedUsers_ReturnNoActionTaken() {
        when(userRepository.findDeactivatedUsersBefore(any())).thenReturn(List.of());

        dataCleanupService.cleanupDeactivatedUserAccounts();

        verify(userRepository, times(1)).findDeactivatedUsersBefore(any());
        verifyNoInteractions(entityManager);
    }

    @Test
    void test_cleanupDeactivatedUserAccounts_WhenEntityManagerThrowsException_ReturnHandledGracefully() {
        when(userRepository.findDeactivatedUsersBefore(any())).thenReturn(List.of(user));
        when(entityManager.createQuery(anyString())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> dataCleanupService.cleanupDeactivatedUserAccounts());

        verify(userRepository, times(1)).findDeactivatedUsersBefore(any());
        verify(entityManager, times(1)).createQuery(anyString());
    }

}