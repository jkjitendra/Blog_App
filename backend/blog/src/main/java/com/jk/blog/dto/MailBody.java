package com.jk.blog.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailBody {

    private String to;

    private String subject;

    private String text;

}
