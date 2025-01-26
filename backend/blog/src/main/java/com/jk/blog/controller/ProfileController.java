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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * Fetch the profile of a specific user.
     * Only accessible by the owner of the profile.
     */
    @PreAuthorize("authentication.principal.id == #userId")
    @GetMapping("/user/{userId}")
    public ResponseEntity<APIResponse<ProfileResponseBody>> getProfileByUserId(@PathVariable Long userId) {
        ProfileResponseBody profileResponseBody = this.profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(new APIResponse<>(true, "Profile fetched successfully", profileResponseBody));
    }

    /**
     * Update the profile of a specific user.
     * Only accessible by the owner of the profile.
     */
    @PreAuthorize("authentication.principal.id == #userId")
    @PutMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ProfileResponseBody>> updateProfile(@PathVariable Long userId,
                                                             @Valid @RequestPart("profile") String profileRequestBody,
                                                             @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        ProfileRequestBody profileJSON = new ObjectMapper().readValue(profileRequestBody, ProfileRequestBody.class);
//        if (image != null && !image.isEmpty()) {
//            String imageUrl = fileService.uploadImage(path, image);
//            profileJSON.setImageUrl(imageUrl);
//        }
        ProfileResponseBody profileResponseBody = this.profileService.updateProfile(profileJSON, userId, image);
        return ResponseEntity.ok(new APIResponse<>(true, "Profile updated successfully", profileResponseBody));
    }

    /**
     * Patch update the profile of a specific user.
     * Only accessible by the owner of the profile.
     */
    @PreAuthorize("authentication.principal.id == #userId")
    @PatchMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ProfileResponseBody>> patchProfile(@PathVariable Long userId,
                                          @RequestPart("profile") String updatesJson,
                                          @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        Map<String, Object> updates = new ObjectMapper().readValue(updatesJson, new TypeReference<>() {});
        ProfileResponseBody profileResponseBody = profileService.patchProfile(updates, userId, image);
        return ResponseEntity.ok(new APIResponse<>(true, "Profile patched successfully", profileResponseBody));
    }

    /**
     * Delete the profile of a specific user.
     * Only accessible by the owner of the profile.
     */
    @PreAuthorize("authentication.principal.id == #userId")
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<APIResponse<String>> deleteProfile(@PathVariable Long userId) {
        this.profileService.deleteProfile(userId);
        return ResponseEntity.ok(new APIResponse<>(true, "Profile deleted successfully"));
    }
}
