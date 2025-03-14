package com.jk.blog.security;

import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public String getAuthenticatedUsername() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        throw new AccessDeniedException("User is not authenticated");
    }

    @Override
    public Long getAuthenticatedUserId() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            // ✅ Fetch user from the database using email/username
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
            return user.getUserId();
        }
        throw new AccessDeniedException("User is not authenticated");
    }

    /**
     * Checks if the authenticated user has any of the specified roles.
     * @param roles Array of roles to check against the authenticated user's authorities.
     * @return true if the user has at least one of the specified roles, false otherwise.
     */
    @Override
    public boolean hasAnyRole(String... roles) {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false; // No authenticated user
        }

        Set<String> roleSet = Arrays.stream(roles)
                .map(role -> "ROLE_" + role) // Prefix roles with "ROLE_"
                .collect(Collectors.toSet());

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roleSet::contains);
    }
}
