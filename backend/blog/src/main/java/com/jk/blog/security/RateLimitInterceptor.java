package com.jk.blog.security;

import com.jk.blog.exception.RateLimitConfigurationException;
import com.jk.blog.exception.RateLimitExceededException;
import io.github.bucket4j.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This interceptor applies rate limits to specific API endpoints.
 * It uses {@link Bucket4j} to enforce different rate limits for login, OTP requests, OTP verification, and password reset.
 *
 * <p><b>Why is it used?</b></p>
 * - Provides endpoint-specific rate limiting instead of a global approach.
 * - Protects sensitive authentication-related APIs from brute force attacks.
 * - Helps in controlling abuse of password reset and OTP verification processes.
 *
 * <p><b>Implementation Details:</b></p>
 * - Maps client IPs to separate rate-limiting buckets.
 * - Uses different rate limits for login, OTP requests, OTP verification, and password reset.
 * - Throws {@link RateLimitExceededException} when a client exceeds the allowed requests.
 */

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final Map<String, Bucket> loginRateLimiters = new ConcurrentHashMap<>();
    private final Map<String, Bucket> otpRequestLimiters = new ConcurrentHashMap<>();
    private final Map<String, Bucket> otpVerifyLimiters = new ConcurrentHashMap<>();
    private final Map<String, Bucket> resetPasswordLimiters = new ConcurrentHashMap<>();

    // Method to create a rate limit bucket
    private Bucket createBucket(int capacity, Duration refillDuration) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, refillDuration)
                        .build())
                .build();
    }

    private Bucket getBucket(String key, String actionType) {
        return switch (actionType) {
            case "LOGIN" -> loginRateLimiters.computeIfAbsent(key, k -> {
                logger.info("Creating login rate limiter for IP: {}", key);
                return createBucket(5, Duration.ofMinutes(5));
            });
            case "OTP_REQUEST" -> otpRequestLimiters.computeIfAbsent(key, k -> {
                logger.info("Creating OTP request rate limiter for IP: {}", key);
                return createBucket(5, Duration.ofHours(1));
            });
            case "OTP_VERIFY" -> otpVerifyLimiters.computeIfAbsent(key, k -> {
                logger.info("Creating OTP verification rate limiter for IP: {}", key);
                return createBucket(5, Duration.ofMinutes(30));
            });
            case "PASSWORD_RESET" -> resetPasswordLimiters.computeIfAbsent(key, k -> {
                logger.info("Creating password reset rate limiter for IP: {}", key);
                return createBucket(3, Duration.ofHours(1));
            });
            default -> {
                logger.warn("Unknown rate limit action type: {}", actionType);
                throw new RateLimitConfigurationException("Unknown rate limit action type: " + actionType);
            }
        };
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        String requestURI = request.getRequestURI();

        String actionType = getActionType(requestURI);
        if (actionType != null) {
            Bucket bucket = getBucket(clientIp, actionType);
            if (!bucket.tryConsume(1)) {
                logger.warn("Rate limit exceeded for action: {} | IP: {}", actionType, clientIp);
                throw new RateLimitExceededException("Too many requests. Please try again later.");
            } else {
                logger.info("Request allowed for action: {} | IP: {} | Remaining tokens: {}", actionType, clientIp, bucket.getAvailableTokens());
            }
        }

        return true;
    }

    // Determine which API endpoint needs rate limiting
    private String getActionType(String requestURI) {
        if (requestURI.contains("/api/v1/auth/login")) return "LOGIN";
        if (requestURI.contains("/api/v1/auth/forgot-password")) return "OTP_REQUEST";
        if (requestURI.contains("/api/v1/auth/verify-otp")) return "OTP_VERIFY";
        if (requestURI.contains("/api/v1/auth/reset-password")) return "PASSWORD_RESET";
        return null;
    }
}
