package com.jk.blog.repository;

import com.jk.blog.entity.PasswordResetToken;
import com.jk.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByOtpAndUser(Integer otp, User user);

    @Query("SELECT p FROM PasswordResetToken p WHERE p.user = :user AND p.verified = true")
    Optional<PasswordResetToken> findByUserAndVerified(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.user = :user")
    void deleteByUser(User user);

}