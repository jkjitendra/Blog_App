package com.jk.blog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.profile.ProfileRequestBody;
import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.security.AuthenticationFacade;
import com.jk.blog.service.ProfileService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @InjectMocks
    private ProfileController profileController;

    @Mock
    private ProfileService profileService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    private ProfileResponseBody profileResponseBody;

    @BeforeEach
    void setUp() {
        profileResponseBody = new ProfileResponseBody();
        profileResponseBody.setProfileId(1L);
        profileResponseBody.setAbout("Software Developer");
    }

    @Test
    void test_getUsersProfile_whenAuthenticated_returnProfile() {
        when(authenticationFacade.getAuthenticatedUserId()).thenReturn(1L);
        when(profileService.getProfileByUserId(1L)).thenReturn(profileResponseBody);

        ResponseEntity<APIResponse<ProfileResponseBody>> response = profileController.getUsersProfile();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Software Developer", response.getBody().getData().getAbout());

        verify(profileService, times(1)).getProfileByUserId(1L);
    }

    @Test
    void test_updateUsersProfile_whenValidRequest_returnUpdatedProfile() throws IOException {
        ProfileRequestBody profileRequestBody = new ProfileRequestBody();
        MultipartFile mockFile = mock(MultipartFile.class);

        when(authenticationFacade.getAuthenticatedUserId()).thenReturn(1L);
        when(profileService.updateProfile(any(ProfileRequestBody.class), eq(1L), any())).thenReturn(profileResponseBody);

        ResponseEntity<APIResponse<ProfileResponseBody>> response =
                profileController.updateUsersProfile(new ObjectMapper().writeValueAsString(profileRequestBody), mockFile);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());

        verify(profileService, times(1)).updateProfile(any(ProfileRequestBody.class), eq(1L), any());
    }

    @Test
    void test_patchUsersProfile_whenValidRequest_returnPatchedProfile() throws IOException {
        Map<String, Object> updates = Map.of("bio", "Updated Bio");
        MultipartFile mockFile = mock(MultipartFile.class);

        when(authenticationFacade.getAuthenticatedUserId()).thenReturn(1L);
        when(profileService.patchProfile(anyMap(), eq(1L), any())).thenReturn(profileResponseBody);

        ResponseEntity<APIResponse<ProfileResponseBody>> response =
                profileController.patchUsersProfile(new ObjectMapper().writeValueAsString(updates), mockFile);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(profileService, times(1)).patchProfile(anyMap(), eq(1L), any());
    }

    @Test
    void test_deleteUsersProfile_whenAuthenticated_returnSuccessMessage() {
        when(authenticationFacade.getAuthenticatedUserId()).thenReturn(1L);
        doNothing().when(profileService).deleteProfile(1L);

        ResponseEntity<APIResponse<String>> response = profileController.deleteUsersProfile();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());

        verify(profileService, times(1)).deleteProfile(1L);
    }

    @Test
    void test_patchUsersProfile_whenInvalidJsonProvided_throwBadRequestException() throws IOException {

        String invalidJson = "{invalidJson";
        MultipartFile mockFile = mock(MultipartFile.class);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> profileController.patchUsersProfile(invalidJson, mockFile)
        );

        assertEquals("Invalid JSON format for profile updates", exception.getMessage());
    }
}