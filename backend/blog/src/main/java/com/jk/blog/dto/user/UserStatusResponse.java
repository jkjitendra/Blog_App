package com.jk.blog.dto.user;

import com.jk.blog.dto.APIResponse;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserStatusResponse {

    private APIResponse message;
    private UserResponseBody user;
}
