package com.jk.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserRequestBody {
    private Long id;
    @NotEmpty
    @Size(min = 4, message = "name should be greater than 4")
    private String name;
    @NotEmpty
    @Size(min = 4, message = "username should be greater than 4")
    private String userName;
    @Email(message = "Please enter valid email")
    private String email;
    @NotEmpty
    @Size(min = 6, message = "Password must be greater than 6")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password should be combination of numbers, lowercase, uppercase, special Characters")
    private String password;
}
