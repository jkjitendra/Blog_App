package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccountAlreadyActiveException extends RuntimeException {
    public UserAccountAlreadyActiveException(String message) {
        super(message);
    }
}