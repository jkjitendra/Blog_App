package com.jk.blog.security;

import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationFacadeImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthenticationFacadeImpl authenticationFacade;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void test_getAuthentication_WhenAuthenticated_returnAuthenticationObject() {
        Authentication result = authenticationFacade.getAuthentication();
        assertNotNull(result);
        assertEquals(authentication, result);
    }

    @Test
    void test_getAuthentication_WhenNotAuthenticated_returnNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        Authentication result = authenticationFacade.getAuthentication();
        assertNull(result);
    }

    @Test
    void test_getAuthenticatedUsername_WhenAuthenticated_returnUsername() {
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testUser");

        String username = authenticationFacade.getAuthenticatedUsername();
        assertEquals("testUser", username);
    }

    @Test
    void test_getAuthenticatedUsername_WhenNotAuthenticated_ThrowsAccessDeniedException() {
        when(authentication.getPrincipal()).thenReturn(null);

        assertThrows(AccessDeniedException.class, authenticationFacade::getAuthenticatedUsername);
    }

    @Test
    void test_getAuthenticatedUserId_WhenAuthenticated_returnUserId() {
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        User user = new User();
        user.setUserId(1L);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Long userId = authenticationFacade.getAuthenticatedUserId();
        assertEquals(1L, userId);
    }

    @Test
    void test_getAuthenticatedUserId_WhenUserNotFound_ThrowsResourceNotFoundException() {
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("unknown@example.com");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, authenticationFacade::getAuthenticatedUserId);
    }

    @Test
    void test_getAuthenticatedUserId_WhenNotAuthenticated_ThrowsAccessDeniedException() {
        when(authentication.getPrincipal()).thenReturn(null);

        assertThrows(AccessDeniedException.class, authenticationFacade::getAuthenticatedUserId);
    }


    @Test
    void test_hasAnyRole_WhenUserHasNoMatchingRole_ReturnsFalse() {
        // Mock a GrantedAuthority
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn("ROLE_GUEST");

        // Create a set of authorities
        Collection<? extends GrantedAuthority> authorities = Set.of(authority); // ✅ Explicit wildcard usage

        // Ensure authentication is mocked correctly
        when(authentication.isAuthenticated()).thenReturn(true);

        when(authentication.getAuthorities()).thenReturn((Collection) authorities); // ✅ Corrected return type

        // Call the method and verify
        boolean result = authenticationFacade.hasAnyRole("ADMIN", "USER");
        assertFalse(result); // Expect false since "ROLE_GUEST" doesn't match "ADMIN" or "USER"
    }

    @Test
    void test_hasAnyRole_WhenUserIsNotAuthenticated_ReturnsFalse() {
        when(authentication.isAuthenticated()).thenReturn(false);

        boolean result = authenticationFacade.hasAnyRole("ADMIN");
        assertFalse(result);
    }

    @Test
    void test_hasAnyRole_WhenAuthenticationIsNull_ReturnsFalse() {
        when(securityContext.getAuthentication()).thenReturn(null);

        boolean result = authenticationFacade.hasAnyRole("ADMIN");
        assertFalse(result);
    }
}