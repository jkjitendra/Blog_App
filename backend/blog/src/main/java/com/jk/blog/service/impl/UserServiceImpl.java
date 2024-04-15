package com.jk.blog.service.impl;

import com.jk.blog.dto.user.UserCreateRequestBody;
import com.jk.blog.dto.user.UserRequestBody;
import com.jk.blog.dto.user.UserResponseBody;
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
    public UserResponseBody createUser(UserCreateRequestBody userCreateRequestBody) {
        String regionCode = CountryToRegionCodeUtil.getCountryISOCode(userCreateRequestBody.getCountryName());
        if (!PhoneNumberValidationUtil.isValidPhoneNumber(userCreateRequestBody.getMobile(), regionCode)) {
            throw new IllegalArgumentException("Invalid Mobile Number Format");
        }
//        User user = UserMapper.userRequestBodyToUser(userRequestBody);
        User user = this.modelMapper.map(userCreateRequestBody, User.class);
        user.setMobile(PhoneNumberValidationUtil.getPhoneNumber(regionCode, userCreateRequestBody.getMobile()));
        user.setUserCreatedDate(Instant.now());
        Profile profile = new Profile();
        profile.setUser(user); // Associate profile with the user
        user.setProfile(profile);

        User savedUser = this.userRepository.save(user);
//        return UserMapper.userToUserResponseBody(savedUser);
        return this.modelMapper.map(savedUser, UserResponseBody.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseBody getUserById(Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

//        return UserMapper.userToUserResponseBody(user);
        return this.modelMapper.map(user, UserResponseBody.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseBody> getAllUsers() {
        List<User> usersList = this.userRepository.findAll();
        return usersList.stream()
                        .map(user -> this.modelMapper.map(user, UserResponseBody.class))
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
//        return UserMapper.userToUserResponseBody(updatedUser);
        return this.modelMapper.map(updatedUser, UserResponseBody.class);
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
    public UserResponseBody deactivateUserAccount(Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        user.setUserDeleted(true);
        user.setUserDeletionTimestamp(Instant.now());
        User deactivatedUser = this.userRepository.save(user);
        return this.modelMapper.map(deactivatedUser, UserResponseBody.class);
        // TO be done:- send email/notification to the user about account deactivation and data cleanup process
    }

    @Override
    @Transactional
    public UserResponseBody activateUserAccount(Long userId) {
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
        }
        User activatedUser = this.userRepository.save(user);
        return this.modelMapper.map(activatedUser, UserResponseBody.class);
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

}
