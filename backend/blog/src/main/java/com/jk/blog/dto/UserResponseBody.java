package com.jk.blog.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserResponseBody {

    private Long id;

    private String name;

    private String userName;

    private String email;

    private Boolean active;

    private String mobile;

    private String countryName;

    private String createdDate;

    private String lastLoggedInDate;
}
