package com.jk.blog.dto.post;

import com.jk.blog.dto.APIResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostStatusResponse {

    private APIResponse message;
    private PostResponseBody post;
}
