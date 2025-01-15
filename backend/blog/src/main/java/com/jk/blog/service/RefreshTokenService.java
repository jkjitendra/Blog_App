package com.jk.blog.service;


import com.jk.blog.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String login);

    RefreshToken verifyRefreshToken(String refreshToken);

    void deleteRefreshToken(String refreshToken);
}
