package com.jk.blog.exception;


public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }
}