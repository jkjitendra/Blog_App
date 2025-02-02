package com.jk.blog.controller;

import com.jk.blog.controller.api.AuthApi;
import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.AuthDTO.*;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.entity.RefreshToken;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.TokenExpiredException;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.AuthService;
import com.jk.blog.service.PasswordResetService;
import com.jk.blog.service.RefreshTokenService;
import com.jk.blog.service.UserService;
import com.jk.blog.utils.JwtUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
@SecurityRequirements()
public class AuthController implements AuthApi {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.cookie.secure}")
    private boolean isCookieSecure;

    @Value("${secret.jwt.refresh-expiration-time}")
    private long refreshExpirationTime;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<UserResponseBody>> registerUser(@Valid @RequestBody RegisterRequestBody registerRequestBody) {
        UserResponseBody userResponse = this.authService.registerUser(registerRequestBody);
        return ResponseEntity.ok(new APIResponse<>(true, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getLogin(), authRequest.getPassword())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new APIResponse<>(false, "Invalid credentials", null, e.getMessage()));
        }

        User user = this.userRepository.findByEmail(authRequest.getLogin())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authRequest.getLogin()));

        // ✅ Update last login timestamp
        user.setUserLastLoggedInDate(Instant.now());
        this.userRepository.save(user);

        // generate accessToken
        AuthResponse authResponse = this.authService.generateAccessToken(authRequest);
        // generate refreshToken
        RefreshToken refreshToken = this.refreshTokenService.createRefreshToken(authRequest.getLogin());

        // Set the refresh token as an HTTP-only cookie
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken.getRefreshToken())
                .httpOnly(true)
                .secure(isCookieSecure)
                .path("/")
                .maxAge(refreshExpirationTime/1000) // 7 days
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new APIResponse<>(true, "Login successful", AuthResponse.builder().accessToken(authResponse.getAccessToken()).build()));
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // Extract refreshToken from Cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken != null) {
            this.refreshTokenService.deleteRefreshToken(refreshToken);
        }

        // Remove refreshToken from client-side cookies
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(isCookieSecure) // use this in production
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new APIResponse<>(true, "Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<AuthResponse>> createAccessToken(HttpServletRequest request) {
        String refreshToken = null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new APIResponse<>(false, "Refresh token is missing", null));
        }

        try {
            RefreshToken oldRefreshToken = this.refreshTokenService.verifyRefreshToken(refreshToken);
            User user = oldRefreshToken.getUser();
            String accessToken = jwtUtil.generateToken(user.getEmail());

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .build();

            return ResponseEntity.ok()
                    .body(new APIResponse<>(true, "Token refreshed successfully", authResponse));

        } catch (TokenExpiredException ex) {
            this.refreshTokenService.deleteRefreshToken(refreshToken);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new APIResponse<>(false, "Refresh token expired", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new APIResponse<>(false, "Invalid refresh token", null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<APIResponse<String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.debug("Received forgot password request for email: {}", email);
        this.passwordResetService.generateOtp(email);
        return ResponseEntity.ok(new APIResponse<>(true, "Password reset instructions have been sent to your email."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<APIResponse<String>> verifyOtp(@RequestBody VerifyOtpDTO verifyOtpDTO) {
        this.passwordResetService.verifyOtp(verifyOtpDTO);
        return ResponseEntity.ok(new APIResponse<>(true, "OTP verified."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<APIResponse<String>> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        this.passwordResetService.resetPassword(resetPasswordDTO);
        return ResponseEntity.ok(new APIResponse<>(true, "Password reset successful."));
    }

    @PostMapping("/activate")
    public ResponseEntity<APIResponse<Void>> activateUser(@RequestBody AuthRequest authRequest) {
        this.userService.activateUserAccount(authRequest);
        return ResponseEntity.ok(new APIResponse<>(true, "User activated successfully"));
    }
}
