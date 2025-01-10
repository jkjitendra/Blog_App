package com.jk.blog.service;


import com.jk.blog.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String email);

    RefreshToken verifyRefreshToken(String refreshToken);

    void deleteRefreshToken(String refreshToken);
}
