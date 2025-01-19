package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldUpdateNotAllowedException extends RuntimeException {

    public FieldUpdateNotAllowedException(String message) {
        super(message);
    }

}