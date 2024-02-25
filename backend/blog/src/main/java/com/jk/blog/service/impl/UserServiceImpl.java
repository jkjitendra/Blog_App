package com.jk.blog.service.impl;

import com.jk.blog.dto.UserRequestBody;
import com.jk.blog.dto.UserResponseBody;
import com.jk.blog.entity.Profile;
import com.jk.blog.entity.User;
//import com.jk.blog.exception.InvalidTokenException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
//import com.jk.blog.service.MailService;
//import com.jk.blog.service.PasswordResetService;
import com.jk.blog.service.UserService;
import com.jk.blog.utils.CountryToRegionCodeUtil;
import com.jk.blog.utils.PhoneNumberValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//    @Autowired
//    private MailService mailService;
//    @Autowired
//    private PasswordResetService passwordResetService;

    @Override
    @Transactional
    public UserResponseBody createUser(UserRequestBody userRequestBody) {
        String regionCode = CountryToRegionCodeUtil.getCountryISOCode(userRequestBody.getCountryName());
        if (!PhoneNumberValidationUtil.isValidPhoneNumber(userRequestBody.getMobile(), regionCode)) {
            throw new IllegalArgumentException("Invalid Mobile Number Format");
        }
        User user = this.dtoToUser(userRequestBody);
        user.setActive(true);

        Profile profile = new Profile();
        profile.setUser(user); // Associate profile with the user
        user.setProfile(profile);

        User savedUser = this.userRepository.save(user);
        UserResponseBody createdUserResponseBody = this.userToDTO(savedUser);
        createdUserResponseBody.setActive(true);
        return createdUserResponseBody;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseBody getUserById(Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        return this.userToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseBody> getAllUsers() {
        List<User> usersList = this.userRepository.findAll();
        return usersList.stream()
                        .map(this::userToDTO)
                        .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseBody updateUser(UserRequestBody userRequestBody, Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        if(userRequestBody.getName() != null)
            user.setName(userRequestBody.getName());
        if(userRequestBody.getEmail() != null)
            user.setEmail(userRequestBody.getEmail());
        if(userRequestBody.getMobile() != null){
            String regionCode = CountryToRegionCodeUtil.getCountryISOCode(userRequestBody.getCountryName());
            if (!PhoneNumberValidationUtil.isValidPhoneNumber(userRequestBody.getMobile(), regionCode)) {
                throw new IllegalArgumentException("Invalid Mobile Number Format");
            }
            user.setMobile(userRequestBody.getMobile());
        }
        if(userRequestBody.getCountryName() != null)
            user.setCountryName(userRequestBody.getCountryName());
//        user.setPassword(userDTO.getPassword());
//        user.setActive(userDTO.getActive());
        User updatedUser = this.userRepository.save(user);
        return this.userToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        this.userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkUsernameAvailability(String username) {
        return !userRepository.existsByUserName(username);
    }

//    @Override
//    public void verifyAndResetPassword(String token, String newPassword, String email) {
//        // Assuming userRepository and passwordEncoder are already defined and autowired in this class
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "Email", email));
//
//        if (!token.equals(user.getResetToken()) || user.getResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
//            throw new InvalidTokenException("Invalid or expired token");
//        }
//
//        // Proceed to reset password
//        user.setPassword(passwordEncoder.encode(newPassword)); // Make sure to encode the new password
//        user.setResetToken(null); // Clear the reset token
//        user.setResetTokenExpiryDate(null); // Clear the token expiry date
//        userRepository.save(user);
//    }
//
//    public void initiatePasswordReset(String email) {
//        this.passwordResetService.initiateResetPasswordProcess(email);
//    }


    public User dtoToUser(UserRequestBody userRequestBody) {
        return this.modelMapper.map(userRequestBody, User.class);
//        user.setId(userDTO.getId());
//        user.setName(userDTO.getName());
//        user.setEmail(userDTO.getEmail());
//        user.setPassword(userDTO.getPassword());
//        user.setActive(userDTO.getActive());
//        user.setMobile(userDTO.getMobile());
//        user.setCountryName(userDTO.getCountryName());
//        return user;
    }

    public UserResponseBody userToDTO(User user) {
        return this.modelMapper.map(user, UserResponseBody.class);
//        userDTO.setId(user.getId());
//        userDTO.setName(user.getName());
//        userDTO.setEmail(user.getEmail());
//        userDTO.setPassword(user.getPassword());
//        userDTO.setActive(user.getActive());
//        userDTO.setMobile(user.getMobile());
//        userDTO.setCountryName(user.getCountryName());
//        return userDTO;
    }
}
