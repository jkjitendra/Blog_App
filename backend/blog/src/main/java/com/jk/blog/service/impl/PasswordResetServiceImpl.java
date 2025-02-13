package com.jk.blog.service.impl;

import com.jk.blog.dto.AuthDTO.ResetPasswordDTO;
import com.jk.blog.dto.AuthDTO.VerifyOtpDTO;
import com.jk.blog.dto.MailBody;
import com.jk.blog.entity.PasswordResetToken;
import com.jk.blog.entity.User;
import com.jk.blog.exception.*;
import com.jk.blog.repository.PasswordResetTokenRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.EmailService;
import com.jk.blog.service.PasswordResetService;
import com.jk.blog.service.RateLimiterService;
import com.jk.blog.utils.GeneratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${otp.expiration-time}")
    private Long otpExpirationTime;

    @Override
    @Transactional
    public void generateOtp(String email) {

        if (!rateLimiterService.tryConsume(email, "OTP_REQUEST")) {
            throw new RateLimitExceededException("Too many OTP requests. Please wait.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Delete any existing OTP
        passwordResetTokenRepository.deleteByUser(user);

        // Generate OTP
        int otp = GeneratorUtils.generateOTP();

        // Save OTP
        saveOtpToken(user, otp);

        // Send Email
        sendOtpEmail(email, otp);

    }

    @Override
    @Transactional
    public void verifyOtp(VerifyOtpDTO verifyOtpDTO) {

        if (!rateLimiterService.tryConsume(verifyOtpDTO.getEmail(), "OTP_VERIFY")) {
            throw new RateLimitExceededException("Too many OTP verification attempts. Try again later.");
        }

        User user = userRepository.findByEmail(verifyOtpDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", verifyOtpDTO.getEmail()));

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByOtpAndUser(verifyOtpDTO.getOtp(), user)
                .orElseThrow(() -> new InvalidTokenException("otp", "Invalid OTP"));

        if (passwordResetToken.getExpirationTime().isBefore(Instant.now())) {
            passwordResetTokenRepository.deleteByUser(user);
            throw new TokenExpiredException("OTP expired");
        }

        passwordResetToken.setVerified(true);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {

        if (!rateLimiterService.tryConsume(resetPasswordDTO.getEmail(), "PASSWORD_RESET")) {
            throw new RateLimitExceededException("Too many password reset attempts. Try again later.");
        }

        User user = userRepository.findByEmail(resetPasswordDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", resetPasswordDTO.getEmail()));

        // **First check if passwords match before checking OTP**
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getRepeatPassword())) {
            throw new PasswordNotMatchException("Password and Confirm Password do not match. Please try again.");
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByUserAndVerified(user)
                .orElseThrow(() -> new InvalidTokenException("OTP", "OTP not verified"));

        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userRepository.save(user);

        // Delete OTP after successful password reset
        passwordResetTokenRepository.deleteByUser(user);
    }

    /**
     * Saves OTP Token in Database
     */
    private void saveOtpToken(User user, int otp) {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setOtp(otp);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpirationTime(Instant.now().plusSeconds(otpExpirationTime));
        passwordResetToken.setVerified(false);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    /**
     * Sends OTP Email
     */
    private void sendOtpEmail(String email, int otp) {
        MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("Your OTP Code")
                .text("Your one-time password (OTP) is: " + otp + ". It will expire in 5 minutes.")
                .build();

        try {
            emailService.sendEmail(mailBody);
        } catch (MailException e) {
            throw new EmailSendingException(email, e.getMessage());
        }
    }
}
