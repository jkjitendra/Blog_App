package com.jk.blog.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.profile.ProfileRequestBody;
import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.service.FileService;
import com.jk.blog.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;
    @Autowired
    private FileService fileService;
    @Value("${project.files}")
    private String path;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponseBody> getProfileByUserId(@PathVariable Long userId) {
        ProfileResponseBody profileResponseBody = this.profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profileResponseBody);
    }

    @PutMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponseBody> updateProfile(@PathVariable Long userId,
                                                             @Valid @RequestPart("profile") String profileRequestBody,
                                                             @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        ProfileRequestBody profileJSON = new ObjectMapper().readValue(profileRequestBody, ProfileRequestBody.class);
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileService.uploadImage(path, image);
            profileJSON.setImageUrl(imageUrl);
        }
        ProfileResponseBody profileResponseBody = this.profileService.updateProfile(profileJSON, userId);
        return ResponseEntity.ok(profileResponseBody);
    }

    @PatchMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> patchProfile(@PathVariable Long userId,
                                          @RequestPart("profile") String updatesJson,
                                          @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        Map<String, Object> updates = new ObjectMapper().readValue(updatesJson, new TypeReference<>() {});
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileService.uploadImage(path, image);
            updates.put("imageUrl", imageUrl);
        }
        ProfileResponseBody profileResponseBody = profileService.patchProfile(updates, userId);
        return ResponseEntity.ok(profileResponseBody);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long userId) {
        this.profileService.deleteProfile(userId);
        return new ResponseEntity<>(new APIResponse(true, "Profile Deleted Successfully"), HttpStatus.OK);
    }
}
