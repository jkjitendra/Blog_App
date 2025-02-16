package com.jk.blog.service;

import com.jk.blog.entity.RefreshToken;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.TokenExpiredException;
import com.jk.blog.repository.RefreshTokenRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.impl.RefreshTokenServiceImpl;
import com.jk.blog.utils.GeneratorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_REFRESH_TOKEN = "sample_refresh_token";
    private static final Long TEST_USER_ID = 1L;

    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);

        testRefreshToken = new RefreshToken();
        testRefreshToken.setRefreshToken(TEST_REFRESH_TOKEN);
        testRefreshToken.setExpirationTime(Instant.now().plusSeconds(3600));
        testRefreshToken.setUser(testUser);
    }

    /** CREATE REFRESH TOKEN TESTS **/

    @Test
    void test_createRefreshToken_WhenUserExists_ReturnNewToken() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());

        try (MockedStatic<GeneratorUtils> mockedUtils = mockStatic(GeneratorUtils.class)) {
            mockedUtils.when(GeneratorUtils::generateRefreshToken).thenReturn(TEST_REFRESH_TOKEN);

            RefreshToken createdToken = refreshTokenService.createRefreshToken(TEST_EMAIL);

            assertNotNull(createdToken);
            assertEquals(TEST_REFRESH_TOKEN, createdToken.getRefreshToken());
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }
    }

    @Test
    void test_createRefreshToken_WhenUserExists_ReturnUpdatedToken() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.findByUser(testUser)).thenReturn(Optional.of(testRefreshToken));

        try (MockedStatic<GeneratorUtils> mockedUtils = mockStatic(GeneratorUtils.class)) {
            mockedUtils.when(GeneratorUtils::generateRefreshToken).thenReturn("new_refresh_token");

            // Ensure the repository returns the updated token when saving
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

            RefreshToken updatedToken = refreshTokenService.createRefreshToken(TEST_EMAIL);

            assertNotNull(updatedToken);
            assertEquals("new_refresh_token", updatedToken.getRefreshToken());
            verify(refreshTokenRepository, times(1)).save(testRefreshToken);
        }
    }

    @Test
    void test_createRefreshToken_WhenUserNotFound_ThrowResourceNotFoundException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> refreshTokenService.createRefreshToken(TEST_EMAIL));

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    /** VERIFY REFRESH TOKEN TESTS **/

    @Test
    void test_verifyRefreshToken_WhenValid_ReturnToken() {
        when(refreshTokenRepository.findByRefreshToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(testRefreshToken));

        RefreshToken validToken = refreshTokenService.verifyRefreshToken(TEST_REFRESH_TOKEN);

        assertNotNull(validToken);
        assertEquals(TEST_REFRESH_TOKEN, validToken.getRefreshToken());
    }

    @Test
    void test_verifyRefreshToken_WhenTokenExpired_ThrowTokenExpiredException() {
        testRefreshToken.setExpirationTime(Instant.now().minusSeconds(10)); // Expired token
        when(refreshTokenRepository.findByRefreshToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(testRefreshToken));

        assertThrows(TokenExpiredException.class, () -> refreshTokenService.verifyRefreshToken(TEST_REFRESH_TOKEN));

        verify(refreshTokenRepository, times(1)).delete(testRefreshToken);
    }

    @Test
    void test_verifyRefreshToken_WhenTokenNotFound_ThrowResourceNotFoundException() {
        when(refreshTokenRepository.findByRefreshToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> refreshTokenService.verifyRefreshToken(TEST_REFRESH_TOKEN));
    }

    /** DELETE REFRESH TOKEN TEST **/

    @Test
    void test_deleteRefreshToken_WhenTokenExists_DeleteToken() {
        doNothing().when(refreshTokenRepository).deleteByRefreshToken(TEST_REFRESH_TOKEN);

        refreshTokenService.deleteRefreshToken(TEST_REFRESH_TOKEN);

        verify(refreshTokenRepository, times(1)).deleteByRefreshToken(TEST_REFRESH_TOKEN);
    }
}