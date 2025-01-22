package com.jk.blog.repository;

import com.jk.blog.entity.AuthHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthHistoryRepository extends JpaRepository<AuthHistory, Long> {

}
