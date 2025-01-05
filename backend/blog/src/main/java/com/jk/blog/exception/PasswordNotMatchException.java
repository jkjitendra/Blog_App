package com.jk.blog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordNotMatchException extends RuntimeException {
    private String resourceName;
    private String fieldName;
    private String fieldValue;

    public PasswordNotMatchException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s %s does not match with: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public PasswordNotMatchException(String fieldName, String fieldValue) {
        super(String.format("%s does not match with repeatPassword : %s", fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}