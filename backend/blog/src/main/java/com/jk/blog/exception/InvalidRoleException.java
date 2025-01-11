package com.jk.blog.exception;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException(String message) {
        super(message);
    }
}
