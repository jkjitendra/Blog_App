package com.jk.blog.repository;

import com.jk.blog.entity.RefreshToken;
import com.jk.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);

    void deleteByRefreshToken(String refreshToken);
}
