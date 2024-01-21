package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.UserRequestBody;
import com.jk.blog.dto.UserResponseBody;
import com.jk.blog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/")
    public ResponseEntity<UserResponseBody> createUser(@Valid @RequestBody UserRequestBody userRequestBody) {
        UserResponseBody createdUserRequestBody = this.userService.createUser(userRequestBody);
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
        return new ResponseEntity<>(new APIResponse("User Deleted Successfully", true), HttpStatus.OK);
    }
}
