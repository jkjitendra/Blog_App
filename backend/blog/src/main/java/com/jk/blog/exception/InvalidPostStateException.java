package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidPostStateException extends RuntimeException {
    public InvalidPostStateException(String message) {
        super(message);
    }
}