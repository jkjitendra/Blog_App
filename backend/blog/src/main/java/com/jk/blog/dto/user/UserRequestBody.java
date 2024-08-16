package com.jk.blog.dto.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserRequestBody {

    @NotBlank
    @Size(min = 4, message = "Name Should Be Greater Than 4")
    private String name;

    @NotBlank
    @Size(min = 4, message = "Username Should Be Greater Than 4")
    private String userName;

    @NotBlank
    @Email(message = "Please Enter Valid Email")
    private String email;

    @NotBlank(message = "Mobile number must not be empty")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid Mobile Number Format")
    private String mobile;

    @NotBlank(message = "Country Name Must Not Be Empty")
    private String countryName;
}
