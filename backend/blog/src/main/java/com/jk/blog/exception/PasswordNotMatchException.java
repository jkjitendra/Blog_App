package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordNotMatchException extends RuntimeException {
    public PasswordNotMatchException(String message) {
        super(message);
    }
}