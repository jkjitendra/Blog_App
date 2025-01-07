package com.jk.blog.dto.user;

import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.entity.User;

public class UserMapper {

    // to be used if model mapper is not being used
    public static User userRequestBodyToUser(UserRequestBody userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setMobile(userDTO.getMobile());
        user.setCountryName(userDTO.getCountryName());
        return user;
    }

    // to be used if model mapper is not being used
    public static UserResponseBody userToUserResponseBody(User user) {
        UserResponseBody userResponseBody = new UserResponseBody();
        userResponseBody.setId(user.getUserId());
        userResponseBody.setName(user.getName());
        userResponseBody.setUserName(user.getUsername());
        userResponseBody.setEmail(user.getEmail());
        userResponseBody.setMobile(user.getMobile());
        userResponseBody.setCountryName(user.getCountryName());
        userResponseBody.setUserDeleted(user.isUserDeleted());
        userResponseBody.setUserDeletionTimestamp(String.valueOf(user.getUserDeletionTimestamp()));
        userResponseBody.setUserCreatedDate(String.valueOf(user.getUserCreatedDate()));
        userResponseBody.setUserLastLoggedInDate(String.valueOf(user.getUserLastLoggedInDate()));
        if (user.getProfile() != null) {
            ProfileResponseBody profileResponseBody = new ProfileResponseBody();
            profileResponseBody.setProfileId(user.getProfile().getProfileId());
            profileResponseBody.setAbout(user.getProfile().getAbout());
            profileResponseBody.setAddress(user.getProfile().getAddress());
            profileResponseBody.setImageUrl(user.getProfile().getImageUrl());
            profileResponseBody.setSocialMediaLinks(user.getProfile().getSocialMediaLinks());
            userResponseBody.setProfile(profileResponseBody);
        }
        return userResponseBody;
    }
}
