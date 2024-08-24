package com.jk.blog.service.impl;

import com.jk.blog.dto.user.UserCreateRequestBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.entity.Comment;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    private User user;
    private User user2;
    private User deactivatedUser;
    private UserCreateRequestBody userCreateRequestBody;
    private UserRequestBody userRequestBody;
    private UserResponseBody userResponseBody;


    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUserName("testuser");
        user.setName("Test User");
        user.setEmail("testuser@example.com");
        user.setPassword("Test@123");
        user.setMobile("9876543210");
        user.setCountryName("India");
        user.setUserCreatedDate(Instant.now());

        user2 = new User();
        user2.setUserId(2L);
        user2.setUserName("testuser2");
        user2.setName("Test User2");
        user2.setEmail("testuser2@example.com");
        user2.setPassword("Test2@123");
        user2.setMobile("9876543201");
        user2.setCountryName("India");
        user2.setUserCreatedDate(Instant.now());

        userCreateRequestBody = new UserCreateRequestBody();
        userCreateRequestBody.setUserName("testuser");
        userCreateRequestBody.setName("Test User");
        userCreateRequestBody.setEmail("testuser@example.com");
        userCreateRequestBody.setPassword("Test@123");
        userCreateRequestBody.setMobile("9876543210");
        userCreateRequestBody.setCountryName("India");

        userRequestBody = new UserRequestBody();
        userRequestBody.setName("Updated User");
        userRequestBody.setUserName("updateduser");
        userRequestBody.setEmail("updateduser@example.com");
        userRequestBody.setMobile("9876543120");
        userRequestBody.setCountryName("Russia");

        userResponseBody = new UserResponseBody();
        userResponseBody.setId(1L);
        userResponseBody.setUserName("testuser");
        userResponseBody.setName("Test User");
        userResponseBody.setEmail("testuser@example.com");
        userResponseBody.setMobile("9876543210");
        userResponseBody.setCountryName("India");

        deactivatedUser = new User();
        deactivatedUser.setUserId(12L);
        deactivatedUser.setUserName("testuser");
        deactivatedUser.setName("Test User");
        deactivatedUser.setEmail("testuser@example.com");
        deactivatedUser.setPassword("Test@123");
        deactivatedUser.setMobile("9876543210");
        deactivatedUser.setCountryName("India");
        deactivatedUser.setUserDeleted(true);
        deactivatedUser.setUserDeletionTimestamp(Instant.now().minus(80, ChronoUnit.DAYS));

        Post post1 = new Post();
        post1.setPostId(11L);
        post1.setPostDeleted(true);
        post1.setPostDeletionTimestamp(Instant.now().minus(80, ChronoUnit.DAYS));

        Post post2 = new Post();
        post2.setPostId(12L);
        post2.setPostDeleted(true);
        post2.setPostDeletionTimestamp(Instant.now().minus(80, ChronoUnit.DAYS));

        Comment comment = new Comment();
        comment.setCommentId(3L);
        comment.setCommentDeleted(true);
        comment.setCommentDeletionTimestamp(Instant.now().minus(80, ChronoUnit.DAYS));
        deactivatedUser.setComments(List.of(comment));
        deactivatedUser.setPosts(List.of(post1, post2));

    }


    @Test
    public void test_createUser_Success() {

        when(modelMapper.map(userCreateRequestBody, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserResponseBody.class)).thenReturn(userResponseBody);

        UserResponseBody actualResponseBody = userService.createUser(userCreateRequestBody);

        assertNotNull(actualResponseBody);
        assertEquals(userResponseBody, actualResponseBody);

        // Verify that UserRepository.save() is called once
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void test_createUser_ThrowsInvalidCountryNameException() {

        userCreateRequestBody.setMobile("987654321");
        userCreateRequestBody.setCountryName("Ind");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userCreateRequestBody);
        });

        String expectedMessage = "Invalid Country Name or Country Name Not Found.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        // Verify that UserRepository.save() is never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void test_createUser_ThrowsInvalidMobileNumberParseException() {

        userCreateRequestBody.setMobile(null);
        userCreateRequestBody.setCountryName("United States");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userCreateRequestBody);
        });

        String expectedMessage = "Invalid Mobile Number Format or Region Code";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        // Verify that UserRepository.save() is never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void test_createUser_ThrowsInvalidMobileNumberFormatException() {

        userCreateRequestBody.setMobile("123456");
        userCreateRequestBody.setCountryName("United States");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userCreateRequestBody);
        });

        String expectedMessage = "Invalid Mobile Number Format";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        // Verify that UserRepository.save() is never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void test_getUserById_Success() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(modelMapper.map(user, UserResponseBody.class)).thenReturn(userResponseBody);

        UserResponseBody actualResponseBody = userService.getUserById(1L);

        assertNotNull(actualResponseBody);
        assertEquals(userResponseBody, actualResponseBody);

    }

    @Test
    public void test_getUserById_UserNotFoundException() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(1234L);
        });

        String expectedMessage = "User not found with userId : 1234";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void test_getAllUsers_Success() {
        List<User> mockUsers = Arrays.asList(user, user2);

        UserResponseBody firstUserResponseBody = new UserResponseBody();
        firstUserResponseBody.setId(1L);
        UserResponseBody secondUserResponseBody = new UserResponseBody();
        secondUserResponseBody.setId(2L);

        when(userRepository.findAll()).thenReturn(mockUsers);
        when(modelMapper.map(user, UserResponseBody.class)).thenReturn(firstUserResponseBody);
        when(modelMapper.map(user2, UserResponseBody.class)).thenReturn(secondUserResponseBody);

        List<UserResponseBody> actualResponseBodyList = userService.getAllUsers();

        verify(userRepository, times(1)).findAll();
        assertEquals(2, actualResponseBodyList.size());
        assertEquals(1L, actualResponseBodyList.get(0).getId());
        assertEquals(2L, actualResponseBodyList.get(1).getId());

    }

    @Test
    public void test_updateUser_Success() {

        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));

//        when(modelMapper.map(userCreateRequestBody, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserResponseBody.class)).thenReturn(userResponseBody);

        UserResponseBody actualResponseBody = userService.updateUser(userRequestBody, 1L);

        assertNotNull(actualResponseBody);
        assertEquals(userResponseBody, actualResponseBody);

        // Verify that UserRepository.save() is called once
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void test_updateUser_UserNotFoundException() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(1234L);
        });

        String expectedMessage = "User not found with userId : 1234";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        // Verify that UserRepository.save() is never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void test_updateUser_ThrowsInvalidCountryNameException() {

        userRequestBody.setMobile("987654321");
        userRequestBody.setCountryName("Ind");
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userRequestBody, 1L);
        });

        String expectedMessage = "Invalid Country Name or Country Name Not Found.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        // Verify that UserRepository.save() is never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void test_updateUser_ThrowsInvalidMobileNumberParseException() {

        userRequestBody.setMobile("987654321234567890");
        userRequestBody.setCountryName("United States");

        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userRequestBody, 1L);
        });

        String expectedMessage = "Invalid Mobile Number Format or Region Code";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

//      Verify that UserRepository.save() is never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void test_updateUser_ThrowsInvalidMobileNumberFormatException() {

        userRequestBody.setMobile("123456");
        userRequestBody.setCountryName("United States");

        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userRequestBody, 1L);
        });

        String expectedMessage = "Invalid Mobile Number Format";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        // Verify that UserRepository.save() is never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void test_deleteUser_Success() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(user);

    }

    @Test
    public void test_deleteUser_UserNotFoundException() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(1234L);
        });

        String expectedMessage = "User not found with userId : 1234";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        // Verify that UserRepository.delete() is never called
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    public void test_deactivateUserAccount_Success() {

        UserResponseBody expectedUserResponseBody = new UserResponseBody();
        expectedUserResponseBody = new UserResponseBody();
        expectedUserResponseBody.setId(12L);
        expectedUserResponseBody.setUserName("testuser");
        expectedUserResponseBody.setName("Test User");
        expectedUserResponseBody.setEmail("testuser@example.com");
        expectedUserResponseBody.setMobile("9876543210");
        expectedUserResponseBody.setCountryName("India");
        expectedUserResponseBody.setUserDeleted(true);
        expectedUserResponseBody.setUserDeletionTimestamp(String.valueOf(Instant.now()));

        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(any())).thenReturn(user);
        when(modelMapper.map(user, UserResponseBody.class)).thenReturn(expectedUserResponseBody);

        UserResponseBody actual = userService.deactivateUserAccount(12L);

        assertEquals(expectedUserResponseBody, actual);

        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    public void test_deactivateUserAccount_UserNotFoundException() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deactivateUserAccount(1234L);
        });

        String expectedMessage = "User not found with userId : 1234";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        // Verify that UserRepository.delete() is never called
        verify(userRepository, never()).delete(any(User.class));

    }

    @Test
    public void test_activateUserAccount_Success() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(deactivatedUser));
        when(userRepository.save(any())).thenReturn(deactivatedUser);
        when(modelMapper.map(deactivatedUser, UserResponseBody.class)).thenReturn(userResponseBody);

        UserResponseBody actual = userService.activateUserAccount(1L);

        assertNotNull(actual);
        assertFalse(deactivatedUser.isUserDeleted());
        assertNull(deactivatedUser.getUserDeletionTimestamp());

        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    public void test_activateUserAccount_UserNotFoundException() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.activateUserAccount(1234L);
        });

        String expectedMessage = "User not found with userId : 1234";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        // Verify that UserRepository.delete() is never called
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    public void test_checkUsernameAvailability_Available() {

        when(userRepository.existsByUserName(user.getUserName())).thenReturn(false);

        boolean actual = userService.checkUsernameAvailability("testuser");

        assertTrue(actual);

        verify(userRepository, times(1)).existsByUserName(anyString());
    }

    @Test
    public void test_checkUsernameAvailability_NotAvailable() {

        when(userRepository.existsByUserName(user.getUserName())).thenReturn(true);

        boolean actual = userService.checkUsernameAvailability("testuser");

        assertFalse(actual);

        verify(userRepository, times(1)).existsByUserName(anyString());
    }

}
