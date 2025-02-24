package com.jk.blog.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalRateLimiterFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Bucket bucket;

    @InjectMocks
    private GlobalRateLimiterFilter globalRateLimiterFilter;

    @BeforeEach
    void setUp() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    }

    @Test
    void test_doFilter_WhenRequestWithinLimit_PassesThrough() throws IOException, ServletException {
        doNothing().when(filterChain).doFilter(request, response);

        globalRateLimiterFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void test_doFilter_WhenRequestExceedsLimit_ReturnsTooManyRequests() throws Exception{
        // Use reflection to inject the mocked bucket into the rateLimiters map
        Map<String, Bucket> rateLimiters = new ConcurrentHashMap<>();
        rateLimiters.put("192.168.1.1", bucket); // Pre-populate with the mocked bucket
        Field rateLimitersField = GlobalRateLimiterFilter.class.getDeclaredField("rateLimiters");
        rateLimitersField.setAccessible(true);
        rateLimitersField.set(globalRateLimiterFilter, rateLimiters);

        // Arrange
        when(bucket.tryConsume(1)).thenReturn(false); // Simulate rate limit exceeded
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // Act
        globalRateLimiterFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain, never()).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response.getWriter(), times(1)).write("Too many requests. Try again later.");
    }
}