package com.jk.blog.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.dto.profile.ProfileRequestBody;
import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.entity.Profile;
import com.jk.blog.entity.User;
import com.jk.blog.exception.FieldUpdateNotAllowedException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.ProfileRepository;
import com.jk.blog.service.FileService;
import com.jk.blog.service.ProfileService;
import com.jk.blog.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    @Qualifier("s3FileService")
    private FileService fileService;

    @Value("${aws.s3.bucket.profile}")
    private String profileBucketPath;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseBody getProfileByUserId(Long userId) {

        Profile existingProfile = fetchProfileByUserId(userId);

        return this.modelMapper.map(existingProfile, ProfileResponseBody.class);
    }

    @Override
    @Transactional
    public ProfileResponseBody updateProfile(ProfileRequestBody requestBody, Long userId, MultipartFile image) throws IOException {
        validateProfileOwnership(userId, "update");

        Profile existingProfile = fetchProfileByUserId(userId);

        existingProfile.setAddress(requestBody.getAddress());
        existingProfile.setAbout(requestBody.getAbout());
        existingProfile.setSocialMediaLinks(requestBody.getSocialMediaLinks());

        // Handle image update
        if (image != null && !image.isEmpty()) {
            String imageUrl = this.fileService.uploadImage(profileBucketPath + "/images_file/", image);
            existingProfile.setImageUrl(imageUrl);
        } else {
            existingProfile.setImageUrl(null);
        }

        Profile updatedProfile = this.profileRepository.save(existingProfile);
        return this.modelMapper.map(updatedProfile, ProfileResponseBody.class);
    }

    @Override
    @Transactional
    public ProfileResponseBody patchProfile(Map<String, Object> updates, Long userId, MultipartFile image)  throws IOException {
        validateProfileOwnership(userId, "patch");

        Profile profile = fetchProfileByUserId(userId);

        // Fix: Convert immutable map to mutable map before modifying
        Map<String, Object> mutableUpdates = (updates == null) ? new HashMap<>() : new HashMap<>(updates);

        if (image != null && !image.isEmpty()) {
            String imageUrl = this.fileService.uploadImage(profileBucketPath + "/images_file/", image);
            mutableUpdates.put("imageUrl", imageUrl);
        }

        mutableUpdates.forEach((key, value) -> {
            if ("profileId".equals(key) || "userId".equals(key)) {
                throw new FieldUpdateNotAllowedException("Updating the field '" + key + "' is not allowed");
            }
            Field field = ReflectionUtils.findField(Profile.class, key);
            if (field != null) {
                field.setAccessible(true);
                // Convert value to the correct field type to avoid ClassCastException
                Object convertedValue = convertValueToFieldType(field, value);
                ReflectionUtils.setField(field, profile, convertedValue);
            }
        });

        Profile updatedProfile = this.profileRepository.save(profile);
        return this.modelMapper.map(updatedProfile, ProfileResponseBody.class);
    }

    @Override
    @Transactional
    public void deleteProfile(Long userId) {
        validateProfileOwnership(userId, "delete");

        Profile profile = fetchProfileByUserId(userId);

        this.profileRepository.delete(profile);
    }

    private void validateProfileOwnership(Long userId, String action) {
        User authenticatedUser = AuthUtil.getAuthenticatedUser();
        if (authenticatedUser == null || !authenticatedUser.getUserId().equals(userId)) {
            throw new UnAuthorizedException("You are not authorized to %s this profile.", action);
        }
    }

    private Profile fetchProfileByUserId(Long userId) {
        return this.profileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
    }

    private Object convertValueToFieldType(Field field, Object value) {
        Class<?> fieldType = field.getType();

        if (value == null) {
            return null;
        }
        if (fieldType == String.class) {
            return value.toString();
        }
        if (fieldType == List.class) {
            // Handle List<String> for `socialMediaLinks`
            if (value instanceof List<?>) {
                return value;
            }

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(value.toString(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid JSON format for socialMediaLinks", e);
            }
        }

        return value;
    }


}
