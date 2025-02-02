package com.jk.blog.controller.api;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.user.UpdatePasswordRequestBody;
import com.jk.blog.dto.user.UpdatePasswordResponseBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface UserApi {

    @Operation(summary = "Fetch Authenticated User Details", description = "Retrieves details of the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "User details fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<UserResponseBody>> getOwnDetails();

    @Operation(summary = "Fetch User by Email", description = "Retrieves details of a user using their email. Requires admin privileges.")
    @ApiResponse(responseCode = "200", description = "User details fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<APIResponse<UserResponseBody>> getUserByEmail(@PathVariable String email);

    @Operation(summary = "Fetch All Users", description = "Retrieves a list of all registered users. Requires admin privileges.")
    @ApiResponse(responseCode = "200", description = "All users fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    ResponseEntity<APIResponse<List<UserResponseBody>>> getAllUsers();

    @Operation(summary = "Update User Details", description = "Allows authenticated users to update their details.")
    @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid user data provided")
    ResponseEntity<APIResponse<UserResponseBody>> updateUser(@RequestBody UserRequestBody userRequestDTO);

    @Operation(summary = "Update Password", description = "Allows authenticated users to update their password.")
    @ApiResponse(responseCode = "200", description = "Password updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<UpdatePasswordResponseBody>> updatePassword(@RequestBody UpdatePasswordRequestBody updatePasswordRequestBody);

    @Operation(summary = "Delete User", description = "Deletes the authenticated user's account.")
    @ApiResponse(responseCode = "200", description = "User deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<Void>> deleteUser();

    @Operation(summary = "Deactivate User Account", description = "Allows authenticated users to deactivate their account.")
    @ApiResponse(responseCode = "200", description = "User deactivated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not logged in")
    ResponseEntity<APIResponse<Void>> deactivateUser();

}
