package com.jk.blog.dto.AuthDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestBody {

    @NotEmpty
    @Size(min = 4, message = "Name Should Be Greater Than 4")
    private String name;

    @NotEmpty
    @Size(min = 4, message = "Username Should Be Greater Than 4")
    private String userName;

    @Email(message = "Please Enter Valid Email")
    private String email;

    @NotEmpty(message = "Password is Required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{8,}$",
            message = "Password Must Be at least 8 Characters long and include at least one UPPERCASE letter, one lowercase letter, one Number, and one Special Character")
    private String password;

    @NotEmpty(message = "Mobile number must not be empty")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid Mobile Number Format")
    private String mobile;

    @NotEmpty(message = "Country Name Must Not Be Empty")
    private String countryName;

    @Pattern(regexp = "^(SUBSCRIBER|USUAL|ADMIN|MODERATE)$", message = "role can be either SUBSCRIBER, USUAL, ADMIN, MODERATE")
    private String role;  // Role can be "SUBSCRIBER", "USUAL", "ADMIN"
}
