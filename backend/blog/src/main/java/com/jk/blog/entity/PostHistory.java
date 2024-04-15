package com.jk.blog.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.Instant;

@Entity
@Table(name = "post_history")
@Getter
@Setter
@NoArgsConstructor
public class PostHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Column(nullable = false)
    private Long postId;

    @Type(JsonStringType.class)
    @Column(columnDefinition = "json")
    private String postHistoryJsonData;

    @Column(nullable = false)
    private Instant historyCreatedAt = Instant.now();
}
