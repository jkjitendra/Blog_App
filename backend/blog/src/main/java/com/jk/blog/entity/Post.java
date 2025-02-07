package com.jk.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(length = 100, nullable = false)
    private String postTitle;

    @Lob
    @Column(columnDefinition="LONGTEXT")
    private String postContent;

    private String imageUrl;
    private String videoUrl;

    private Instant postCreatedDate;

    private Instant postLastUpdatedDate;

    private boolean isPostDeleted = false;

    private boolean isArchived = false;

    private Instant postDeletionTimestamp;

    @Column(nullable = false)
    private boolean isMemberPost;

    @Column(nullable = false)
    private boolean isLive = true; // Default value set to true

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

}
