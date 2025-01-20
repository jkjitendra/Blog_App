package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnAuthorizedException extends RuntimeException {

    public UnAuthorizedException(String message, Object... args) {
        super(String.format(message, args));
    }
}
