package com.jk.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private Object errorDetails;

    public APIResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public APIResponse(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}

