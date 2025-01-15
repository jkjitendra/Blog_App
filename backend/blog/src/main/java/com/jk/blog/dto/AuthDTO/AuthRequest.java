package com.jk.blog.dto.AuthDTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class AuthRequest {

    @JsonAlias({"email", "username"})
    @NotBlank(message = "Email or Username cannot be blank")
    String login; // Accepts either email or username

    @NotBlank(message = "Password cannot be blank")
    String password;
}