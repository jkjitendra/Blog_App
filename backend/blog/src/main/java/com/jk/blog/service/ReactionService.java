package com.jk.blog.service;

import com.jk.blog.dto.reaction.ReactionSummaryResponse;

public interface ReactionService {

    void reactToPost(Long postId, String emoji);

    void reactToComment(Long postId, Long commentId, String emoji);

    ReactionSummaryResponse getReactionCountsForPost(Long postId);

    ReactionSummaryResponse getReactionCountsForComment(Long commentId);
}
