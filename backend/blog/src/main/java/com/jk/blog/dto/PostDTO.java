package com.jk.blog.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PostDTO {

    private String title;
    private String content;
    private String imageName;
    private String videoName;
    private Long userId;
    private Long categoryId;
    private Boolean isLive;
}
