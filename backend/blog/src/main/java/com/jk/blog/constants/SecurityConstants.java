package com.jk.blog.constants;

public class SecurityConstants {

    // Public endpoints that do not require authentication
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/api/v1/oauth/success",
            "/oauth2/**",
            "/login/**",
            "/api/v1/oauth/user",
            "/h2-console/**",
            "/swagger-ui/**",  // Swagger UI HTML
            "/swagger-ui.html",       // Legacy Swagger UI
            "/v3/api-docs/**", // OpenAPI JSON docs
            "/swagger-resources/**", // Swagger resources
            "/swagger-resources",
            "/actuator/**",           // If using actuator endpoints
            "/webjars/**"       // WebJars for Swagger UI assets
    };

    public final static String SECURITY_SCHEME_NAME = "bearerAuth";

    // Private constructor to prevent instantiation
    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
