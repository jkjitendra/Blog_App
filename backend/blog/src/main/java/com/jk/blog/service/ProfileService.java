package com.jk.blog.service;

import com.jk.blog.dto.profile.ProfileRequestBody;
import com.jk.blog.dto.profile.ProfileResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ProfileService {

    ProfileResponseBody getProfileByUserId(Long userId);

    ProfileResponseBody updateProfile(ProfileRequestBody profileRequestBody, Long userId, MultipartFile image) throws IOException;

    ProfileResponseBody patchProfile(Map<String, Object> updates, Long userId, MultipartFile image) throws IOException;

    void deleteProfile(Long userId);
}
