package com.jk.blog.dto.user;

import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.entity.Role;
import com.jk.blog.entity.User;
import com.jk.blog.exception.FieldUpdateNotAllowedException;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    // to be used if model mapper is not being used
    // Converts UserRequestBody to User entity
    public static User userRequestBodyToUser(UserRequestBody userDTO) {
        if (userDTO == null) {
            throw new FieldUpdateNotAllowedException("UserRequestBody cannot be null");
        }
        User user = new User();
        user.setName(userDTO.getName());
        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        user.setMobile(userDTO.getMobile());
        user.setCountryName(userDTO.getCountryName());
        return user;
    }

    // to be used if model mapper is not being used
    // Converts User entity to UserResponseBody DTO
    public static UserResponseBody userToUserResponseBody(User user) {
        if (user == null) {
            throw new FieldUpdateNotAllowedException("User entity cannot be null");
        }
        UserResponseBody userResponseBody = new UserResponseBody();
        userResponseBody.setId(user.getUserId());
        userResponseBody.setName(user.getName());
        userResponseBody.setUserName(user.getUsername());
        userResponseBody.setEmail(user.getEmail());
        userResponseBody.setMobile(user.getMobile());
        userResponseBody.setCountryName(user.getCountryName());
        userResponseBody.setUserDeleted(user.isUserDeleted());

        if (user.getUserDeletionTimestamp() != null) {
            userResponseBody.setUserDeletionTimestamp(ISO_FORMATTER.format(user.getUserDeletionTimestamp()));
        }
        if (user.getUserCreatedDate() != null) {
            userResponseBody.setUserCreatedDate(ISO_FORMATTER.format(user.getUserCreatedDate()));
        }
        if (user.getUserLastLoggedInDate() != null) {
            userResponseBody.setUserLastLoggedInDate(ISO_FORMATTER.format(user.getUserLastLoggedInDate()));
        }

        if (user.getProfile() != null) {
            userResponseBody.setProfile(convertProfile(user));
        }

        userResponseBody.setRoles(convertRoles(user.getRoles()));

        return userResponseBody;
    }

    // Converts Profile entity to ProfileResponseBody DTO
    private static ProfileResponseBody convertProfile(User user) {
        ProfileResponseBody profileResponseBody = new ProfileResponseBody();
        profileResponseBody.setProfileId(user.getProfile().getProfileId());
        profileResponseBody.setAbout(user.getProfile().getAbout());
        profileResponseBody.setAddress(user.getProfile().getAddress());
        profileResponseBody.setImageUrl(user.getProfile().getImageUrl());
        profileResponseBody.setSocialMediaLinks(user.getProfile().getSocialMediaLinks());
        return profileResponseBody;
    }

    // Converts roles into a safer string set for API responses
    private static Set<String> convertRoles(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

}
