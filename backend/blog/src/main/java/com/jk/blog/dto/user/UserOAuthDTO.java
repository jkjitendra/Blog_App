package com.jk.blog.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOAuthDTO {

    private String email;
    private String name;
    private String provider;
}
