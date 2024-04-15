package com.jk.blog.config.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"user"})
public interface ProfileMixin {
}
