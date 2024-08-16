package com.jk.blog.controller;

import com.jk.blog.dto.*;
import com.jk.blog.dto.user.UserCreateRequestBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.dto.user.UserStatusResponse;
import com.jk.blog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/")
    public ResponseEntity<UserResponseBody> createUser(@Valid @RequestBody UserCreateRequestBody userCreateRequestBody) {
        UserResponseBody createdUserRequestBody = this.userService.createUser(userCreateRequestBody);
        return new ResponseEntity<>(createdUserRequestBody, HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<List<UserResponseBody>> getAllUser() {
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseBody> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(this.userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseBody> updateUser(@Valid @RequestBody UserRequestBody userRequestBody, @PathVariable("userId") Long uId) {
        UserResponseBody updatedUser = this.userService.updateUser(userRequestBody, uId);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<APIResponse> deleteUser(@PathVariable("userId") Long uId) {
        this.userService.deleteUser(uId);
        return new ResponseEntity<>(new APIResponse(true, "User Deleted Successfully"), HttpStatus.OK);
    }

    @PatchMapping(value = "/user/{userId}/deactivate")
    public ResponseEntity<UserStatusResponse> patchUserDeactivate(@PathVariable Long userId) throws IOException{
        UserResponseBody userResponseBody = this.userService.deactivateUserAccount(userId);
        APIResponse apiResponse = new APIResponse(true, "User Deactivated Successfully");
        UserStatusResponse response = new UserStatusResponse(apiResponse, userResponseBody);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/user/{userId}/activate")
    public ResponseEntity<UserStatusResponse> patchUserActivate(@PathVariable Long userId) throws IOException{
        UserResponseBody userResponseBody = this.userService.activateUserAccount(userId);
        APIResponse apiResponse = new APIResponse(true, "User Activated Successfully");
        UserStatusResponse response = new UserStatusResponse(apiResponse, userResponseBody);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = userService.checkUsernameAvailability(username);
        return ResponseEntity.ok(Collections.singletonMap("isAvailable", isAvailable));
    }

//    @PostMapping("/reset-password-request")
//    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
//        try {
//            this.userService.initiatePasswordReset(request.getEmail());
//            return ResponseEntity.ok("Password reset link has been sent to your email.");
//        } catch (ResourceNotFoundException e) {
//            return ResponseEntity.ok("If the email is registered, a password reset link will be sent.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing your request.");
//        }
//    }
//
//    @PostMapping("/reset-password/verify")
//    public ResponseEntity<APIResponse> resetPasswordWithToken(@RequestBody PasswordResetVerificationRequest request) {
//        this.userService.verifyAndResetPassword(request.getToken(), request.getNewPassword(), request.getEmail());
//        return new ResponseEntity<>(new APIResponse("Password reset successfully", true), HttpStatus.OK);
//    }


}
