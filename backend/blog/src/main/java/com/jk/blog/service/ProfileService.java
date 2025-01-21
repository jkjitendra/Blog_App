package com.jk.blog.service;

import com.jk.blog.dto.profile.ProfileRequestBody;
import com.jk.blog.dto.profile.ProfileResponseBody;

import java.util.Map;

public interface ProfileService {

    ProfileResponseBody getProfileByUserId(Long userId);

    ProfileResponseBody updateProfile(ProfileRequestBody profileRequestBody, Long userId);

    ProfileResponseBody patchProfile(Map<String, Object> updates, Long userId);

    void deleteProfile(Long userId);
}
