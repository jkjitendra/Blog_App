package com.jk.blog.service;

import com.jk.blog.dto.UserRequestBody;
import com.jk.blog.dto.UserResponseBody;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface UserService {

    UserResponseBody createUser(UserRequestBody user);
    UserResponseBody updateUser(UserRequestBody user, Long userId);
    UserResponseBody getUserById(Long userId);
    List<UserResponseBody> getAllUsers();
    void deleteUser(Long userId);
    boolean checkUsernameAvailability(String username);
//    void initiatePasswordReset(String email);
//    void verifyAndResetPassword(String token, String newPassword, String email);

}
