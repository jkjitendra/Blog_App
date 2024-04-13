package com.jk.blog.repository;

import com.jk.blog.entity.Category;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUser(User user);

    List<Post> findByCategory(Category category);

    @Query("SELECT p FROM Post p WHERE p.postTitle like :patternKey")
    List<Post> searchKeyOnTitle(@Param("patternKey") String searchKey);

    @Query("SELECT p FROM Post p WHERE p.isPostDeleted = false OR (p.isPostDeleted = true AND p.postDeletionTimestamp > :cutoff)")
    List<Post> findActiveOrRecoverablePosts(Instant cutoff);

    @Query("SELECT p FROM Post p WHERE p.isPostDeleted = true AND p.postDeletionTimestamp <= :cutoff")
    List<Post> findPostsEligibleForPermanentDeletion(Instant cutoff);

    Optional<Post> findByPostIdAndIsLiveTrueAndIsPostDeletedFalse(Long postId);

    Page<Post> findByIsLiveTrueAndIsPostDeletedFalse(Pageable pageable);
}
