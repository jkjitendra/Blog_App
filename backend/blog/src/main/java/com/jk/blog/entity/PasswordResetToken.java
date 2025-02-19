  package com.jk.blog.entity;

  import jakarta.persistence.*;
  import lombok.Getter;
  import lombok.Setter;

  import java.time.Instant;

  @Entity
  @Getter
  @Setter
  public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private Instant expirationTime;

    private boolean verified;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

  }