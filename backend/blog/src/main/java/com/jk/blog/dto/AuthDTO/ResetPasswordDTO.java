package com.jk.blog.dto.AuthDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO {

    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "New Password is Required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{6,}$",
            message = "Password Must Be at least 6 Characters long and include at least one UPPERCASE letter, one lowercase letter, one Number, and one Special Character")
    private String newPassword;

    @NotBlank(message = "Repeat Password is Required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{6,}$",
            message = "Password Must Be at least 6 Characters long and include at least one UPPERCASE letter, one lowercase letter, one Number, and one Special Character")
    private String repeatPassword;
}
