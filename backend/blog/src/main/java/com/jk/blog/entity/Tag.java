package com.jk.blog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    @Column(name = "tag_name", nullable = false, unique = true)
    private String tagName;

    @ManyToMany(mappedBy = "tags", cascade = CascadeType.MERGE)
    private Set<Post> posts = new HashSet<>();
}
