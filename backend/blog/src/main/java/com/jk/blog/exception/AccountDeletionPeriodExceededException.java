package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDeletionPeriodExceededException extends RuntimeException {
    public AccountDeletionPeriodExceededException(String message) {
        super(message);
    }
}