package com.jk.blog.service.impl;

import com.jk.blog.service.RateLimiterService;
import io.github.bucket4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This service provides a reusable way to apply rate-limiting to specific actions in the application.
 * It uses {@link Bucket4j} to enforce action-based rate limits.
 *
 * <p><b>Why is it used?</b></p>
 * - Allows flexible rate limiting per user action.
 * - Enables centralized management of rate-limiting policies.
 * - Supports OTP requests, OTP verification, and password reset limits.
 */

@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterServiceImpl.class);

    private final Map<String, Bucket> otpRequestBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> otpVerificationBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> passwordResetBuckets = new ConcurrentHashMap<>();

    private Bucket createBucket(int capacity, Duration refillDuration) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, refillDuration)
                        .build())
                .build();
    }

    @Override
    public boolean tryConsume(String key, String actionType) {
        Bucket bucket = switch (actionType) {
            case "OTP_REQUEST" -> otpRequestBuckets.computeIfAbsent(key, k -> {
                logger.info("Creating OTP request rate limiter for key: {}", key);
                return createBucket(5, Duration.ofHours(1));
            });
            case "OTP_VERIFY" -> otpVerificationBuckets.computeIfAbsent(key, k -> {
                logger.info("Creating OTP verification rate limiter for key: {}", key);
                return createBucket(5, Duration.ofMinutes(30));
            });
            case "PASSWORD_RESET" -> passwordResetBuckets.computeIfAbsent(key, k -> {
                logger.info("Creating password reset rate limiter for key: {}", key);
                return createBucket(3, Duration.ofHours(1));
            });
            default -> {
                logger.warn("Unknown rate limit action type: {}", actionType);
                throw new IllegalArgumentException("Unknown rate limit action type: " + actionType);
            }
        };

        boolean allowed = bucket.tryConsume(1);
        if (allowed) {
            logger.info("Request allowed for action: {} | Key: {} | Remaining tokens: {}", actionType, key, bucket.getAvailableTokens());
        } else {
            logger.warn("Rate limit exceeded for action: {} | Key: {}", actionType, key);
        }
        return allowed;
    }
}
