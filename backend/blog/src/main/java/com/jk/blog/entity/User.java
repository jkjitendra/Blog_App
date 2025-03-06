package com.jk.blog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = true)
    private String userName;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "name can't be blank")
    private String name;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "email can't be blank")
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true)
    private String mobile;

    @Column(nullable = true)
    private String countryName;

    @Column(nullable = false)
    private String provider; // Values: "local", "google", "github", "facebook"

    @Column(nullable = true) // Only for OAuth users
    private String providerId;

    private Instant userCreatedDate;
    private Instant userLastLoggedInDate;
    private boolean isUserDeleted = false;
    private Instant userDeletionTimestamp;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Profile profile;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private PasswordResetToken passwordResetToken;

    public void updateLastLoggedIn() {
        this.userLastLoggedInDate = Instant.now();
    }

    // Fetch permissions dynamically from roles
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add permissions as authorities
        authorities.addAll(roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toSet()));

        // Add roles as authorities (Spring Security expects roles to be prefixed with "ROLE_")
        authorities.addAll(roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet()));

        return authorities;
    }

    @Override
    public String getPassword() {
        return "local".equals(provider) ? password : "";
    }
    @Override
    public String getUsername() {
        return email; // Always use email for authentication as userName is not always available for OAuth users.
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
}
