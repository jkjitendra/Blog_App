package com.jk.blog.security;

import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This filter applies global rate limiting to all incoming HTTP requests.
 * It uses {@link Bucket4j} to restrict the number of requests a client can make in a specific timeframe.
 *
 * <p><b>Why is it used?</b></p>
 * - Ensures that all incoming requests comply with rate-limiting policies.
 * - Prevents DDoS attacks and excessive resource consumption.
 * - Works as a global request filter before controllers process requests.
 *
 * <p><b>Implementation Details:</b></p>
 * - Uses IP-based throttling to track and limit client requests.
 * - Applies a bucket with a limit of 100 requests per minute per IP.
 * - Rejects requests with HTTP status 429 (Too Many Requests) if the limit is exceeded.
 */

@Component
public class GlobalRateLimiterFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(GlobalRateLimiterFilter.class);

    private final Map<String, Bucket> rateLimiters = new ConcurrentHashMap<>();


    private Bucket getBucket(String clientIp) {
        return rateLimiters.computeIfAbsent(clientIp, key -> {
            logger.info("Creating global rate limiter for IP: {}", clientIp);
            return Bucket.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(100)
                            .refillGreedy(100, Duration.ofMinutes(1))
                            .build())
                    .build();
        });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIp = httpRequest.getRemoteAddr();

        Bucket bucket = getBucket(clientIp);

        if (bucket.tryConsume(1)) {
            logger.info("Global rate limit passed | IP: {} | Remaining tokens: {}", clientIp, bucket.getAvailableTokens());
            chain.doFilter(request, response);
        } else {
            logger.warn("Global rate limit exceeded | IP: {}", clientIp);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("Too many requests. Try again later.");
        }
    }
}
