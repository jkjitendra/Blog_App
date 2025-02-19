package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.AuthDTO.*;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.entity.RefreshToken;
import com.jk.blog.entity.User;
import com.jk.blog.exception.InvalidTokenException;
import com.jk.blog.exception.TokenExpiredException;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.AuthService;
import com.jk.blog.service.PasswordResetService;
import com.jk.blog.service.RefreshTokenService;
import com.jk.blog.service.UserService;
import com.jk.blog.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private RegisterRequestBody registerRequest;
    private AuthRequest authRequest;
    private User testUser;
    private RefreshToken testRefreshToken;


    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestBody();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        authRequest = new AuthRequest();
        authRequest.setLogin("test@example.com");
        authRequest.setPassword("password123");

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setUserLastLoggedInDate(Instant.now());

        testRefreshToken = new RefreshToken();
        testRefreshToken.setRefreshToken("sample-refresh-token");
        testRefreshToken.setUser(testUser);

    }

    @Test
    void test_registerUser_Success() {
        when(authService.registerUser(any(RegisterRequestBody.class))).thenReturn(new UserResponseBody());

        ResponseEntity<APIResponse<UserResponseBody>> response = authController.registerUser(registerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
    }

    @Test
    void test_login_Success() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(authService.generateAccessToken(any(AuthRequest.class))).thenReturn(new AuthResponse("access-token"));
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(testRefreshToken);

        ResponseEntity<APIResponse<AuthResponse>> response = authController.login(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
    }


    @Test
    void test_login_InvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<APIResponse<AuthResponse>> response = authController.login(authRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
    }

    @Test
    void test_logout_Success() {
        Cookie cookie = new Cookie("refreshToken", "sample-refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doNothing().when(refreshTokenService).deleteRefreshToken(anyString());

        ResponseEntity<APIResponse<?>> responseEntity = authController.logout(request, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().getSuccess());
    }

    @Test
    void test_createAccessToken_Success() {
        Cookie cookie = new Cookie("refreshToken", "sample-refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(refreshTokenService.verifyRefreshToken(anyString())).thenReturn(testRefreshToken);
        when(jwtUtil.generateToken(anyString())).thenReturn("new-access-token");

        ResponseEntity<APIResponse<AuthResponse>> response = authController.createAccessToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("new-access-token", response.getBody().getData().getAccessToken());
    }

    @Test
    void test_createAccessToken_ExpiredToken() {
        Cookie cookie = new Cookie("refreshToken", "expired-refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new TokenExpiredException("Refresh token expired"))
                .when(refreshTokenService).verifyRefreshToken(anyString());

        ResponseEntity<APIResponse<AuthResponse>> response = authController.createAccessToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
    }

    @Test
    void test_forgotPassword_Success() {
        doNothing().when(passwordResetService).generateOtp(anyString());

        ResponseEntity<APIResponse<String>> response = authController.forgotPassword(Map.of("email", "test@example.com"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
    }

    @Test
    void test_verifyOtp_Success() {
        VerifyOtpDTO verifyOtpDTO = new VerifyOtpDTO();
        verifyOtpDTO.setEmail("test@example.com");
        verifyOtpDTO.setOtp("123456");

        // Mock the service call
        doNothing().when(passwordResetService).verifyOtp(any(VerifyOtpDTO.class));

        ResponseEntity<APIResponse<String>> response = authController.verifyOtp(verifyOtpDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("OTP verified.", response.getBody().getMessage());

        verify(passwordResetService, times(1)).verifyOtp(any(VerifyOtpDTO.class));
    }

    @Test
    void test_verifyOtp_InvalidOtp() {
        VerifyOtpDTO verifyOtpDTO = new VerifyOtpDTO();
        verifyOtpDTO.setEmail("test@example.com");
        verifyOtpDTO.setOtp("000000");

        doThrow(new InvalidTokenException("otp", "Invalid OTP"))
                .when(passwordResetService).verifyOtp(any(VerifyOtpDTO.class));

        ResponseEntity<APIResponse<String>> response = authController.verifyOtp(verifyOtpDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertEquals("Invalid OTP", response.getBody().getErrorDetails());

        verify(passwordResetService, times(1)).verifyOtp(any(VerifyOtpDTO.class));
    }

    @Test
    void test_resetPassword_Success() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setEmail("test@example.com");
        resetPasswordDTO.setNewPassword("NewPassword123!");

        // Mock the service call
        doNothing().when(passwordResetService).resetPassword(any(ResetPasswordDTO.class));

        ResponseEntity<APIResponse<String>> response = authController.resetPassword(resetPasswordDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Password reset successful.", response.getBody().getMessage());

        verify(passwordResetService, times(1)).resetPassword(any(ResetPasswordDTO.class));
    }

    @Test
    void test_resetPassword_InvalidToken() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setEmail("test@example.com");
        resetPasswordDTO.setNewPassword("NewPassword123!");
        resetPasswordDTO.setRepeatPassword("NewPassword123!"); // Ensure it matches

        // Simulate token expiration or invalid token scenario
        doThrow(new InvalidTokenException("OTP", "OTP not verified")).when(passwordResetService).resetPassword(any(ResetPasswordDTO.class));

        ResponseEntity<APIResponse<String>> response = authController.resetPassword(resetPasswordDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()); // Changed to match exception handling
        assertFalse(response.getBody().getSuccess());
        assertEquals("OTP not verified", response.getBody().getErrorDetails()); // Match the exception message

        verify(passwordResetService, times(1)).resetPassword(any(ResetPasswordDTO.class));
    }

    @Test
    void test_activateUser_Success() {
        when(userService.activateUserAccount(any(AuthRequest.class))).thenReturn(null); // **Fix**

        ResponseEntity<APIResponse<Void>> response = authController.activateUser(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
    }
}
