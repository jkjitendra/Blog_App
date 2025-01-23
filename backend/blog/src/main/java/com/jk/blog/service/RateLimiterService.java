package com.jk.blog.service;

public interface RateLimiterService {

    boolean tryConsume(String key, String actionType);
}
