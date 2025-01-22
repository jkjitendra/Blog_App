package com.jk.blog.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.entity.AuthHistory;
import com.jk.blog.repository.AuthHistoryRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
public class AuthLoggingAspect {

    @Autowired
    private AuthHistoryRepository authHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Pointcut for AuthController methods
    @Pointcut("execution(* com.jk.blog.controller.AuthController.*(..))")
    public void authControllerMethods() {}

    @AfterReturning(pointcut = "authControllerMethods()", returning = "result")
    public void logAuthActions(JoinPoint joinPoint, Object result) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String jsonData = objectMapper.writeValueAsString(result);

            AuthHistory authHistory = new AuthHistory();
            authHistory.setAction(methodName);
            authHistory.setActionData(jsonData);
            authHistory.setActionTimestamp(Instant.now());

            this.authHistoryRepository.save(authHistory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

