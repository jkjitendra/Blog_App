package com.jk.blog.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.dto.user.UserStatusResponse;
import com.jk.blog.entity.UserHistory;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.ProfileRepository;
import com.jk.blog.repository.UserHistoryRepository;
import com.jk.blog.repository.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
public class UserProfileHistoryLoggingAspect {

    @Autowired
    private UserHistoryRepository userHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Pointcut for UserController methods related to user updates
    @Pointcut("execution(* com.jk.blog.controller.UserController.updateUser(..)) || " +
            "execution(* com.jk.blog.controller.UserController.updatePassword(..)) || " +
            "execution(* com.jk.blog.controller.UserController.deactivateUser(..)) || " +
            "execution(* com.jk.blog.controller.UserController.activateUser(..)) || " +
            "execution(* com.jk.blog.controller.ProfileController.updateProfile(..)) || " +
            "execution(* com.jk.blog.controller.ProfileController.patchProfile(..))")
    public void userProfileModificationMethods() {}

    @AfterReturning(pointcut = "userProfileModificationMethods()", returning = "result")
    public void logUserHistory(JoinPoint joinPoint, Object result) throws JsonProcessingException {
        try {
            System.out.println("Aspect triggered for: " + joinPoint.getSignature());

            if (result instanceof UserResponseBody userResponseBody) {
                logUser(userResponseBody.getId());
            } else if (result instanceof ProfileResponseBody profileResponseBody) {
                logProfile(profileResponseBody.getProfileId());
            } else if (result instanceof UserStatusResponse userStatusResponse) {
                logUser(userStatusResponse.getUser().getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logUser(Long userId) throws JsonProcessingException {
        var user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        var jsonData = objectMapper.writeValueAsString(user);
        logHistory(userId, jsonData);
    }

    private void logProfile(Long profileId) throws JsonProcessingException {
        Long userId = this.profileRepository.findUserIdByProfileId(profileId);

        var user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        var jsonData = objectMapper.writeValueAsString(user);
        logHistory(userId, jsonData);
    }

    private void logHistory(Long userId, String jsonData) {
        UserHistory history = new UserHistory();
        history.setUserId(userId);
        history.setUserHistoryJsonData(jsonData);
        history.setHistoryCreatedAt(Instant.now());
        userHistoryRepository.save(history);
    }

}

