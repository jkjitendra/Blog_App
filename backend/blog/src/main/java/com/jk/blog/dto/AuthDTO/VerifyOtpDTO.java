package com.jk.blog.dto.AuthDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpDTO {

    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotNull(message = "OTP cannot be null")
    private Integer otp;
}
