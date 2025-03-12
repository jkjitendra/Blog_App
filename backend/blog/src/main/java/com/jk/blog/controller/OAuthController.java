package com.jk.blog.controller;

import com.jk.blog.dto.user.UserOAuthDTO;
import com.jk.blog.entity.User;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/oauth")
public class OAuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> oauthSuccess(@RequestParam("provider") String provider) {
        return ResponseEntity.ok(Map.of("message", "Logged in using " + provider));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAuthenticatedUser(@CookieValue(value = "accessToken", required = false) String accessToken) {
        System.out.println("access Token" + accessToken);
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Session expired. Please log in again."));
        }

        try {
            String email = jwtUtil.extractUsername(accessToken);
            Optional<User> existingUser = userRepository.findByEmail(email);

            if (existingUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found in the system."));
            }

            User user = existingUser.get();

            UserOAuthDTO oAuthDTO = UserOAuthDTO.builder()
                    .email(user.getEmail())
                    .name(user.getName())
                    .provider(user.getProvider())
                    .build();

            return ResponseEntity.ok(oAuthDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token. Please log in again.");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "OAuth2 User not found. Make sure you are logged in."));
        }

        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User is not authenticated via OAuth2."));
        }

        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "OAuth authentication failed: No email received."));
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found in the system."));
        }

        User user = existingUser.get();

        UserOAuthDTO oAuthDTO = UserOAuthDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .provider(user.getProvider())
                .build();

        return ResponseEntity.ok(oAuthDTO);
    }
}