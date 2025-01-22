package com.jk.blog.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.Instant;

@Entity
@Table(name = "auth_history")
@Getter
@Setter
@NoArgsConstructor
public class AuthHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // Name of the authentication action (e.g., "login", "logout", "register").

    @Type(JsonStringType.class)
    @Column(columnDefinition = "json")
    private String actionData;

    @Column(nullable = false)
    private Instant actionTimestamp; // Timestamp of when the action occurred.
}
