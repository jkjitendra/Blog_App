package com.jk.blog.service.impl;

import com.jk.blog.dto.AuthDTO.AuthRequest;
import com.jk.blog.dto.MailBody;
import com.jk.blog.dto.user.*;
import com.jk.blog.entity.User;
import com.jk.blog.exception.*;
import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.security.AuthenticationFacade;
import com.jk.blog.service.EmailService;
import com.jk.blog.service.UserService;
import com.jk.blog.utils.CountryToRegionCodeUtil;
import com.jk.blog.utils.JwtUtil;
import com.jk.blog.utils.PhoneNumberValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AuthenticationFacade authenticationFacade;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseBody> findUserById(Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        return Optional.of(UserMapper.userToUserResponseBody(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseBody> findUserByEmail(String email) {
        User user = this.userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return Optional.of(UserMapper.userToUserResponseBody(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseBody> findUserByUserName(String username) {
        User user = this.userRepository
                        .findByUserName(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return Optional.of(UserMapper.userToUserResponseBody(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseBody> getAllUsers() {
        List<User> usersList = this.userRepository.findAll();
        return usersList.stream()
                        .map(UserMapper::userToUserResponseBody)
                        .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseBody updateUser(UserRequestBody userRequestBody) {
        String authenticatedUserEmail = authenticationFacade.getAuthenticatedUsername();
        User user = this.userRepository
                .findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authenticatedUserEmail));

        if(userRequestBody.getName() != null)
            user.setName(userRequestBody.getName());
        if(userRequestBody.getEmail() != null)
            user.setEmail(userRequestBody.getEmail());

        if(userRequestBody.getMobile() != null && userRequestBody.getCountryName() != null){
            String regionCode = CountryToRegionCodeUtil.getCountryISOCode(userRequestBody.getCountryName());
            if (PhoneNumberValidationUtil.isValidPhoneNumber(userRequestBody.getMobile(), regionCode)) {
                throw new IllegalArgumentException("Invalid Mobile Number Format");
            }
            user.setCountryName(userRequestBody.getCountryName());
            user.setMobile(PhoneNumberValidationUtil.getPhoneNumber(regionCode, userRequestBody.getMobile()));
        }
        User updatedUser = this.userRepository.save(user);
        return UserMapper.userToUserResponseBody(updatedUser);
    }

    @Override
    @Transactional
    public UpdatePasswordResponseBody updatePassword(UpdatePasswordRequestBody updatePasswordRequestBody) {
        String authenticatedUserEmail = authenticationFacade.getAuthenticatedUsername();
        User user = this.userRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authenticatedUserEmail));

        if (!user.getEmail().equals(authenticatedUserEmail)) {
            throw new UnAuthorizedException("You are not allowed to change another user's password.");
        }

        // Verify the old password
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), updatePasswordRequestBody.getOldPassword())
            );
        } catch (PasswordNotMatchException e) {
            throw new PasswordNotMatchException("Incorrect old password");
        }

        // Encode and update the new password
        user.setPassword(passwordEncoder.encode(updatePasswordRequestBody.getNewPassword()));
        this.userRepository.save(user);

        // Generate a new JWT token for the user
        String newAccessToken = jwtUtil.generateToken(user.getEmail());

        return new UpdatePasswordResponseBody(newAccessToken);
    }

    @Override
    @Transactional
    public void deleteUser() {
        String authenticatedUserEmail = authenticationFacade.getAuthenticatedUsername();
        User user = this.userRepository
                .findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authenticatedUserEmail));

        user.setUserDeleted(true);
        user.setUserDeletionTimestamp(Instant.now());
        this.userRepository.save(user);
    }

    @Override
    @Transactional
    public User deactivateUserAccount() {
        String authenticatedUserEmail = authenticationFacade.getAuthenticatedUsername();
        User user = this.userRepository
                        .findByEmail(authenticatedUserEmail)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "email", authenticatedUserEmail));

        user.setUserDeleted(true);
        user.setUserDeletionTimestamp(Instant.now());
        this.userRepository.save(user);

        return user; // AOP will use this to send the email
    }

    @Override
    @Transactional
    public UserResponseBody activateUserAccount(AuthRequest authRequest) {

        User user = userRepository.findByEmail(authRequest.getLogin())
                .or(() -> userRepository.findByUserName(authRequest.getLogin()))  // If email not found, try username
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email/username: " + authRequest.getLogin()));

        // Check if the password matches
        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            throw new UnAuthorizedException("Credentials don't match. Activation failed.");
        }

        if (!user.isUserDeleted()) {
            throw new UserAccountAlreadyActiveException("User account is already active.");
        }

        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        // If the account was marked as deleted, reactivate it
        if (user.getUserDeletionTimestamp() != null && user.getUserDeletionTimestamp().isAfter(cutoff)) {
            user.setUserDeleted(false);
            user.setUserDeletionTimestamp(null);
        } else {
            throw new AccountDeletionPeriodExceededException("Account deletion period exceeded. Activation not allowed.");
        }

        User activatedUser = this.userRepository.save(user);

        // Trigger asynchronous task to restore posts and comments
        restoreUserDataInBackground(user, cutoff);

        // Send an email notification about account reactivation
        sendRestorationEmail(user);

        return UserMapper.userToUserResponseBody(activatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkUsernameAvailability(String username) {
        return !this.userRepository.existsByUserName(username);
    }

    /**
     * Asynchronously restores posts and comments for the user.
     */
    @Async
    public void restoreUserDataInBackground(User user, Instant cutoff) {
        user.getPosts().forEach(post -> {
            if (post.isPostDeleted() && post.getPostDeletionTimestamp() != null && post.getPostDeletionTimestamp().isAfter(cutoff)) {
                post.setPostDeleted(false);
                post.setPostDeletionTimestamp(null);
                this.postRepository.save(post); // Save each post update
            }
        });

        user.getComments().forEach(comment -> {
            if (comment.isCommentDeleted() && comment.getCommentDeletionTimestamp() != null
                    && comment.getCommentDeletionTimestamp().isAfter(cutoff)) {
                comment.setCommentDeleted(false);
                comment.setCommentDeletionTimestamp(null);
                this.commentRepository.save(comment); // Save each comment update
            }
        });

        System.out.println("Data restoration completed for user: " + user.getEmail());
    }

    /**
     * Sends an email to notify the user about data restoration in progress.
     */
    private void sendRestorationEmail(User user) {
        String subject = "Your Account Data is Being Restored";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Your account has been successfully activated. We are currently restoring your posts and comments that were deactivated. " +
                        "You will see them in your account shortly.\n\n" +
                        "Thank you for your patience.\n\n" +
                        "Best regards,\nSupport Team",
                user.getName()
        );
        this.emailService.sendEmail(
                MailBody.builder()
                        .to(user.getEmail())
                        .subject(subject)
                        .text(body)
                        .build()
        );
    }
}
