package com.jk.blog.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequestBody {

    @NotBlank
    @Size(min = 10, message = "content should be greater than 10")
    private String commentDesc;

    private Long userId;

    private Long postId;
}
