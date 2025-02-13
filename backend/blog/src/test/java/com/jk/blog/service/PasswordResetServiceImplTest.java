package com.jk.blog.service;

import com.jk.blog.dto.AuthDTO.ResetPasswordDTO;
import com.jk.blog.dto.AuthDTO.VerifyOtpDTO;
import com.jk.blog.dto.MailBody;
import com.jk.blog.entity.PasswordResetToken;
import com.jk.blog.entity.User;
import com.jk.blog.exception.*;
import com.jk.blog.repository.PasswordResetTokenRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.impl.PasswordResetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    private static final String TEST_EMAIL = "test@example.com";
    private static final int TEST_OTP = 123456;

    private User testUser;
    private PasswordResetToken passwordResetToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword("oldEncodedPassword");

        passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(testUser);
        passwordResetToken.setOtp(TEST_OTP);
        passwordResetToken.setExpirationTime(Instant.now().plusSeconds(300));
        passwordResetToken.setVerified(false);

        ReflectionTestUtils.setField(passwordResetService, "otpExpirationTime", 300L);


    }

    /** OTP GENERATION TESTS **/

    @Test
    void test_generateOtp_whenRateLimitExceeded_returnThrowsException() {
        when(rateLimiterService.tryConsume(TEST_EMAIL, "OTP_REQUEST")).thenReturn(false);

        assertThrows(RateLimitExceededException.class, () -> passwordResetService.generateOtp(TEST_EMAIL));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void test_generateOtp_whenUserNotFound_returnThrowsResourceNotFoundException() {
        when(rateLimiterService.tryConsume(TEST_EMAIL, "OTP_REQUEST")).thenReturn(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> passwordResetService.generateOtp(TEST_EMAIL));
    }

    @Test
    void test_generateOtp_whenMailSendingFails_returnThrowsEmailSendingException() {
        when(rateLimiterService.tryConsume(TEST_EMAIL, "OTP_REQUEST")).thenReturn(true); // Ensure rate limiter allows OTP request
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        doThrow(new MailException("Failed to send email") {}).when(emailService).sendEmail(any(MailBody.class));

        assertThrows(EmailSendingException.class, () -> passwordResetService.generateOtp(TEST_EMAIL));
    }

    @Test
    void test_generateOtp_whenSuccessful_returnSavesOtpToken() {
        when(rateLimiterService.tryConsume(TEST_EMAIL, "OTP_REQUEST")).thenReturn(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        passwordResetService.generateOtp(TEST_EMAIL);

        verify(passwordResetTokenRepository, times(1)).deleteByUser(testUser);
        verify(passwordResetTokenRepository, times(1)).save(tokenCaptor.capture());
        verify(emailService, times(1)).sendEmail(any(MailBody.class));

        assertEquals(TEST_EMAIL, tokenCaptor.getValue().getUser().getEmail());
        assertFalse(tokenCaptor.getValue().isVerified());
    }

    /** OTP VERIFICATION TESTS **/

    @Test
    void test_verifyOtp_whenRateLimitExceeded_returnThrowsException() {
        VerifyOtpDTO verifyOtpDTO = new VerifyOtpDTO(TEST_EMAIL, TEST_OTP);
        when(rateLimiterService.tryConsume(TEST_EMAIL, "OTP_VERIFY")).thenReturn(false);

        assertThrows(RateLimitExceededException.class, () -> passwordResetService.verifyOtp(verifyOtpDTO));
    }

    @Test
    void test_verifyOtp_whenUserNotFound_returnThrowsResourceNotFoundException() {
        VerifyOtpDTO verifyOtpDTO = new VerifyOtpDTO(TEST_EMAIL, TEST_OTP);
        when(rateLimiterService.tryConsume(TEST_EMAIL, "OTP_VERIFY")).thenReturn(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> passwordResetService.verifyOtp(verifyOtpDTO));
    }

    @Test
    void test_verifyOtp_whenOtpInvalid_returnThrowsInvalidTokenException() {
        VerifyOtpDTO verifyOtpDTO = new VerifyOtpDTO(TEST_EMAIL, TEST_OTP);
        when(rateLimiterService.tryConsume(TEST_EMAIL, "OTP_VERIFY")).thenReturn(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.findByOtpAndUser(TEST_OTP, testUser)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> passwordResetService.verifyOtp(verifyOtpDTO));
    }

    @Test
    void test_verifyOtp_whenOtpExpired_returnThrowsTokenExpiredException() {
        VerifyOtpDTO verifyOtpDTO = new VerifyOtpDTO(TEST_EMAIL, TEST_OTP);
        passwordResetToken.setExpirationTime(Instant.now().minusSeconds(10)); // Expired OTP

        when(rateLimiterService.tryConsume(TEST_EMAIL, "OTP_VERIFY")).thenReturn(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.findByOtpAndUser(TEST_OTP, testUser)).thenReturn(Optional.of(passwordResetToken));

        assertThrows(TokenExpiredException.class, () -> passwordResetService.verifyOtp(verifyOtpDTO));

        verify(passwordResetTokenRepository, times(1)).deleteByUser(testUser);
    }

    @Test
    void test_verifyOtp_whenSuccessful_returnMarksOtpAsVerified() {
        VerifyOtpDTO verifyOtpDTO = new VerifyOtpDTO(TEST_EMAIL, TEST_OTP);
        when(rateLimiterService.tryConsume(TEST_EMAIL, "OTP_VERIFY")).thenReturn(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.findByOtpAndUser(TEST_OTP, testUser)).thenReturn(Optional.of(passwordResetToken));

        passwordResetService.verifyOtp(verifyOtpDTO);

        assertTrue(passwordResetToken.isVerified());
        verify(passwordResetTokenRepository, times(1)).save(passwordResetToken);
    }

    /** PASSWORD RESET TESTS **/

    @Test
    void test_resetPassword_whenPasswordsDoNotMatch_returnThrowsException() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(TEST_EMAIL, "newPass123", "wrongPass");

        when(rateLimiterService.tryConsume(TEST_EMAIL, "PASSWORD_RESET")).thenReturn(true); // Ensure rate limiter doesn't fail first
        when(userRepository.findByEmail(resetPasswordDTO.getEmail())).thenReturn(Optional.ofNullable(testUser));

        assertThrows(PasswordNotMatchException.class, () -> passwordResetService.resetPassword(resetPasswordDTO));
    }

    @Test
    void test_resetPassword_whenRateLimitExceeded_returnThrowsException() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(TEST_EMAIL, "newPass123", "newPass123");
        when(rateLimiterService.tryConsume(TEST_EMAIL, "PASSWORD_RESET")).thenReturn(false);

        assertThrows(RateLimitExceededException.class, () -> passwordResetService.resetPassword(resetPasswordDTO));
    }

    @Test
    void test_resetPassword_whenUserNotFound_returnThrowsResourceNotFoundException() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(TEST_EMAIL, "newPass123", "newPass123");
        when(rateLimiterService.tryConsume(TEST_EMAIL, "PASSWORD_RESET")).thenReturn(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> passwordResetService.resetPassword(resetPasswordDTO));
    }

    @Test
    void test_resetPassword_whenOtpNotVerified_returnThrowsInvalidTokenException() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(TEST_EMAIL, "newPass123", "newPass123");
        when(rateLimiterService.tryConsume(TEST_EMAIL, "PASSWORD_RESET")).thenReturn(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.findByUserAndVerified(testUser)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> passwordResetService.resetPassword(resetPasswordDTO));
    }
}