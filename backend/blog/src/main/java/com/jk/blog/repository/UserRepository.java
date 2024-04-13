package com.jk.blog.repository;

import com.jk.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

//    Optional<User> findByResetTokenAndEmail(String resetToken, String email);

    boolean existsByUserName(String userName);

    @Query("SELECT u FROM User u WHERE u.isUserDeleted = true AND u.userDeletionTimestamp < :cutoff")
    List<User> findDeactivatedUsersBefore(Instant cutoff);

}
