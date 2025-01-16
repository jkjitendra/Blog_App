package com.jk.blog.aop;

import com.jk.blog.dto.MailBody;
import com.jk.blog.entity.User;
import com.jk.blog.service.EmailService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserAccountAspect {

    private final EmailService emailService;

    public UserAccountAspect(EmailService emailService) {
        this.emailService = emailService;
    }

    @AfterReturning(value = "execution(* com.jk.blog.service.UserService.deactivateUserAccount(..))", returning = "user")
    public void sendDeactivationEmail(User user) {
        if (user != null) {
            String subject = "Account Deactivated";
            String body = "Hello " + user.getName() + ",\n\nYour account has been deactivated successfully.";
            emailService.sendEmail(MailBody.builder().to(user.getEmail()).subject(subject).text(body).build());
        }
    }

    @AfterReturning(value = "execution(* com.jk.blog.service.UserService.activateUserAccount(..))", returning = "user")
    public void sendActivationEmail(User user) {
        if (user != null) {
            String subject = "Account Activated";
            String body = "Hello " + user.getName() + ",\n\nYour account has been reactivated successfully.";
            emailService.sendEmail(MailBody.builder().to(user.getEmail()).subject(subject).text(body).build());
        }
    }
}

