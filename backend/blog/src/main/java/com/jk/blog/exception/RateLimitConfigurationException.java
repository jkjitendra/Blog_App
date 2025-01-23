package com.jk.blog.exception;

public class RateLimitConfigurationException extends RuntimeException {
    public RateLimitConfigurationException(String message) {
        super(message);
    }
}