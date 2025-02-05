package com.jk.blog.service;

import com.jk.blog.dto.AuthDTO.AuthRequest;
import com.jk.blog.dto.AuthDTO.AuthResponse;
import com.jk.blog.dto.AuthDTO.RegisterRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.entity.Role;
import com.jk.blog.entity.RoleType;
import com.jk.blog.entity.User;
import com.jk.blog.exception.InvalidRoleException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.ResourceAlreadyExistsException;
import com.jk.blog.repository.RoleRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.impl.AuthServiceImpl;
import com.jk.blog.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestBody registerRequest;
    private AuthRequest authRequest;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestBody();
        registerRequest.setName("John Doe");
        registerRequest.setUserName("johndoe");
        registerRequest.setEmail("johndoe@example.com");
        registerRequest.setPassword("Password123@");
        registerRequest.setMobile("9090908080");
        registerRequest.setCountryName("IN");
        registerRequest.setRole("ROLE_USUAL");

        authRequest = new AuthRequest();
        authRequest.setLogin("johndoe@example.com");
        authRequest.setPassword("Password123@");

        role = new Role();
        role.setName(RoleType.ROLE_USUAL.name());

        user = User.builder()
                .userId(1L)
                .name("John Doe")
                .userName("johndoe")
                .email("johndoe@example.com")
                .password(passwordEncoder.encode("Password123@"))
                .mobile("9090908080")
                .countryName("IN")
                .userCreatedDate(Instant.now())
                .roles(Set.of(role))
                .build();
    }

    /**
     * Test case: User registration with valid input
     */
    @Test
    void test_registerUser_ShouldRegisterSuccessfully_WhenValidInput() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleType.ROLE_USUAL.name())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserResponseBody.class))).thenReturn(new UserResponseBody());

        UserResponseBody response = authService.registerUser(registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test case: Registration with an existing email should throw UserAlreadyExistingException
     */
    @Test
    void test_registerUser_ShouldThrowResourceAlreadyExistingException_WhenUserAlreadyExists() {

        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ROLE_USUAL");
        when(roleRepository.findByName(registerRequest.getRole())).thenReturn(Optional.of(role1));
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.registerUser(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test case: Registration with an invalid role should throw InvalidRoleException
     */
    @Test
    void test_registerUser_ShouldThrowInvalidRoleException_WhenRoleIsInvalid() {
        registerRequest.setRole("INVALID_ROLE");

        assertThrows(InvalidRoleException.class, () -> authService.registerUser(registerRequest));
        verify(roleRepository, never()).findByName(anyString());
    }

    /**
     * Test case: Registration should throw ResourceNotFoundException when role is not found
     */
    @Test
    void test_registerUser_ShouldThrowResourceNotFoundException_WhenRoleNotFound() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleType.ROLE_USUAL.name())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.registerUser(registerRequest));
    }

    /**
     * Test case: Generate access token successfully
     */
    @Test
    void test_generateAccessToken_ShouldReturnAuthResponse() {
        when(userRepository.findByEmail(authRequest.getLogin())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getEmail())).thenReturn("jwtToken");

        AuthResponse response = authService.generateAccessToken(authRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getAccessToken());
    }

    /**
     * Test case: Generate access token should throw exception when user not found
     */
    @Test
    void test_generateAccessToken_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userRepository.findByEmail(authRequest.getLogin())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.generateAccessToken(authRequest));
    }
}