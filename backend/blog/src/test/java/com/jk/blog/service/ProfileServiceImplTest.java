package com.jk.blog.service;

import com.jk.blog.dto.profile.ProfileRequestBody;
import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.entity.Profile;
import com.jk.blog.entity.User;
import com.jk.blog.exception.FieldUpdateNotAllowedException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.ProfileRepository;
import com.jk.blog.service.impl.ProfileServiceImpl;
import com.jk.blog.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MultipartFile mockImage;

    @Mock
    private FileService fileService;

    private User testUser;
    private Profile testProfile;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_PROFILE_ID = 100L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(TEST_USER_ID);

        testProfile = new Profile();
        testProfile.setProfileId(TEST_PROFILE_ID);
        testProfile.setUser(testUser);
    }

    /** GET PROFILE TESTS **/

    @Test
    void test_getProfileByUserId_WhenProfileExists_ReturnProfileResponseBody() {
        when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.of(testProfile));
        when(modelMapper.map(testProfile, ProfileResponseBody.class)).thenReturn(new ProfileResponseBody());

        ProfileResponseBody response = profileService.getProfileByUserId(TEST_USER_ID);

        assertNotNull(response);
    }

    @Test
    void test_getProfileByUserId_WhenProfileNotFound_ThrowResourceNotFoundException() {
        when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profileService.getProfileByUserId(TEST_USER_ID));
    }

    /** UPDATE PROFILE TESTS **/

    @Test
    void test_updateProfile_WhenUserIsAuthorized_ReturnUpdatedProfile() throws IOException {
        ProfileRequestBody requestBody = new ProfileRequestBody();
        requestBody.setAddress("New Address");
        requestBody.setAbout("User 1 is a developer");
        requestBody.setImageUrl("mockedImageUrl");
        requestBody.setSocialMediaLinks(List.of("www.github.com/user1", "www.linkedin.com/in/user1"));

        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);
            when(fileService.uploadImage(anyString(), any())).thenReturn("mockedImageUrl");

            when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
            when(modelMapper.map(testProfile, ProfileResponseBody.class)).thenReturn(new ProfileResponseBody());

            ProfileResponseBody response = profileService.updateProfile(requestBody, TEST_USER_ID, mockImage);

            assertNotNull(response);
            verify(profileRepository, times(1)).save(any(Profile.class));
        }
    }

    @Test
    void test_updateProfile_WhenUserIsAuthorizedAndProfileRequestBodyIsNull_ReturnUpdatedProfile() throws IOException {
        ProfileRequestBody requestBody = new ProfileRequestBody();
        requestBody.setAddress(null);
        requestBody.setAbout(null);
        requestBody.setImageUrl(null);
        requestBody.setSocialMediaLinks(null);


        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);

            when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
            when(modelMapper.map(testProfile, ProfileResponseBody.class)).thenReturn(new ProfileResponseBody());

            ProfileResponseBody response = profileService.updateProfile(requestBody, TEST_USER_ID, null);

            assertNotNull(response);
            verify(profileRepository, times(1)).save(any(Profile.class));
        }
    }

    @Test
    void test_updateProfile_WhenUserNotAuthorized_ThrowUnAuthorizedException() {
        ProfileRequestBody requestBody = new ProfileRequestBody();
        requestBody.setAddress("New Address");

        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            User differentUser = new User();
            differentUser.setUserId(2L);  // Different user ID
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(differentUser);

            assertThrows(UnAuthorizedException.class, () -> profileService.updateProfile(requestBody, TEST_USER_ID, mockImage));
        }
    }

    @Test
    void test_updateProfile_WhenProfileNotFound_ThrowResourceNotFoundException() {
        ProfileRequestBody requestBody = new ProfileRequestBody();
        requestBody.setAddress("New Address");

        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);

            when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> profileService.updateProfile(requestBody, TEST_USER_ID, mockImage));
        }
    }

    /** PATCH PROFILE TESTS **/

    @Test
    void test_patchProfile_WhenUserIsAuthorized_ReturnPatchedProfile() throws IOException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("address", "Updated Address");
        updates.put("about", "Updated About");
        updates.put("socialMediaLinks", List.of("www.github.com/user1", "www.linkedin.com/in/user1"));


        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);
            when(fileService.uploadImage(anyString(), any())).thenReturn("mockedImageUrl");

            when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
            when(modelMapper.map(testProfile, ProfileResponseBody.class)).thenReturn(new ProfileResponseBody());

            ProfileResponseBody response = profileService.patchProfile(updates, TEST_USER_ID, mockImage);

            assertNotNull(response);
            verify(profileRepository, times(1)).save(any(Profile.class));
        }
    }

    @Test
    void test_patchProfile_WhenUserIsAuthorizedAndRequestBodyIsNull_ReturnPatchedProfile() throws IOException {
        Map<String, Object> updates = new HashMap<>();

        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);

            when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
            when(modelMapper.map(testProfile, ProfileResponseBody.class)).thenReturn(new ProfileResponseBody());

            ProfileResponseBody response = profileService.patchProfile(updates, TEST_USER_ID, null);

            assertNotNull(response);
            verify(profileRepository, times(1)).save(any(Profile.class));
        }
    }


    @Test
    void test_patchProfile_WhenUserNotAuthorized_ThrowUnAuthorizedException() {
        Map<String, Object> updates = Map.of("about", "Updated About");

        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            User differentUser = new User();
            differentUser.setUserId(2L);
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(differentUser);

            assertThrows(UnAuthorizedException.class, () -> profileService.patchProfile(updates, TEST_USER_ID, mockImage));
        }
    }

    @Test
    void test_patchProfile_WhenTryingToUpdateRestrictedFields_ThrowFieldUpdateNotAllowedException() {
        Map<String, Object> updates = Map.of("profileId", 200L);

        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);

            when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.of(testProfile));

            assertThrows(FieldUpdateNotAllowedException.class, () -> profileService.patchProfile(updates, TEST_USER_ID, mockImage));
        }
    }

    /** DELETE PROFILE TESTS **/

    @Test
    void test_deleteProfile_WhenUserIsAuthorized_DeleteProfile() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);

            when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.of(testProfile));
            doNothing().when(profileRepository).delete(testProfile);

            profileService.deleteProfile(TEST_USER_ID);

            verify(profileRepository, times(1)).delete(testProfile);
        }
    }

    @Test
    void test_deleteProfile_WhenUserNotAuthorized_ThrowUnAuthorizedException() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            User differentUser = new User();
            differentUser.setUserId(2L);
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(differentUser);

            assertThrows(UnAuthorizedException.class, () -> profileService.deleteProfile(TEST_USER_ID));
        }
    }

    @Test
    void test_deleteProfile_WhenProfileNotFound_ThrowResourceNotFoundException() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {

            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);
            when(profileRepository.findByUser_UserId(TEST_USER_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> profileService.deleteProfile(TEST_USER_ID));
        }
    }
}