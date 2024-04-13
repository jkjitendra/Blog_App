package com.jk.blog.repository;

import com.jk.blog.dto.CommentResponseBody;
import com.jk.blog.entity.Comment;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<List<Comment>> findByPost_PostId(Long postId);

    List<Comment> findTop2ByPost_PostIdOrderByCreatedDateDesc(Long postId);

    @Query("SELECT c FROM Comment c WHERE c.isCommentDeleted = true AND c.commentDeletionTimestamp <= :cutoff")
    List<Comment> findCommentsEligibleForPermanentDeletion(Instant cutoff);

    @Query("SELECT c FROM Comment c WHERE c.user.userId = :userId")
    List<Comment> findByUserId(@Param("userId") Long userId);

//    @Query("SELECT c FROM Comment c WHERE c.post.user = :user")
//    List<Comment> findByUserPosts(@Param("user") User user);
}
