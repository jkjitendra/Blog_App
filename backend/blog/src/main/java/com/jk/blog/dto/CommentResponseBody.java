package com.jk.blog.dto;


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
    private Date createdDate;
    private Date lastUpdatedDate;
    private Long userId;
    private Long postId;
}
