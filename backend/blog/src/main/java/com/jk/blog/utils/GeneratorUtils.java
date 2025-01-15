package com.jk.blog.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class GeneratorUtils {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public static Integer generateOTP() {
        // Generate a secure random 6-digit OTP
        return secureRandom.nextInt(900_000) + 100_000; // 100_000 to 999_999
    }

    public static String generateRefreshToken() {
        byte[] randomBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.withoutPadding().encodeToString(randomBytes);
    }
}
