package com.jk.blog.config.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"posts", "comments"})
public interface UserMixin {
}

