package com.jk.blog.service.impl;


import com.jk.blog.entity.RefreshToken;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.TokenExpiredException;
import com.jk.blog.repository.RefreshTokenRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.RefreshTokenService;
import com.jk.blog.utils.GeneratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-expiration-time}")
    private long refreshExpirationTime;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Check if the user already has a refresh token
        RefreshToken existingToken = refreshTokenRepository.findByUser(user).orElse(null);

        if (existingToken != null) {
            // Update existing refresh token
            existingToken.setRefreshToken(GeneratorUtils.generateRefreshToken());
            existingToken.setExpirationTime(Instant.now().plusSeconds(refreshExpirationTime/1000));
            return refreshTokenRepository.save(existingToken);
        }

        // If no existing refresh token, Create a new one
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(GeneratorUtils.generateRefreshToken());
        refreshToken.setExpirationTime(Instant.now().plusSeconds(refreshExpirationTime/1000));
        refreshToken.setUser(user);

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken verifyRefreshToken(String refreshToken) {
        RefreshToken rfToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("RefreshToken", "refreshToken", refreshToken));

        if (rfToken.getExpirationTime().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rfToken);
            throw new TokenExpiredException("refreshToken");
        }

        // Force initialization of permissions to avoid LazyInitializationException
//        rfToken.getUser().getRoles().forEach(role -> Hibernate.initialize(role.getPermissions()));

        return rfToken;
    }

    @Override
    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }
}
