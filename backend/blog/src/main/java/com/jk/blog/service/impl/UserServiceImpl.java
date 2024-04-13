package com.jk.blog.service.impl;

import com.jk.blog.dto.UserRequestBody;
import com.jk.blog.dto.UserResponseBody;
import com.jk.blog.entity.Profile;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.UserService;
import com.jk.blog.utils.CountryToRegionCodeUtil;
import com.jk.blog.utils.PhoneNumberValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        user.setMobile(PhoneNumberValidationUtil.getPhoneNumber(regionCode, userRequestBody.getMobile()));
        user.setCreatedDate(Instant.now());
        Profile profile = new Profile();
        profile.setUser(user); // Associate profile with the user
        user.setProfile(profile);

        User savedUser = this.userRepository.save(user);
        return this.userToDTO(savedUser);
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

        if(userRequestBody.getMobile() != null && userRequestBody.getCountryName() != null){
            String regionCode = CountryToRegionCodeUtil.getCountryISOCode(userRequestBody.getCountryName());
            if (!PhoneNumberValidationUtil.isValidPhoneNumber(userRequestBody.getMobile(), regionCode)) {
                throw new IllegalArgumentException("Invalid Mobile Number Format");
            }
            user.setCountryName(userRequestBody.getCountryName());
            user.setMobile(PhoneNumberValidationUtil.getPhoneNumber(regionCode, userRequestBody.getMobile()));
        }
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
    @Transactional
    public void deactivateUserAccount(Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        user.setUserDeleted(true);
        this.userRepository.save(user);
        // TO be done:- send email/notification to the user about account deactivation and data cleanup process
    }

    @Override
    @Transactional
    public void activateUserAccount(Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        if (user.isUserDeleted() && user.getUserDeletionTimestamp().isAfter(cutoff)) {
            user.setUserDeleted(false);
            user.setUserDeletionTimestamp(null);
            user.getPosts().forEach((comment) -> {
                if (comment.isPostDeleted() && comment.getPostDeletionTimestamp() != null && comment.getPostDeletionTimestamp().isAfter(cutoff)) {
                    comment.setPostDeleted(false);
                    comment.setPostDeletionTimestamp(null);
                }
            });
            this.userRepository.save(user);
        }
        // TO be done:- send email/notification to the user about account activation OR we can create an AOP for this
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
