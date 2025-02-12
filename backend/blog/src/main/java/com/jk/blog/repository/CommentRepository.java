package com.jk.blog.repository;

import com.jk.blog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.user.userId IN :userIds")
    List<Comment> findByUserIds(@Param("userIds") Set<Long> userIds);


    Optional<List<Comment>> findByPost_PostId(Long postId);

    List<Comment> findTop2ByPost_PostIdOrderByCommentCreatedDateDesc(Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.isCommentDeleted = true AND c.commentDeletionTimestamp <= :cutoff")
    void deleteCommentsEligibleForPermanentDeletion(@Param("cutoff") Instant cutoff);

    @Query("SELECT c FROM Comment c WHERE c.user.userId = :userId")
    List<Comment> findByUserId(@Param("userId") Long userId);

}
