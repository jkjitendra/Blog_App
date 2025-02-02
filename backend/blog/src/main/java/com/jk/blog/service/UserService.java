package com.jk.blog.service;

import com.jk.blog.dto.AuthDTO.AuthRequest;
import com.jk.blog.dto.user.*;
import com.jk.blog.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponseBody updateUser(UserRequestBody user);

    Optional<UserResponseBody> findUserById(Long userId);

    Optional<UserResponseBody> findUserByEmail(String email);

    Optional<UserResponseBody> findUserByUserName(String username);

    List<UserResponseBody> getAllUsers();

    UpdatePasswordResponseBody updatePassword(UpdatePasswordRequestBody updatePasswordRequestBody);

    void deleteUser();

    User deactivateUserAccount();

    UserResponseBody activateUserAccount(AuthRequest authRequest);

    boolean checkUsernameAvailability(String username);


}
