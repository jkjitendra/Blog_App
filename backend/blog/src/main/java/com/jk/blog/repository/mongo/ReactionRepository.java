package com.jk.blog.repository.mongo;

import com.jk.blog.entity.ReactionModel;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ReactionRepository extends MongoRepository<ReactionModel, String> {

    Optional<ReactionModel> findByUserIdAndPostId(Long userId, Long postId);

    Optional<ReactionModel> findByUserIdAndCommentId(Long userId, Long commentId);

    List<ReactionModel> findByPostId(Long postId);

    List<ReactionModel> findByCommentId(Long commentId);

    @Aggregation(pipeline = {
            "{ '$match': { 'postId': ?0, 'type': 'post' } }",
            "{ '$group': { '_id': '$emoji', 'count': { '$sum': 1 } } }"
    })
    List<Map<String, Object>> getReactionCountsByPostId(Long postId);

    @Aggregation(pipeline = {
            "{ '$match': { 'commentId': ?0, 'type': 'comment' } }",
            "{ '$group': { '_id': '$emoji', 'count': { '$sum': 1 } } }"
    })
    List<Map<String, Object>> getReactionCountsByCommentId(Long commentId);

}
