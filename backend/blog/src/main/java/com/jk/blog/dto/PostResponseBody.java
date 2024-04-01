package com.jk.blog.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
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

    private Long userId;

    private Long categoryId;

    private Boolean isLive;

    private Set<String> tagNames = new HashSet<>();

}
