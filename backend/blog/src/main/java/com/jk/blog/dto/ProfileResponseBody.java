package com.jk.blog.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProfileResponseBody {

    private Long profileId;

    private String address;

    private String about;

    private String imageUrl;

    private List<String> socialMediaLinks;
}
