package com.jk.blog.service;

import com.jk.blog.dto.ProfileRequestBody;
import com.jk.blog.dto.ProfileResponseBody;

import java.util.List;
import java.util.Map;

public interface ProfileService {

//    ProfileResponseBody createProfile(ProfileRequestBody profileRequestBody, Long userId);
    ProfileResponseBody getProfileByUserId(Long userId);

    ProfileResponseBody updateProfile(ProfileRequestBody profileRequestBody, Long userId);

    ProfileResponseBody patchProfile(Map<String, Object> updates, Long userId);

    void deleteProfile(Long userId);
}
