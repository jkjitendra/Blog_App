package com.jk.blog.service;

import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("johndoe@example.com");
        user.setPassword("encodedPassword");
        user.setProvider("local");
    }

    /**
     * Test case: Successfully load user by email
     */
    @Test
    void test_loadUserByUsername_ShouldReturnUserDetails_WhenEmailExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        assertNotNull(userDetails);
        assertFalse(userDetails.getPassword().isEmpty(), "Password should not be empty!");
        assertEquals("encodedPassword", userDetails.getPassword());
        verify(userRepository, times(1)).findByEmail("johndoe@example.com");
    }

    @Test
    void test_loadUserByUsername_ShouldReturnEmptyPassword_WhenEmailExistsAndProviderIsNotLocal() {
        user.setProvider("github");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        assertNotNull(userDetails);
        assertTrue(userDetails.getPassword().isEmpty(), "Password should be empty!");
        assertNotEquals("encodedPassword", userDetails.getPassword());
        verify(userRepository, times(1)).findByEmail("johndoe@example.com");
    }

    /**
     * Test case: Throw ResourceNotFoundException when user not found by email
     */
    @Test
    void test_loadUserByUsername_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("unknown@example.com"));

        verify(userRepository, times(1)).findByEmail("unknown@example.com");
    }

}
