package com.jk.blog.service;

import com.jk.blog.dto.AuthDTO.ResetPasswordDTO;
import com.jk.blog.dto.AuthDTO.VerifyOtpDTO;

public interface PasswordResetService {

    void generateOtp(String email);

    void verifyOtp(VerifyOtpDTO verifyOtpDTO);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);
}
