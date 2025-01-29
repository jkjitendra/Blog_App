package com.jk.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reactions")
public class ReactionModel {

    @Id
    private String id;
    private Long postId;
    private Long commentId;
    private Long userId;
    private String emoji;
    private String type; // "post" or "comment"

}
