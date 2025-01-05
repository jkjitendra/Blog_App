package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidTokenException extends RuntimeException {
    private String token;
    private String message;

    public InvalidTokenException(String token, String message) {
        super(String.format("Invalid token: %s. Reason: %s", token, message));
        this.token = token;
        this.message = message;
    }
}