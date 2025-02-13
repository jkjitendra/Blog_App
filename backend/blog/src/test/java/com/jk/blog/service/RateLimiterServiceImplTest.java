package com.jk.blog.service;

import com.jk.blog.service.impl.RateLimiterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceImplTest {

    @InjectMocks
    private RateLimiterServiceImpl rateLimiterService;

    private final String testKey = "testUser";

    @BeforeEach
    void setUp() {
        // Reset rate limiters before each test
        rateLimiterService = new RateLimiterServiceImpl();
    }

    @Test
    void testTryConsume_OtpRequest_WithinLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.tryConsume(testKey, "OTP_REQUEST"));
        }
    }

    @Test
    void testTryConsume_OtpRequest_ExceedsLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.tryConsume(testKey, "OTP_REQUEST"));
        }
        assertFalse(rateLimiterService.tryConsume(testKey, "OTP_REQUEST")); // 6th request should fail
    }

    @Test
    void testTryConsume_OtpVerification_WithinLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.tryConsume(testKey, "OTP_VERIFY"));
        }
    }

    @Test
    void testTryConsume_OtpVerification_ExceedsLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.tryConsume(testKey, "OTP_VERIFY"));
        }
        assertFalse(rateLimiterService.tryConsume(testKey, "OTP_VERIFY")); // 6th request should fail
    }

    @Test
    void testTryConsume_PasswordReset_WithinLimit() {
        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimiterService.tryConsume(testKey, "PASSWORD_RESET"));
        }
    }

    @Test
    void testTryConsume_PasswordReset_ExceedsLimit() {
        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimiterService.tryConsume(testKey, "PASSWORD_RESET"));
        }
        assertFalse(rateLimiterService.tryConsume(testKey, "PASSWORD_RESET")); // 4th request should fail
    }

    @Test
    void testTryConsume_UnknownActionType_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                rateLimiterService.tryConsume(testKey, "INVALID_ACTION")
        );

        assertEquals("Unknown rate limit action type: INVALID_ACTION", exception.getMessage());
    }

}