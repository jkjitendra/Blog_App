package com.jk.blog.dto.post;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class PostRequestBody {

    @NotBlank
    @Size(min = 4, message = "title should be greater than 4")
    private String title;

    @NotBlank
    @Size(min = 10, message = "content should be greater than 10")
    private String content;

    private Boolean isMemberPost;

    private String imageUrl;

    private String videoUrl;

    private Long userId;

    private Long categoryId;

    private Set<String> tagNames = new HashSet<>();

}
