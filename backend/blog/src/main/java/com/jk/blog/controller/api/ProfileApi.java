package com.jk.blog.controller.api;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.profile.ProfileResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfileApi {

    @Operation(summary = "Fetch User Profile", description = "Retrieves the profile details of the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Profile fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<ProfileResponseBody>> getUsersProfile();

    @Operation(summary = "Update User Profile", description = "Allows the authenticated user to update their profile.")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid profile data provided")
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<ProfileResponseBody>> updateUsersProfile(
            @Valid @RequestPart("profile") String profileRequestBody,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException;

    @Operation(summary = "Patch Update Profile", description = "Partially updates the authenticated user's profile.")
    @ApiResponse(responseCode = "200", description = "Profile patched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid update parameters provided")
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<ProfileResponseBody>> patchUsersProfile(
            @RequestPart("profile") String updatesJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException;

    @Operation(summary = "Delete User Profile", description = "Deletes the authenticated user's profile.")
    @ApiResponse(responseCode = "200", description = "Profile deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<String>> deleteUsersProfile();

}
