package com.jk.blog.service;

import com.jk.blog.dto.UserRequestBody;
import com.jk.blog.dto.UserResponseBody;

import java.util.List;

public interface UserService {

    UserResponseBody createUser(UserRequestBody user);
    UserResponseBody updateUser(UserRequestBody user, Long userId);
    UserResponseBody getUserById(Long userId);
    List<UserResponseBody> getAllUsers();
    void deleteUser(Long userId);
}
