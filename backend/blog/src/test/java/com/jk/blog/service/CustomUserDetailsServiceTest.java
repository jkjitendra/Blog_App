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
        user.setUserName("johndoe");
        user.setEmail("johndoe@example.com");
        user.setPassword("encodedPassword");
    }

    /**
     * Test case: Successfully load user by email
     */
    @Test
    void test_loadUserByUsername_ShouldReturnUserDetails_WhenEmailExists() {
        when(userRepository.findByEmail("johndoe@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("johndoe@example.com");

        assertNotNull(userDetails);
        assertEquals("johndoe@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        verify(userRepository, times(1)).findByEmail("johndoe@example.com");
    }

    /**
     * Test case: Successfully load user by username
     */
    @Test
    void test_loadUserByUsername_ShouldReturnUserDetails_WhenUsernameExists() {
        when(userRepository.findByEmail("johndoe")).thenReturn(Optional.empty());
        when(userRepository.findByUserName("johndoe")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("johndoe");

        assertNotNull(userDetails);
        assertEquals("johndoe@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        verify(userRepository, times(1)).findByEmail("johndoe");
        verify(userRepository, times(1)).findByUserName("johndoe");
    }

    /**
     * Test case: Throw ResourceNotFoundException when user not found by email or username
     */
    @Test
    void test_loadUserByUsername_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("unknown@example.com"));

        verify(userRepository, times(1)).findByEmail("unknown@example.com");
        verify(userRepository, times(1)).findByUserName("unknown@example.com");
    }

}
