package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.user.PasswordUpdateDTO;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<Optional<UserResponseBody>>> getUserDetails(@PathVariable Long id) {
        String username = authenticationFacade.getAuthenticatedUsername();
        Optional<UserResponseBody> user = userService.findUserById(id);
        if (user.isPresent() && user.get().getEmail().equals(username)) {
            return new ResponseEntity<>(new APIResponse<>(true, "User fetched successfully", user), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new APIResponse<>(false, "Access denied"), HttpStatus.FORBIDDEN);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<UserResponseBody>> updateUserDetails(@PathVariable Long id, @Valid @RequestBody UserRequestBody userRequestDTO) {
        String username = authenticationFacade.getAuthenticatedUsername();
        Optional<UserResponseBody> user = userService.findUserById(id);
        if (user.isPresent() && user.get().getEmail().equals(username)) {
            UserResponseBody updatedUser = userService.updateUser(userRequestDTO, id);
            return ResponseEntity.ok(new APIResponse<>(true, "User updated successfully", updatedUser));
        } else {
            return new ResponseEntity<>(new APIResponse<>(false, "Access denied"), HttpStatus.FORBIDDEN);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/update-password")
    public ResponseEntity<APIResponse<UserResponseWithTokenDTO>> updatePassword(@PathVariable Long id, @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        String username = authenticationFacade.getAuthenticatedUsername();
        Optional<UserResponseBody> user = userService.findUserById(id);
        if (user.isPresent() && user.get().getEmail().equals(username)) {
            UserResponseWithTokenDTO userResponseDTO = userService.updatePassword(id, passwordUpdateDTO);
            return ResponseEntity.ok(new APIResponse<>(true, "Password updated successfully", userResponseDTO));
        } else {
            return new ResponseEntity<>(new APIResponse<>(false, "Access denied"), HttpStatus.FORBIDDEN);
        }
    }


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteUser(@PathVariable Long id) {
        String username = authenticationFacade.getAuthenticatedUsername();
        Optional<UserResponseBody> user = userService.findUserById(id);
        if (user.isPresent() && user.get().getEmail().equals(username)) {
            userService.deleteUser(id);
            return ResponseEntity.ok(new APIResponse<>(true, "User deleted successfully"));
        } else {
            return new ResponseEntity<>(new APIResponse<>(false, "Access denied"), HttpStatus.FORBIDDEN);
        }
    }


}
