package com.jk.blog.service.impl;

import com.jk.blog.dto.reaction.ReactionSummaryResponse;
import com.jk.blog.entity.ReactionModel;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.mongo.ReactionRepository;
import com.jk.blog.service.ReactionService;
import com.jk.blog.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReactionServiceImpl implements ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private PostRepository postRepository;

    public void reactToPost(Long postId, String emoji) {

        // Get the authenticated user
        User user = AuthUtil.getAuthenticatedUser();
        if (user == null) {
            throw new UnAuthorizedException("User must be logged in to create a post.");
        }

        validatePostExists(postId);

        ReactionModel existingReaction = this.reactionRepository.findByUserIdAndPostId(user.getUserId(), postId).orElse(null);

        if (existingReaction != null) {
            // Update reaction
            existingReaction.setEmoji(emoji);
            this.reactionRepository.save(existingReaction);
        } else {
            // Create a new reaction
            ReactionModel reaction = new ReactionModel();
            reaction.setPostId(postId);
            reaction.setUserId(user.getUserId());
            reaction.setEmoji(emoji);
            reaction.setType("post");
            this.reactionRepository.save(reaction);
        }
    }

    public void reactToComment(Long postId, Long commentId, String emoji) {

        validatePostExists(postId);

        // Get the authenticated user
        User user = AuthUtil.getAuthenticatedUser();
        if (user == null) {
            throw new UnAuthorizedException("User must be logged in to create a post.");
        }

        // Check if a reaction already exists in mongodb
        ReactionModel existingReaction = this.reactionRepository
                                             .findByUserIdAndCommentId(user.getUserId(), commentId)
                                             .orElse(null);

        if (existingReaction != null) {
            // Update reaction in mongodb
            existingReaction.setEmoji(emoji);
            this.reactionRepository.save(existingReaction);
        } else {
            // Create a new reaction in mongodb
            ReactionModel reaction = new ReactionModel();
            reaction.setCommentId(commentId);
            reaction.setUserId(user.getUserId());
            reaction.setEmoji(emoji);
            reaction.setType("comment");
            this.reactionRepository.save(reaction);
        }
    }

    @Override
    public ReactionSummaryResponse getReactionCountsForPost(Long postId) {
        List<Map<String, Object>> reactionCounts = reactionRepository.getReactionCountsByPostId(postId);
        return buildReactionSummary(reactionCounts);
    }

    @Override
    public ReactionSummaryResponse getReactionCountsForComment(Long commentId) {
        List<Map<String, Object>> reactionCounts = reactionRepository.getReactionCountsByCommentId(commentId);
        return buildReactionSummary(reactionCounts);
    }

    private ReactionSummaryResponse buildReactionSummary(List<Map<String, Object>> reactionCounts) {
        Map<String, Long> emojiCounts = reactionCounts.stream()
                .collect(Collectors.toMap(
                        entry -> entry.get("_id").toString(),
                        entry -> ((Number) entry.get("count")).longValue()
                ));

        long totalReactions = emojiCounts.values().stream().mapToLong(Long::longValue).sum();

        return new ReactionSummaryResponse(emojiCounts, totalReactions);
    }

    private void validatePostExists(Long postId) {
        // Validate if the post exists in the SQL database
        boolean postExists = this.postRepository.existsById(postId);

        if (!postExists) {
            throw new ResourceNotFoundException("Post", "postId", postId);
        }
    }

}
