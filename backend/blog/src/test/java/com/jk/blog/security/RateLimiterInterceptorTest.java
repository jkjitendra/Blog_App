package com.jk.blog.security;

import com.jk.blog.exception.RateLimitConfigurationException;
import com.jk.blog.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    }

    @Test
    void test_preHandle_WhenLoginRequestWithinLimit_ShouldPass() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void test_preHandle_WhenOtpRequestWithinLimit_ShouldPass() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/forgot-password");

        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void test_preHandle_WhenOtpVerifyWithinLimit_ShouldPass() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/verify-otp");

        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void test_preHandle_WhenPasswordResetWithinLimit_ShouldPass() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/reset-password");

        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void test_preHandle_WhenRateLimitExceeded_ShouldThrowException() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        // Simulate exceeding rate limit by consuming all tokens
        for (int i = 0; i < 5; i++) {
            rateLimitInterceptor.preHandle(request, response, new Object());
        }

        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitInterceptor.preHandle(request, response, new Object());
        });
    }

    @Test
    void test_preHandle_WhenActionTypeIsNull_DoNothing() throws Exception {
        // Simulate an unknown action type
        when(request.getRequestURI()).thenReturn("/api/v1/health");

        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        assertTrue(result, "Should return true for other urls without applying rate limiting.");

    }
}