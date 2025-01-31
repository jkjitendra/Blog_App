package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.user.UpdatePasswordDTO;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.dto.user.UserResponseWithTokenDTO;
import com.jk.blog.security.AuthenticationFacade;
import com.jk.blog.service.UserService;
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
public class UserController {

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
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<UserResponseBody>> updateUser(@PathVariable Long id,
                                                                    @Valid @RequestBody UserRequestBody userRequestDTO) {
        UserResponseBody updatedUser = userService.updateUser(userRequestDTO, id);
        return ResponseEntity.ok(new APIResponse<>(true, "User updated successfully", updatedUser));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/update-password")
    public ResponseEntity<APIResponse<UserResponseWithTokenDTO>> updatePassword(@PathVariable Long id,
                                                                                @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        UserResponseWithTokenDTO userResponseDTO = userService.updatePassword(id, updatePasswordDTO);
        return ResponseEntity.ok(new APIResponse<>(true, "Password updated successfully", userResponseDTO));
    }

    @PreAuthorize("isAuthenticated() or hasAuthority('USER_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new APIResponse<>(true, "User deleted successfully"));
    }

    @PreAuthorize("isAuthenticated() or hasAuthority('USER_MANAGE')")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<APIResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUserAccount(id);
        return ResponseEntity.ok(new APIResponse<>(true, "User deactivated successfully"));
    }

    @PostMapping("/{email}/activate")
    public ResponseEntity<APIResponse<Void>> activateUser(@PathVariable String email) {
        userService.activateUserAccount(email);
        return ResponseEntity.ok(new APIResponse<>(true, "User activated successfully"));
    }

}
