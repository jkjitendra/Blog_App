package com.jk.blog.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jk.blog.dto.user.UserStatusResponse;
import com.jk.blog.dto.user.UserMapper;
import com.jk.blog.dto.profile.ProfileResponseBody;
import com.jk.blog.dto.user.UserResponseBody;
import com.jk.blog.entity.*;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.ProfileRepository;
import com.jk.blog.repository.UserHistoryRepository;
import com.jk.blog.repository.UserRepository;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Pointcut("execution(* com.jk.blog.service.UserService.createUser(..)) || execution(* com.jk.blog.service.UserService.updateUser(..)) || execution(* com.jk.blog.service.UserService.patchUser(..)) || " +
              "execution(* com.jk.blog.service.UserService.deactivateUserAccount(..)) || execution(* com.jk.blog.service.UserService.activateUserAccount(..)) || " +
              "execution(* com.jk.blog.service.ProfileService.updateProfile(..)) || execution(* com.jk.blog.service.ProfileService.patchProfile(..))")
    public void userModificationMethods() {}

    @AfterReturning(pointcut = "userModificationMethods()", returning = "result")
    public void logUserHistory(JoinPoint joinPoint, Object result) throws JsonProcessingException {

        try {
            System.out.println("Aspect triggered for: " + joinPoint.getSignature());

            if (result instanceof UserResponseBody) {
                logUser(((UserResponseBody) result).getId());
            } else if (result instanceof ProfileResponseBody) {
                logProfile(((ProfileResponseBody) result).getProfileId());
            } else if (result instanceof UserStatusResponse) {
                logUser(((UserStatusResponse) result).getUser().getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logUser(Long userId) throws JsonProcessingException {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        UserResponseBody userResponseBody = UserMapper.userToUserResponseBody(user);
        String jsonData = objectMapper.writeValueAsString(userResponseBody);
        logHistory(user.getUserId(), jsonData);
    }

    private void logProfile(Long profileId) throws JsonProcessingException {
        Long userId = this.profileRepository.findUserIdByProfileId(profileId);

        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        UserResponseBody userResponseBody = UserMapper.userToUserResponseBody(user);
        String jsonData = objectMapper.writeValueAsString(userResponseBody);
        logHistory(user.getUserId(), jsonData);
    }

    private void logHistory(Long userId, String jsonData) {
        UserHistory history = new UserHistory();
        history.setUserId(userId);
        history.setUserHistoryJsonData(jsonData);
        history.setHistoryCreatedAt(Instant.now());
        userHistoryRepository.save(history);
    }

}

