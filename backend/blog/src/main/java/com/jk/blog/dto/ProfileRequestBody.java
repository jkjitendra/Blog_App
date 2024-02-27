package com.jk.blog.dto;


import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProfileRequestBody {

    private String address;

    private String about;

    private String imageUrl;

    private List<String> socialMediaLinks;
}
