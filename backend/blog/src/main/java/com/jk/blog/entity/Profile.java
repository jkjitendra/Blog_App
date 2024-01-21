package com.jk.blog.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Table(name = "profiles")
@NoArgsConstructor
@Getter
@Setter
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    private String mobile;
    private String address;
    private String about;
    private String image;
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "json", nullable = false, updatable = true, name = "socialMediaLinks")
    private List<String> socialMediaLinks;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;
}
