package com.jk.blog.service.impl;


import com.jk.blog.dto.profile.ProfileRequestBody;
import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.entity.Profile;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.ProfileRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.ProfileService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

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

//    @Override
//    public ProfileResponseBody createProfile(ProfileRequestBody requestBody, Long userId) {
//        User user = this.userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
//        Profile profile = this.modelMapper.map(requestBody, Profile.class);
//        profile.setUser(user);
//        Profile savedProfile = this.profileRepository.save(profile);
//        return this.modelMapper.map(savedProfile, ProfileResponseBody.class);
//    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseBody getProfileByUserId(Long userId) {
        Profile profile = this.profileRepository
                              .findByUser_UserId(userId)
                              .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
        return this.modelMapper.map(profile, ProfileResponseBody.class);
    }

    @Override
    @Transactional
    public ProfileResponseBody updateProfile(ProfileRequestBody requestBody, Long userId) {
        Profile existingProfile = this.profileRepository
                                      .findByUser_UserId(userId)
                                      .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

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
//        this.modelMapper.map(requestBody, existingProfile);
        Profile updatedProfile = this.profileRepository.save(existingProfile);
        return this.modelMapper.map(updatedProfile, ProfileResponseBody.class);
    }

    @Override
    @Transactional
    public ProfileResponseBody patchProfile(Map<String, Object> updates, Long userId) {
        Profile profile = this.profileRepository
                              .findByUser_UserId(userId)
                              .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        updates.forEach((key, value) -> {
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
        Profile profile = this.profileRepository
                              .findByUser_UserId(userId)
                              .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
        this.profileRepository.delete(profile);
    }
}
