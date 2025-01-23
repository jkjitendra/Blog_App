package com.jk.blog.config;

import com.jk.blog.security.RateLimitInterceptor;
import com.jk.blog.service.RateLimiterService;
import com.jk.blog.service.impl.RateLimiterServiceImpl;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This configuration class is responsible for managing rate limiting across the application.
 * It defines rate-limiting rules using {@link Bucket} and registers the {@link RateLimitInterceptor}.
 *
 * <p><b>Why is it used?</b></p>
 * - It enforces global and per-endpoint rate limits using Bucket4j.
 * - Registers {@link RateLimitInterceptor} to apply rate limits before request processing.
 * - Provides a centralized approach to manage request throttling for security and API abuse prevention.
 *
 * <p><b>Implementation Details:</b></p>
 * - Implements {@link WebMvcConfigurer} to register interceptors.
 * - Defines beans for {@link RateLimiterService} and {@link RateLimitInterceptor}.
 */

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    private Bucket createBucket(int capacity, Duration refillDuration) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillIntervally(capacity, refillDuration)
                        .build())
                .build();
    }

    @Bean
    public RateLimiterService rateLimiterService() {
        return new RateLimiterServiceImpl();
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor());
    }
}
