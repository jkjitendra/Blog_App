package com.jk.blog.service;

import com.jk.blog.dto.user.*;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponseBody updateUser(UserRequestBody user, Long userId);

    Optional<UserResponseBody> findUserById(Long userId);

    UserResponseWithTokenDTO updatePassword(Long id, PasswordUpdateDTO passwordUpdateDTO);

    void deleteUser(Long userId);

    UserResponseBody deactivateUserAccount(Long userId);

    UserResponseBody activateUserAccount(Long userId);

    boolean checkUsernameAvailability(String username);


}
