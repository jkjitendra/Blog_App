package com.jk.blog.dto.comment;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CommentResponseBody {

    private Long commentId;

    private String commentDesc;

    private String commentCreatedDate;

    private String commentLastUpdatedDate;

    private boolean isCommentDeleted;

    private String commentDeletionTimestamp;

    private Long userId;

    private Long postId;
}
