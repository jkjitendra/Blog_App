package com.jk.blog.dto.post;

import com.jk.blog.dto.comment.CommentResponseBody;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class PostResponseBody {

    private Long postId;

    private String title;

    private String content;

    private String imageUrl;

    private String videoUrl;

    private boolean isMemberPost;

    private String postCreatedDate;

    private String postLastUpdatedDate;

    private boolean isPostDeleted;

    private String postDeletionTimestamp;

    private Long userId;

    private Long categoryId;

    private Boolean isLive;

    private Set<String> tagNames = new HashSet<>();

    private List<CommentResponseBody> comments;

}
