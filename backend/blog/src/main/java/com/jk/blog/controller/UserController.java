package com.jk.blog.controller;

import com.jk.blog.constants.SecurityConstants;
import com.jk.blog.controller.api.UserApi;
import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.user.UpdatePasswordRequestBody;
import com.jk.blog.dto.user.UpdatePasswordResponseBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.security.AuthenticationFacade;
import com.jk.blog.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_NAME)
@Tag(name = "User Management", description = "APIs for managing users and their details")
public class UserController implements UserApi {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<APIResponse<UserResponseBody>> getOwnDetails() {
        Long userId = authenticationFacade.getAuthenticatedUserId();
        Optional<UserResponseBody> user = userService.findUserById(userId);

        return user.map(responseBody ->
                        ResponseEntity.ok(new APIResponse<>(true, "User fetched successfully", responseBody)))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new APIResponse<>(false, "User not found")));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping("/{email}")
    public ResponseEntity<APIResponse<UserResponseBody>> getUserByEmail(@PathVariable String email) {
        Optional<UserResponseBody> user = userService.findUserByEmail(email);

        return user.map(responseBody ->
                        ResponseEntity.ok(new APIResponse<>(true, "User fetched successfully", responseBody)))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new APIResponse<>(false, "User not found")));
    }

    /**
     * ✅ Admins can fetch a list of all users.
     */
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping("/all")
    public ResponseEntity<APIResponse<List<UserResponseBody>>> getAllUsers() {
        List<UserResponseBody> users = userService.getAllUsers();
        return ResponseEntity.ok(new APIResponse<>(true, "All users fetched successfully", users));
    }

    /**
     * ✅ Users can update their own details.
     * ✅ Admins can update any user's details.
     */
    @PreAuthorize("isAuthenticated() or hasAuthority('USER_MANAGE')")
    @PutMapping("/update-user")
    public ResponseEntity<APIResponse<UserResponseBody>> updateUser(
            @Valid @RequestBody UserRequestBody userRequestDTO
    ) {
        UserResponseBody updatedUser = userService.updateUser(userRequestDTO);
        return ResponseEntity.ok(new APIResponse<>(true, "User updated successfully", updatedUser));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update-password")
    public ResponseEntity<APIResponse<UpdatePasswordResponseBody>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequestBody updatePasswordRequestBody
    ) {
        UpdatePasswordResponseBody userResponseDTO = userService.updatePassword(updatePasswordRequestBody);
        return ResponseEntity.ok(new APIResponse<>(true, "Password updated successfully", userResponseDTO));
    }

    @PreAuthorize("isAuthenticated() or hasAuthority('USER_MANAGE')")
    @DeleteMapping("/")
    public ResponseEntity<APIResponse<Void>> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.ok(new APIResponse<>(true, "User deleted successfully"));
    }

    @PreAuthorize("isAuthenticated() or hasAuthority('USER_MANAGE')")
    @PostMapping("/deactivate")
    public ResponseEntity<APIResponse<Void>> deactivateUser() {
        userService.deactivateUserAccount();
        return ResponseEntity.ok(new APIResponse<>(true, "User deactivated successfully"));
    }

//    @PostMapping("/activate")
//    public ResponseEntity<APIResponse<Void>> activateUser(@RequestBody AuthRequest authRequest) {
//        userService.activateUserAccount(authRequest);
//        return ResponseEntity.ok(new APIResponse<>(true, "User activated successfully"));
//    }

}
