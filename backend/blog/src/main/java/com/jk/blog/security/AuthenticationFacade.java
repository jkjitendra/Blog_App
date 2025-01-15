package com.jk.blog.security;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {

    Authentication getAuthentication();

    String getAuthenticatedUsername();

    Long getAuthenticatedUserId();

    boolean hasRole(String role);

}