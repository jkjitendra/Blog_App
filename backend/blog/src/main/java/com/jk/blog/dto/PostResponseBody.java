package com.jk.blog.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class PostResponseBody {

    private String title;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private Long userId;
    private Long categoryId;
    private Boolean isLive;
}
