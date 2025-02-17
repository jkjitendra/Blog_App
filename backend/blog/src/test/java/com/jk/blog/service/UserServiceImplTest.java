package com.jk.blog.service;

import com.jk.blog.dto.AuthDTO.AuthRequest;
import com.jk.blog.dto.user.UpdatePasswordRequestBody;
import com.jk.blog.dto.user.UpdatePasswordResponseBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.entity.Comment;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import com.jk.blog.exception.AccountDeletionPeriodExceededException;
import com.jk.blog.exception.InvalidCountryException;
import com.jk.blog.exception.PasswordNotMatchException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.security.AuthenticationFacade;
import com.jk.blog.service.impl.UserServiceImpl;
import com.jk.blog.utils.CountryToRegionCodeUtil;
import com.jk.blog.utils.JwtUtil;
import com.jk.blog.utils.PhoneNumberValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    private User testUser;
    private Post postWithinCutoff;
    private Post postBeyondCutoff;
    private Comment commentWithinCutoff;
    private Comment commentBeyondCutoff;
    private static final Instant NOW = Instant.now();
    private static final Instant CUTOFF = NOW.minusSeconds(60 * 60 * 24 * 90); // 90 days ago

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "test";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setUserName(TEST_USERNAME);
        testUser.setName("John Doe");
        testUser.setMobile("+1234567890");
        testUser.setCountryName("USA");
        testUser.setUserDeleted(false);

        // Post deleted within cutoff period (should be restored)
        postWithinCutoff = new Post();
        postWithinCutoff.setPostDeleted(true);
        postWithinCutoff.setPostDeletionTimestamp(NOW.minusSeconds(60 * 60 * 24 * 30)); // 30 days ago

        // Post deleted beyond cutoff period (should NOT be restored)
        postBeyondCutoff = new Post();
        postBeyondCutoff.setPostDeleted(true);
        postBeyondCutoff.setPostDeletionTimestamp(NOW.minusSeconds(60 * 60 * 24 * 120)); // 120 days ago

        // Comment deleted within cutoff period (should be restored)
        commentWithinCutoff = new Comment();
        commentWithinCutoff.setCommentDeleted(true);
        commentWithinCutoff.setCommentDeletionTimestamp(NOW.minusSeconds(60 * 60 * 24 * 30)); // 30 days ago

        // Comment deleted beyond cutoff period (should NOT be restored)
        commentBeyondCutoff = new Comment();
        commentBeyondCutoff.setCommentDeleted(true);
        commentBeyondCutoff.setCommentDeletionTimestamp(NOW.minusSeconds(60 * 60 * 24 * 120)); // 120 days ago

        testUser.setPosts(List.of(postWithinCutoff, postBeyondCutoff));
        testUser.setComments(List.of(commentWithinCutoff, commentBeyondCutoff));
    }

    /** FIND USER TESTS **/

    @Test
    void test_findUserById_WhenUserExists_ReturnUserResponse() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        Optional<UserResponseBody> response = userService.findUserById(TEST_USER_ID);

        assertTrue(response.isPresent());
        assertEquals(TEST_EMAIL, response.get().getEmail());
    }

    @Test
    void test_findUserById_WhenUserNotFound_ThrowException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(TEST_USER_ID));
    }

    @Test
    void test_findUserByEmail_WhenUserExists_ReturnUserResponse() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        Optional<UserResponseBody> response = userService.findUserByEmail(TEST_EMAIL);

        assertTrue(response.isPresent());
        assertEquals(TEST_EMAIL, response.get().getEmail());
    }

    @Test
    void test_findUserByEmail_WhenUserNotFound_ThrowException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findUserByEmail(TEST_EMAIL));
    }

    @Test
    void test_findUserByUserName_WhenUserExists_ReturnUserResponse() {
        when(userRepository.findByUserName(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        Optional<UserResponseBody> response = userService.findUserByUserName(TEST_USERNAME);

        assertTrue(response.isPresent());
        assertEquals(TEST_USERNAME, response.get().getUserName());
    }

    @Test
    void test_findUserByUserName_WhenUserNotFound_ThrowException() {
        when(userRepository.findByUserName(TEST_USERNAME)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findUserByUserName(TEST_USERNAME));
    }

    @Test
    void test_getAllUsers_WhenUsersExist_ReturnUserList() {
        User testUser1 = new User();
        testUser1.setUserId(1L);
        testUser1.setName("John Doe");
        testUser1.setEmail("john@example.com");

        User testUser2 = new User();
        testUser2.setUserId(2L);
        testUser2.setName("Jane Doe");
        testUser2.setEmail("jane@example.com");

        List<User> users = List.of(testUser1, testUser2);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponseBody> response = userService.getAllUsers();

        assertEquals(2, response.size());
        assertEquals("John Doe", response.get(0).getName());
        assertEquals("Jane Doe", response.get(1).getName());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void test_getAllUsers_WhenNoUsersExist_ReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        List<UserResponseBody> response = userService.getAllUsers();

        assertTrue(response.isEmpty());

        verify(userRepository, times(1)).findAll();
    }


    /** UPDATE USER TESTS **/

    @Test
    void test_updateUser_WhenValidData_UpdateUserSuccessfully() {
        UserRequestBody requestBody = new UserRequestBody();
        requestBody.setName("Updated Name");
        requestBody.setEmail(TEST_EMAIL);

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseBody response = userService.updateUser(requestBody);

        assertEquals("Updated Name", response.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void test_updateUser_WhenUserExistsAndValidData_ReturnUpdatedUser() {
        UserRequestBody requestBody = new UserRequestBody();
        requestBody.setName("Updated Name");
        requestBody.setEmail("updated@example.com");

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseBody response = userService.updateUser(requestBody);

        assertNotNull(response);
        assertEquals("Updated Name", testUser.getName());
        assertEquals("updated@example.com", testUser.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void test_updateUser_WhenUserNotFound_ThrowResourceNotFoundException() {
        UserRequestBody requestBody = new UserRequestBody();
        requestBody.setName("New Name");

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(requestBody));
    }

    @Test
    void test_updateUser_WhenUpdatingPhoneNumberWithInvalidFormat_ThrowInvalidCountryException() {
        UserRequestBody requestBody = new UserRequestBody();
        requestBody.setMobile("invalid_phone");
        requestBody.setCountryName("InvalidCountry");

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        try (MockedStatic<CountryToRegionCodeUtil> countryMock = mockStatic(CountryToRegionCodeUtil.class)) {
            when(CountryToRegionCodeUtil.getCountryISOCode("InvalidCountry"))
                    .thenThrow(new InvalidCountryException("Invalid Country Name: InvalidCountry"));

            assertThrows(InvalidCountryException.class, () -> userService.updateUser(requestBody));
        }

    }

    @Test
    void test_updateUser_WhenUpdatingPhoneNumberWithValidFormat_ReturnUpdatedUser() {
        UserRequestBody requestBody = new UserRequestBody();
        requestBody.setMobile("9876543210");
        requestBody.setCountryName("India");

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        try (MockedStatic<CountryToRegionCodeUtil> countryMock = mockStatic(CountryToRegionCodeUtil.class);
             MockedStatic<PhoneNumberValidationUtil> phoneMock = mockStatic(PhoneNumberValidationUtil.class)) {

            countryMock.when(() -> CountryToRegionCodeUtil.getCountryISOCode("India")).thenReturn("IN");
            phoneMock.when(() -> PhoneNumberValidationUtil.isValidPhoneNumber("9876543210", "IN")).thenReturn(false);
            phoneMock.when(() -> PhoneNumberValidationUtil.getPhoneNumber("IN", "9876543210")).thenReturn("+919876543210");

            UserResponseBody response = userService.updateUser(requestBody);

            assertNotNull(response);
            assertEquals("+919876543210", testUser.getMobile());
            assertEquals("India", testUser.getCountryName());

            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    /** UPDATE PASSWORD TESTS **/

    @Test
    void test_updatePassword_WhenOldPasswordMatches_UpdatePasswordSuccessfully() {
        UpdatePasswordRequestBody passwordRequest = new UpdatePasswordRequestBody();
        passwordRequest.setOldPassword("oldPass123");
        passwordRequest.setNewPassword("NewPass123!");

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(passwordRequest.getNewPassword())).thenReturn("hashedPassword");
        when(jwtUtil.generateToken(TEST_EMAIL)).thenReturn("newToken");

//        doNothing().when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        UpdatePasswordResponseBody response = userService.updatePassword(passwordRequest);

        assertEquals("newToken", response.getAccessToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void test_updatePassword_WhenOldPasswordIncorrect_ThrowException() {
        UpdatePasswordRequestBody passwordRequest = new UpdatePasswordRequestBody();
        passwordRequest.setOldPassword("wrongPass");

        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        doThrow(PasswordNotMatchException.class).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(PasswordNotMatchException.class, () -> userService.updatePassword(passwordRequest));
    }

    /** DELETE USER TESTS **/

    @Test
    void test_deleteUser_WhenAuthenticated_SuccessfullyMarksUserDeleted() {
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        userService.deleteUser();

        assertTrue(testUser.isUserDeleted());
        verify(userRepository, times(1)).save(testUser);
    }

    /** DEACTIVATE USER ACCOUNT TESTS **/

    @Test
    void test_deactivateUserAccount_WhenUserExists_SuccessfullyDeactivatesUser() {
        // Mock authentication
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User response = userService.deactivateUserAccount();

        assertNotNull(response);
        assertTrue(response.isUserDeleted());
        assertNotNull(response.getUserDeletionTimestamp());

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void test_deactivateUserAccount_WhenUserNotFound_ThrowsResourceNotFoundException() {
        // Mock authentication but return empty user
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deactivateUserAccount());

        verify(userRepository, never()).save(any(User.class)); // Ensure no save operation occurs
    }

    /** ACTIVATE USER ACCOUNT TESTS **/

    @Test
    void test_activateUserAccount_WhenWithinAllowedPeriod_ReactivatesUser() {
        testUser.setUserDeleted(true);
        testUser.setUserDeletionTimestamp(Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS));

        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin(TEST_EMAIL);
        authRequest.setPassword("ValidPassword123");

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseBody response = userService.activateUserAccount(authRequest);

        assertFalse(testUser.isUserDeleted());
        assertNull(testUser.getUserDeletionTimestamp());
        assertEquals(TEST_EMAIL, response.getEmail());
    }

    @Test
    void test_activateUserAccount_WhenBeyondAllowedPeriod_ThrowException() {
        testUser.setUserDeleted(true);
        testUser.setUserDeletionTimestamp(Instant.now().minus(100, java.time.temporal.ChronoUnit.DAYS));

        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin(TEST_EMAIL);
        authRequest.setPassword("ValidPassword123");

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        assertThrows(AccountDeletionPeriodExceededException.class, () -> userService.activateUserAccount(authRequest));
    }

    /** CHECK USERNAME AVAILABILITY TESTS **/

    @Test
    void test_checkUsernameAvailability_WhenUsernameNotTaken_ReturnTrue() {
        when(userRepository.existsByUserName("newUser")).thenReturn(false);

        boolean isAvailable = userService.checkUsernameAvailability("newUser");

        assertTrue(isAvailable);
    }

    @Test
    void test_checkUsernameAvailability_WhenUsernameTaken_ReturnFalse() {
        when(userRepository.existsByUserName("existingUser")).thenReturn(true);

        boolean isAvailable = userService.checkUsernameAvailability("existingUser");

        assertFalse(isAvailable);
    }

    @Test
    void test_restoreUserDataInBackground_WhenPostsAndCommentsWithinCutoff_AreRestored() {
        userService.restoreUserDataInBackground(testUser, CUTOFF);

        // Verify posts restoration
        verify(postRepository, times(1)).save(postWithinCutoff);
        assertFalse(postWithinCutoff.isPostDeleted());
        assertNull(postWithinCutoff.getPostDeletionTimestamp());

        verify(postRepository, never()).save(postBeyondCutoff); // Should NOT be restored

        // Verify comments restoration
        verify(commentRepository, times(1)).save(commentWithinCutoff);
        assertFalse(commentWithinCutoff.isCommentDeleted());
        assertNull(commentWithinCutoff.getCommentDeletionTimestamp());

        verify(commentRepository, never()).save(commentBeyondCutoff); // Should NOT be restored
    }

    @Test
    void test_restoreUserDataInBackground_WhenPostsAndCommentsBeyondCutoff_RemainDeleted() {
        userService.restoreUserDataInBackground(testUser, CUTOFF);

        // Ensure beyond-cutoff posts/comments are NOT restored
        assertTrue(postBeyondCutoff.isPostDeleted());
        assertNotNull(postBeyondCutoff.getPostDeletionTimestamp());
        verify(postRepository, never()).save(postBeyondCutoff);

        assertTrue(commentBeyondCutoff.isCommentDeleted());
        assertNotNull(commentBeyondCutoff.getCommentDeletionTimestamp());
        verify(commentRepository, never()).save(commentBeyondCutoff);
    }

    @Test
    void test_restoreUserDataInBackground_WhenUserHasNoPostsOrComments_ShouldNotFail() {
        User emptyUser = new User();
        emptyUser.setEmail("emptyuser@example.com");

        // Should not throw any exceptions
        assertDoesNotThrow(() -> userService.restoreUserDataInBackground(emptyUser, CUTOFF));

        // Verify no repository operations occur
        verifyNoInteractions(postRepository);
        verifyNoInteractions(commentRepository);
    }

}