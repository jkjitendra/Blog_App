package com.jk.blog.dto.reaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReactionSummaryResponse {

    private Map<String, Long> emojis;
    private Long totalReactions;
}
