package com.jk.blog.service.impl;

import com.jk.blog.dto.AuthDTO.AuthRequest;
import com.jk.blog.dto.AuthDTO.AuthResponse;
import com.jk.blog.dto.AuthDTO.RegisterRequestBody;
import com.jk.blog.dto.AuthDTO.ResetPasswordDTO;
import com.jk.blog.dto.MailBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.entity.*;
import com.jk.blog.exception.*;
import com.jk.blog.repository.PasswordResetTokenRepository;
import com.jk.blog.repository.RoleRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.AuthService;
import com.jk.blog.service.RefreshTokenService;
import com.jk.blog.utils.GeneratorUtils;
import com.jk.blog.utils.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import static com.jk.blog.constants.AppConstants.EMAIL_OTP_SUBJECT;
import static com.jk.blog.constants.AppConstants.EMAIL_OTP_TEXT;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailServiceImpl emailService;

    @Value("${otp.expiration-time}")
    private Long otpExpirationTime;

    @Override
    @Transactional
    public UserResponseBody registerUser(RegisterRequestBody registerRequestBody) {
        Optional<User> userOptional = userRepository.findByEmail(registerRequestBody.getEmail());
        RoleType roleType;

        try {
            roleType = RoleType.valueOf(registerRequestBody.getRole().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidRoleException("Invalid role: " + registerRequestBody.getRole());
        }

        Role userRole = roleRepository.findByName(roleType.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", registerRequestBody.getRole()));

        if (userOptional.isEmpty()) {
            User user = User.builder()
                    .name(registerRequestBody.getName())
                    .userName(registerRequestBody.getUserName())
                    .email(registerRequestBody.getEmail())
                    .password(passwordEncoder.encode(registerRequestBody.getPassword()))
                    .mobile(registerRequestBody.getMobile())
                    .countryName(registerRequestBody.getCountryName())
                    .userCreatedDate(Instant.now())
                    .roles(new HashSet<>(Collections.singleton(userRole)))
                    .build();
            user.getRoles().add(userRole);
            User savedUser = userRepository.save(user);
            return modelMapper.map(savedUser, UserResponseBody.class);
        } else {
            throw new UserAlreadyExistingException("User", "email", registerRequestBody.getEmail());
        }
    }

    @Override
    public AuthResponse generateAccessToken(AuthRequest authRequest) {
        final User user = userRepository.findByEmail(authRequest.getEmail())
                                        .orElseThrow(() -> new ResourceNotFoundException("User", "email", authRequest.getEmail()));
        final String accessToken = jwtUtil.generateToken(user.getUsername());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .build();
    }


    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        int otp = GeneratorUtils.generateOTP();
        MailBody mailBody = MailBody.builder()
                .to(email)
                .subject(EMAIL_OTP_SUBJECT)
                .text(EMAIL_OTP_TEXT + otp)
                .build();

        PasswordResetToken token = new PasswordResetToken();
        token.setOtp(otp);
        token.setUser(user);
        token.setVerified(false);
        token.setExpirationTime(Instant.now().plusSeconds(otpExpirationTime)); // OTP expires in 5 mins
        passwordResetTokenRepository.save(token);

        try {
            emailService.sendEmail(mailBody);
        } catch (MailException e) {
            throw new EmailSendingException(email, "Failed to send email", e);
        }

    }

    @Override
    @Transactional
    public void verifyOTP(Integer otp, String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new InvalidTokenException("otp", "Invalid OTP Received"));

        if (passwordResetToken.getExpirationTime().isBefore(Instant.now())) {
            passwordResetTokenRepository.deleteById(passwordResetToken.getId());
            throw new TokenExpiredException(String.valueOf(otp), passwordResetToken.getExpirationTime().toString());
        }
        passwordResetToken.setVerified(true);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO, String email) {

        User existingUser = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByUserAndVerified(existingUser)
                .orElseThrow(() -> new InvalidTokenException("token", "No verified password reset token found"));


        if (!Objects.equals(resetPasswordDTO.getNewPassword(), resetPasswordDTO.getRepeatPassword())) {
            throw new PasswordNotMatchException("newPassword", resetPasswordDTO.getNewPassword());
        }

        String encodedPassword = passwordEncoder.encode(resetPasswordDTO.getNewPassword());
        userRepository.updatePassword(email, encodedPassword);

        // Delete the password reset token after successful password reset
        passwordResetTokenRepository.deleteByUser(existingUser);
    }


}
