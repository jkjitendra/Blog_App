package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailSendingException extends RuntimeException {
    private String recipientEmail;

    public EmailSendingException(String recipientEmail, String message) {
        super(String.format("Failed to send email to %s. Reason: %s", recipientEmail, message));
        this.recipientEmail = recipientEmail;
    }
}
