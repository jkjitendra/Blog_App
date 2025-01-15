package com.jk.blog.security;

import com.jk.blog.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

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
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getUserId(); // ✅ Use userId instead of looking up in DB
        }
        throw new AccessDeniedException("User is not authenticated");
    }

    @Override
    public boolean hasRole(String role) {
        Collection<? extends GrantedAuthority> authorities = getAuthentication().getAuthorities();
        return authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}
