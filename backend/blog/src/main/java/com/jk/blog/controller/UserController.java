package com.jk.blog.controller;

import com.jk.blog.dto.UserDTO;
import com.jk.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userRequestBody) {
        UserDTO createdUserDTO = this.userService.createUser(userRequestBody);
        return new ResponseEntity<>(createdUserDTO, HttpStatus.CREATED);
    }
}
