package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.AuthDTO.AuthRequest;
import com.jk.blog.dto.AuthDTO.AuthResponse;
import com.jk.blog.dto.user.UserCreateRequestBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.service.UserService;
import com.jk.blog.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public APIResponse<UserResponseBody> registerUser(@RequestBody UserCreateRequestBody userRequestDTO) {
        UserResponseBody userResponse = this.userService.createUser(userRequestDTO);
        return new APIResponse<>(true, "User registered successfully", userResponse);
    }

    @PostMapping("/login")
    public APIResponse<AuthResponse> login(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            return new APIResponse<>(false, "Invalid credentials", null, e.getMessage());
        }

        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String jwt = this.jwtUtil.generateToken(userDetails.getUsername());
        return new APIResponse<>(true, "Login successful", new AuthResponse(jwt));
    }
}
