package com.jk.blog.repository;

import com.jk.blog.entity.PostHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHistoryRepository extends JpaRepository<PostHistory, Long> {
}