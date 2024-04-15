package com.jk.blog.repository;

import com.jk.blog.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser_UserId(Long userId);

    @Query("SELECT p.user.userId FROM Profile p WHERE p.profileId = :profileId")
    Long findUserIdByProfileId(@Param("profileId") Long profileId);
}
