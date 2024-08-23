package com.jk.blog.dto.AuthDTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    String email;

    @NotBlank(message = "Password cannot be blank")
    String password;

}

