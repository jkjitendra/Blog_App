package com.jk.blog.service;

import com.jk.blog.dto.AuthDTO.AuthRequest;
import com.jk.blog.dto.AuthDTO.AuthResponse;
import com.jk.blog.dto.AuthDTO.RegisterRequestBody;
import com.jk.blog.dto.AuthDTO.ResetPasswordDTO;
import com.jk.blog.dto.user.UserResponseBody;

public interface AuthService {

    UserResponseBody registerUser(RegisterRequestBody registerRequestBody);

    AuthResponse generateAccessToken(AuthRequest authRequest);

    void forgotPassword(String email);

    void verifyOTP(Integer otp, String email);

    void resetPassword(ResetPasswordDTO resetPasswordDTO, String email);
}
