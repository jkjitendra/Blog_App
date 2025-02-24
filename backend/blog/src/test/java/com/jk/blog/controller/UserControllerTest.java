package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.user.UpdatePasswordRequestBody;
import com.jk.blog.dto.user.UpdatePasswordResponseBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.security.AuthenticationFacade;
import com.jk.blog.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private UserController userController;

    private UserResponseBody userResponseBody;

    @BeforeEach
    void setUp() {
        userResponseBody = new UserResponseBody();
        userResponseBody.setEmail("test@example.com");
        userResponseBody.setId(1L);
    }

    @Test
    void test_getOwnDetails_WhenUserExists_returnUserDetails() {
        when(authenticationFacade.getAuthenticatedUserId()).thenReturn(1L);
        when(userService.findUserById(1L)).thenReturn(Optional.of(userResponseBody));

        ResponseEntity<APIResponse<UserResponseBody>> response = userController.getOwnDetails();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("User fetched successfully", response.getBody().getMessage());
    }

    @Test
    void test_getOwnDetails_WhenUserDoesNotExist_returnNotFound() {
        when(authenticationFacade.getAuthenticatedUserId()).thenReturn(1L);
        when(userService.findUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<APIResponse<UserResponseBody>> response = userController.getOwnDetails();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void test_getUserByEmail_WhenUserExists_returnUserDetails() {
        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.of(userResponseBody));

        ResponseEntity<APIResponse<UserResponseBody>> response = userController.getUserByEmail("test@example.com");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("User fetched successfully", response.getBody().getMessage());
    }

    @Test
    void test_getUserByEmail_WhenUserDoesNotExist_returnNotFound() {
        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<APIResponse<UserResponseBody>> response = userController.getUserByEmail("test@example.com");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void test_getAllUsers_WhenUsersExist_returnUserList() {
        List<UserResponseBody> users = List.of(userResponseBody);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<APIResponse<List<UserResponseBody>>> response = userController.getAllUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("All users fetched successfully", response.getBody().getMessage());
    }

    @Test
    void test_updateUser_WhenValidRequest_returnUpdatedUser() {
        UserRequestBody userRequestBody = new UserRequestBody();
        when(userService.updateUser(any())).thenReturn(userResponseBody);

        ResponseEntity<APIResponse<UserResponseBody>> response = userController.updateUser(userRequestBody);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("User updated successfully", response.getBody().getMessage());
    }

    @Test
    void test_updatePassword_WhenValidRequest_returnSuccess() {
        UpdatePasswordRequestBody requestBody = new UpdatePasswordRequestBody();
        UpdatePasswordResponseBody responseBody = new UpdatePasswordResponseBody();
        when(userService.updatePassword(any())).thenReturn(responseBody);

        ResponseEntity<APIResponse<UpdatePasswordResponseBody>> response = userController.updatePassword(requestBody);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Password updated successfully", response.getBody().getMessage());
    }

    @Test
    void test_deleteUser_WhenUserExists_returnSuccess() {
        doNothing().when(userService).deleteUser();
        ResponseEntity<APIResponse<Void>> response = userController.deleteUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("User deleted successfully", response.getBody().getMessage());
    }

    @Test
    void test_deactivateUser_WhenUserExists_returnSuccess() {
        when(userService.deactivateUserAccount()).thenReturn(null);
        ResponseEntity<APIResponse<Void>> response = userController.deactivateUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("User deactivated successfully", response.getBody().getMessage());
    }
}
