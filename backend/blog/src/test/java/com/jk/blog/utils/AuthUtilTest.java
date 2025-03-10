package com.jk.blog.utils;

import com.jk.blog.dto.AuthDTO.AuthenticatedUserDTO;
import com.jk.blog.entity.Role;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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

    @InjectMocks
    private AuthUtil authUtil;

    private User user;
    private AuthenticatedUserDTO authenticatedUserDTO;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);

        user = new User();
        user.setUserId(1L);
        user.setEmail("testuser@example.com");
        user.setUserName("testuser");

        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");

        user.setRoles(Set.of(roleAdmin));

        authenticatedUserDTO = new AuthenticatedUserDTO();
        authenticatedUserDTO.setOAuthUser(true);
        authenticatedUserDTO.setUser(user);
        authenticatedUserDTO.setProvider("github");
        authenticatedUserDTO.setEmail("testuser@example.com");
        authenticatedUserDTO.setRoles(user.getRoles().stream().map(Object::toString).collect(Collectors.toSet()));
        authenticatedUserDTO.setName(user.getRoles().toString());

    }

//    @Test
//    void test_GetAuthenticatedUser_ShouldReturnAuthenticatedUserDTO_WhenAuthenticated() {
//        // Mock SecurityContext and Authentication
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn(userDetails);
//        when(userDetails.getUsername()).thenReturn("testuser@example.com");
//
//        // Mock userRepository to return the test user
//        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
//
//        try (MockedStatic<AuthenticatedUserDTO> authMock = mockStatic(AuthenticatedUserDTO.class)) {
//            authMock.when(() -> AuthenticatedUserDTO.fromUser(user)).thenReturn(authenticatedUserDTO);
//
//            // Call the method
//            AuthenticatedUserDTO result = authUtil.getAuthenticatedUser();
//
//            // Verify and Assert
//            assertNotNull(result, "Authenticated user should not be null");
//            assertEquals("testuser@example.com", result.getEmail());
//            assertEquals(user, result.getUser());
//        }
//
//        verify(userRepository, times(1)).findByEmail("testuser@example.com");
//    }


    @Test
    void test_GetAuthenticatedUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("nonexistentuser@example.com");

        // Call the method and expect exception
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> authUtil.getAuthenticatedUser());

        // Verify and Assert
        assertEquals("User not found with email : nonexistentuser@example.com", exception.getMessage());
    }

    @Test
    void test_GetAuthenticatedUser_ShouldReturnNull_WhenPrincipalIsNotUserDetails() {
        // Mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // Call the method
        AuthenticatedUserDTO result = authUtil.getAuthenticatedUser();

        // Verify and Assert
        assertNull(result);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void test_UserHasRole_ShouldReturnTrue_WhenUserHasRole() {
        boolean hasRole = authUtil.userHasRole(user, "ADMIN");

        assertTrue(hasRole);
    }

    @Test
    void test_UserHasRole_ShouldReturnFalse_WhenUserDoesNotHaveRole() {
        boolean hasRole = authUtil.userHasRole(user, "MODERATOR");

        assertFalse(hasRole);
    }

    @Test
    void test_UserHasRole_ShouldReturnFalse_WhenUserHasNoRoles() {
        user.setRoles(Collections.emptySet());

        boolean hasRole = authUtil.userHasRole(user, "ADMIN");

        assertFalse(hasRole);
    }
}