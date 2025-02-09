package com.jk.blog.utils;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorUtilsTest {

    @RepeatedTest(10) // Run multiple times to ensure randomness and range
    void test_GenerateOTP_ShouldReturnSixDigitInteger_WhenCalled() {
        Integer otp = GeneratorUtils.generateOTP();

        assertNotNull(otp, "Generated OTP should not be null");
        assertTrue(otp >= 100_000 && otp <= 999_999, "OTP should be a 6-digit number");
    }

    @Test
    void test_GenerateOTP_ShouldGenerateUniqueValues_WhenCalledMultipleTimes() {
        Set<Integer> otpSet = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            otpSet.add(GeneratorUtils.generateOTP());
        }

        assertTrue(otpSet.size() > 90, "Generated OTPs should be mostly unique");
    }

    @RepeatedTest(10)
    void test_GenerateRefreshToken_ShouldReturnValidBase64EncodedString_WhenCalled() {
        String refreshToken = GeneratorUtils.generateRefreshToken();

        assertNotNull(refreshToken, "Refresh token should not be null");
        assertEquals(43, refreshToken.length(), "Refresh token should be of expected Base64 length (without padding)");
        assertTrue(refreshToken.matches("^[A-Za-z0-9_-]+$"), "Refresh token should be Base64 URL safe");
    }

    @Test
    void test_GenerateRefreshToken_ShouldGenerateUniqueTokens_WhenCalledMultipleTimes() {
        Set<String> tokenSet = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            tokenSet.add(GeneratorUtils.generateRefreshToken());
        }

        assertTrue(tokenSet.size() > 90, "Generated refresh tokens should be mostly unique");
    }
}