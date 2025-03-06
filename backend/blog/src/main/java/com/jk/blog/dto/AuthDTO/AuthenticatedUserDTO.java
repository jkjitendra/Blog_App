package com.jk.blog.dto.AuthDTO;

import com.jk.blog.entity.Role;
import com.jk.blog.entity.User;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticatedUserDTO {
    private String email;
    private String name;
    private Set<String> roles; // Supports multiple roles dynamically
    private String provider; // Will be null for normal users
    private boolean isOAuthUser;
    private User user;

    public static AuthenticatedUserDTO fromUser(User user) {
        return AuthenticatedUserDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .provider(user.getProvider()) // "local" for normal users
                .isOAuthUser(!"local".equals(user.getProvider())) // Check if OAuth user
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .user(user)
                .build();
    }

}