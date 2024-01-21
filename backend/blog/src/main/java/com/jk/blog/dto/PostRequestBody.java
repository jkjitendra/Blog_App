package com.jk.blog.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostRequestBody {

    @NotEmpty
    @Size(min = 4, message = "title should be greater than 4")
    private String title;
    @NotEmpty
    @Size(min = 10, message = "content should be greater than 10")
    private String content;
    private String imageUrl;
    private String videoUrl;
    private Long userId;
    private Long categoryId;
}
