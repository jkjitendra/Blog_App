package com.jk.blog.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.Instant;

@Entity
@Table(name = "user_history")
@Getter
@Setter
@NoArgsConstructor
public class UserHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Column(nullable = false)
    private Long userId;

    @Type(JsonStringType.class)
    @Column(columnDefinition = "json")
    private String userHistoryJsonData;

    @Column(nullable = false)
    private Instant historyCreatedAt = Instant.now();
}
