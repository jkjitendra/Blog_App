package com.jk.blog.exception;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenExpiredException extends RuntimeException {
    private String token;
    private String expiryDate;
    private String source;

    public TokenExpiredException(String source) {
        super(String.format("%s Token expired", source));
        this.source = source;
    }

    public TokenExpiredException(String token, String expiryDate) {
        super(String.format("Token: %s expired at: %s", token, expiryDate));
        this.token = token;
        this.expiryDate = expiryDate;
    }
}