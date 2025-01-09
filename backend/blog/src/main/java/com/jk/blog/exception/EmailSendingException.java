package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailSendingException extends RuntimeException {
    private String recipientEmail;
    private String message;

    public EmailSendingException(String recipientEmail, String message) {
        super(String.format("Failed to send email to %s. Reason: %s", recipientEmail, message));
        this.recipientEmail = recipientEmail;
        this.message = message;
    }

    public EmailSendingException(String recipientEmail, String message, Throwable cause) {
        super(String.format("Failed to send email to %s. Reason: %s", recipientEmail, message), cause);
        this.recipientEmail = recipientEmail;
        this.message = message;
    }
}
