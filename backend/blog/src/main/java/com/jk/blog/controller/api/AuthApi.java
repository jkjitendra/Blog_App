package com.jk.blog.controller.api;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.AuthDTO.*;
import com.jk.blog.dto.user.UserResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface AuthApi {

    @Operation(summary = "Register a new user", description = "Registers a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))

    })
    public ResponseEntity<APIResponse<UserResponseBody>> registerUser(@Valid @RequestBody RegisterRequestBody registerRequestBody);


    @Operation(summary = "User login", description = "Authenticates a user and returns access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    public ResponseEntity<APIResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) throws Exception;


    @Operation(summary = "User logout", description = "Logs out a user by deleting the refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    public ResponseEntity<APIResponse<?>> logout(HttpServletRequest request, HttpServletResponse response);


    @Operation(summary = "Refresh access token", description = "Generates a new access token using the refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token is missing or invalid",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    public ResponseEntity<APIResponse<AuthResponse>> createAccessToken(HttpServletRequest request);


    @Operation(summary = "Forgot password", description = "Initiates the password reset process by sending an OTP to the user's email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset instructions have been sent to your email",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email provided",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    public ResponseEntity<APIResponse<String>> forgotPassword(@RequestBody Map<String, String> request);


    @Operation(summary = "Verify OTP", description = "Verifies the OTP sent to the user's email for password reset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid OTP provided",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    public ResponseEntity<APIResponse<String>> verifyOtp(@RequestBody VerifyOtpDTO verifyOtpDTO);


    @Operation(summary = "Reset password", description = "Resets the user's password using the verified OTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    public ResponseEntity<APIResponse<String>> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO);


    @Operation(summary = "Activate User Account", description = "Activates User Account by Verifying credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account Activated successful",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    public ResponseEntity<APIResponse<Void>> activateUser(@RequestBody AuthRequest authRequest);
}
