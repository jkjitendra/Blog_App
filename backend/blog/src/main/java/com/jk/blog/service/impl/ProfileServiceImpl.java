package com.jk.blog.service.impl;


import com.jk.blog.dto.profile.ProfileRequestBody;
import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.entity.Profile;
import com.jk.blog.entity.User;
import com.jk.blog.exception.FieldUpdateNotAllowedException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.ProfileRepository;
import com.jk.blog.repository.UserRepository;
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
import java.util.Map;


@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

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

        if (image != null && !image.isEmpty()) {
            String imageUrl = this.fileService.uploadImage(profileBucketPath + "/images_file/", image);
            existingProfile.setImageUrl(imageUrl);
        }

        if (requestBody.getAddress() != null) {
            existingProfile.setAddress(requestBody.getAddress());
        }
        if (requestBody.getAbout() != null) {
            existingProfile.setAbout(requestBody.getAbout());
        }
        if (requestBody.getImageUrl() != null) {
            existingProfile.setImageUrl(requestBody.getImageUrl());
        }
        if (requestBody.getSocialMediaLinks() != null) {
            existingProfile.setSocialMediaLinks(requestBody.getSocialMediaLinks());
        }

        Profile updatedProfile = this.profileRepository.save(existingProfile);
        return this.modelMapper.map(updatedProfile, ProfileResponseBody.class);
    }

    @Override
    @Transactional
    public ProfileResponseBody patchProfile(Map<String, Object> updates, Long userId, MultipartFile image)  throws IOException {
        validateProfileOwnership(userId, "patch");

        Profile profile = fetchProfileByUserId(userId);

        if (image != null && !image.isEmpty()) {
            String imageUrl = this.fileService.uploadImage(profileBucketPath + "/images_file/", image);
            updates.put("imageUrl", imageUrl);
        }

        updates.forEach((key, value) -> {
            if ("profileId".equals(key) || "userId".equals(key)) {
                throw new FieldUpdateNotAllowedException("Updating the field '" + key + "' is not allowed");
            }
            Field field = ReflectionUtils.findField(Profile.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, profile, value);
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

    /**
     * Fetches the Profile entity associated with a specific user ID.
     * @param userId The ID of the user whose profile needs to be fetched.
     * @return The Profile entity associated with the given user ID.
     * @throws ResourceNotFoundException if the profile does not exist.
     */
    private Profile fetchProfileByUserId(Long userId) {
        return this.profileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
    }
}
