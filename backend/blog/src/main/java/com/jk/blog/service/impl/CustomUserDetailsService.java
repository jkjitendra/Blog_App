package com.jk.blog.service.impl;


import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        // Fetch user by email OR username (both are unique)
        User user = userRepository.findByEmail(login)
                .or(() -> userRepository.findByUserName(login))  // If email not found, try username
                .orElseThrow(() -> new ResourceNotFoundException("User", "email/username: ", login));

        // Convert User entity to Spring Security UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())  // Always use email as unique identifier
                .password(user.getPassword())  // Use encoded password
                .authorities(user.getAuthorities())  // Load roles & permissions
                .build();
    }
}
