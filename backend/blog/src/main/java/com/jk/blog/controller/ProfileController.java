package com.jk.blog.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.constants.SecurityConstants;
import com.jk.blog.controller.api.ProfileApi;
import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.profile.ProfileRequestBody;
import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.security.AuthenticationFacade;
import com.jk.blog.service.ProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/profiles")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_NAME)
@Tag(name = "Profile Management", description = "APIs for managing user profiles")
public class ProfileController implements ProfileApi {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    /**
     * Fetch the profile of a specific user.
     * Only accessible by the owner of the profile.
     */
    @GetMapping("/user")
    public ResponseEntity<APIResponse<ProfileResponseBody>> getUsersProfile() {
        Long userId = authenticationFacade.getAuthenticatedUserId();
        ProfileResponseBody profileResponseBody = this.profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(new APIResponse<>(true, "Profile fetched successfully", profileResponseBody));
    }

    /**
     * Update the profile of a specific user.
     * Only accessible by the owner of the profile.
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ProfileResponseBody>> updateUsersProfile(
                                                             @Valid @RequestPart("profile") String profileRequestBody,
                                                             @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        ProfileRequestBody profileJSON = new ObjectMapper().readValue(profileRequestBody, ProfileRequestBody.class);
        Long userId = authenticationFacade.getAuthenticatedUserId();

        ProfileResponseBody profileResponseBody = this.profileService.updateProfile(profileJSON, userId, image);
        return ResponseEntity.ok(new APIResponse<>(true, "Profile updated successfully", profileResponseBody));
    }

    /**
     * Patch update the profile of a specific user.
     * Only accessible by the owner of the profile.
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping(value = "/user/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ProfileResponseBody>> patchUsersProfile(
                                          @RequestPart("profile") String updatesJson,
                                          @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        Map<String, Object> updates = new ObjectMapper().readValue(updatesJson, new TypeReference<>() {});
        Long userId = authenticationFacade.getAuthenticatedUserId();

        ProfileResponseBody profileResponseBody = profileService.patchProfile(updates, userId, image);
        return ResponseEntity.ok(new APIResponse<>(true, "Profile patched successfully", profileResponseBody));
    }

    /**
     * Delete the profile of a specific user.
     * Only accessible by the owner of the profile.
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/user")
    public ResponseEntity<APIResponse<String>> deleteUsersProfile() {
        Long userId = authenticationFacade.getAuthenticatedUserId();
        this.profileService.deleteProfile(userId);
        return ResponseEntity.ok(new APIResponse<>(true, "Profile deleted successfully"));
    }
}
