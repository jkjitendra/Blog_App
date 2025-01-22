package com.jk.blog.service;

import com.jk.blog.dto.user.*;
import com.jk.blog.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponseBody updateUser(UserRequestBody user, Long userId);

    Optional<UserResponseBody> findUserById(Long userId);

    Optional<UserResponseBody> findUserByEmail(String email);

    Optional<UserResponseBody> findUserByUserName(String username);

    List<UserResponseBody> getAllUsers();

    UserResponseWithTokenDTO updatePassword(Long id, UpdatePasswordDTO updatePasswordDTO);

    void deleteUser(Long userId);

    User deactivateUserAccount(Long userId);

    UserResponseBody activateUserAccount(String email);

    boolean checkUsernameAvailability(String username);


}
