package com.jk.blog.repository;

import com.jk.blog.entity.Category;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUser(User user);

    @Query("SELECT p FROM Post p WHERE p.user.userId IN :userIds")
    List<Post> findByUserIds(@Param("userIds") Set<Long> userIds);

    Page<Post> findByUser(User user, Pageable pageable);

    Page<Post> findByCategory(Category category, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.postTitle like :patternKey")
    Page<Post> searchKeyOnTitle(@Param("patternKey") String searchKey, Pageable pageable);

    // Fetch only public posts for non-subscribers
    @Query("SELECT p FROM Post p WHERE p.isMemberPost = false AND p.isLive = true AND p.isPostDeleted = false")
    Page<Post> findPublicPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isPostDeleted = false OR (p.isPostDeleted = true AND p.postDeletionTimestamp > :cutoff)")
    List<Post> findActiveOrRecoverablePosts(Instant cutoff);

    @Modifying
    @Query("DELETE FROM Post p WHERE p.isPostDeleted = true AND p.postDeletionTimestamp <= :cutoff")
    void deletePostsEligibleForPermanentDeletion(@Param("cutoff") Instant cutoff);

    // Fetch post by ID ensuring only live, non-deleted posts
    @Query("SELECT p FROM Post p WHERE p.postId = :postId AND p.isLive = true AND p.isPostDeleted = false")
    Optional<Post> findByPostIdAndIsLiveTrueAndIsPostDeletedFalse(@Param("postId") Long postId);

    // Fetch all posts (public + member) for authenticated users
    Page<Post> findByIsLiveTrueAndIsPostDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isArchived = false AND p.isLive = true AND p.isPostDeleted = false")
    Page<Post> findActiveAndUnarchivedPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.postId = :postId AND p.isLive = true AND p.isArchived = false AND p.isPostDeleted = false")
    Optional<Post> findActiveAndUnarchivedPostsById(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p WHERE p.isArchived = true AND p.isLive = true AND p.isPostDeleted = false")
    Page<Post> findActiveAndArchivedPosts(Pageable pageable);

}
