package com.jk.blog.service;

import com.jk.blog.dto.user.UserCreateRequestBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;

import java.util.List;

public interface UserService {

    UserResponseBody createUser(UserCreateRequestBody user);

    UserResponseBody updateUser(UserRequestBody user, Long userId);

    UserResponseBody getUserById(Long userId);

    List<UserResponseBody> getAllUsers();

    void deleteUser(Long userId);

    UserResponseBody deactivateUserAccount(Long userId);

    UserResponseBody activateUserAccount(Long userId);

    boolean checkUsernameAvailability(String username);

//    void initiatePasswordReset(String email);
//    void verifyAndResetPassword(String token, String newPassword, String email);

}
