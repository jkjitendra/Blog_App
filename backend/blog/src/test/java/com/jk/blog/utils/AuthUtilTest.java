package com.jk.blog.utils;

import com.jk.blog.entity.Role;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUtilTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private User user;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        AuthUtil authUtil = new AuthUtil(userRepository);

        user = new User();
        user.setUserId(1L);
        user.setEmail("testuser@example.com");
        user.setUserName("testuser");

        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");

        user.setRoles(Set.of(roleAdmin));
    }

    @Test
    void test_GetAuthenticatedUser_ShouldReturnUser_WhenAuthenticated() {
        // Mock the SecurityContext and UserRepository
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser@example.com");
        // Mock UserRepository
        User user = new User();
        user.setEmail("testuser@example.com");
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));

        // Call the method
        User result = AuthUtil.getAuthenticatedUser();

        // Verify and Assert
        assertNotNull(result);
        assertEquals("testuser@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("testuser@example.com");
    }

    @Test
    void test_GetAuthenticatedUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("nonexistentuser@example.com");

        // Mock UserRepository
        when(userRepository.findByEmail("nonexistentuser@example.com")).thenReturn(Optional.empty());

        // Call the method and expect exception
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                AuthUtil::getAuthenticatedUser
        );

        // Verify and Assert
        assertEquals("User not found with email : nonexistentuser@example.com", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("nonexistentuser@example.com");
    }

    @Test
    void test_GetAuthenticatedUser_ShouldReturnNull_WhenPrincipalIsNotUserDetails() {
        // Mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // Call the method
        User result = AuthUtil.getAuthenticatedUser();

        // Verify and Assert
        assertNull(result);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void test_UserHasRole_ShouldReturnTrue_WhenUserHasRole() {
        boolean hasRole = AuthUtil.userHasRole(user, "ADMIN");

        assertTrue(hasRole);
    }

    @Test
    void test_UserHasRole_ShouldReturnFalse_WhenUserDoesNotHaveRole() {
        boolean hasRole = AuthUtil.userHasRole(user, "MODERATOR");

        assertFalse(hasRole);
    }

    @Test
    void test_UserHasRole_ShouldReturnFalse_WhenUserHasNoRoles() {
        user.setRoles(Collections.emptySet());

        boolean hasRole = AuthUtil.userHasRole(user, "ADMIN");

        assertFalse(hasRole);
    }
}