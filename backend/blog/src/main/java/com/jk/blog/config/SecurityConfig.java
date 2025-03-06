package com.jk.blog.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.constants.SecurityConstants;
import com.jk.blog.entity.Profile;
import com.jk.blog.entity.RefreshToken;
import com.jk.blog.entity.Role;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.oauth.ProfileImageFetcherFactory;
import com.jk.blog.repository.ProfileRepository;
import com.jk.blog.repository.RoleRepository;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.RefreshTokenService;
import com.jk.blog.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ProfileImageFetcherFactory profileImageFetcherFactory;

    @Value("${app.cookie.secure}")
    private boolean isCookieSecure;

    @Value("${jwt.refresh-expiration-time}")
    private long refreshExpirationTime;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> this.userRepository.findByEmail(username)
                                         .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
  //          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
              .csrf(AbstractHttpConfigurer::disable)
              .cors(AbstractHttpConfigurer::disable)
              .authorizeHttpRequests(authorizeRequests ->
                      authorizeRequests
                              .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
                              .anyRequest().authenticated()
              )
              .headers(headers -> headers
                      .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
              )
              .oauth2Login(oauth2 -> oauth2
                      .userInfoEndpoint(userInfo -> userInfo
                              .oidcUserService(oidcUserService()) // Google (OIDC)
                              .userService(oAuth2UserService()) // Facebook, GitHub (OAuth2)
                      )
//                      .successHandler((request, response, authentication) -> {
//                          response.setContentType("application/json");
//                          response.setCharacterEncoding("UTF-8");
//
//                          OAuth2User user = (OAuth2User) authentication.getPrincipal();
//                          String token = user.getAttribute("token");
//                          String refreshToken = user.getAttribute("refresh_token");
//
//                          ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
//                                  .httpOnly(true)
//                                  .secure(isCookieSecure) // Set to false for local testing
//                                  .path("/")
//                                  .maxAge(refreshExpirationTime/1000) // 7 days
//                                  .sameSite("Strict")
//                                  .build();
//                          response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
//
//                          response.getWriter().write("{\"token\":\"" + token + "\"}");
//                          response.getWriter().flush();
//                      })
              )
              .csrf(csrf -> csrf.ignoringRequestMatchers(toH2Console()).disable())
              .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .logout(LogoutConfigurer::permitAll);

        // Add JWT token filter
        http
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

      return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
      DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
      authenticationProvider.setUserDetailsService(userDetailsService());
      authenticationProvider.setPasswordEncoder(passwordEncoder());
      return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
      return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Custom OIDC User Service for Google Login
     */
    @Bean
    public OidcUserService oidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                OidcUser oidcUser = delegate.loadUser(userRequest);
                return (OidcUser) processOAuthUser(userRequest, oidcUser, "google");
            }
        };
    }

    /**
     * Custom OAuth2 User Service for Facebook, GitHub Login
     */
    @Bean
    public DefaultOAuth2UserService oAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return new DefaultOAuth2UserService() {
            @Override
            public OAuth2User loadUser(org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest userRequest) {
                OAuth2User oAuth2User = delegate.loadUser(userRequest);
                String provider = userRequest.getClientRegistration().getRegistrationId();
                return processOAuthUser(userRequest, oAuth2User, provider);
            }
        };
    }

    /**
     * Processes the OAuth2 user, assigns roles, and generates JWT token.
     */
    private OAuth2User processOAuthUser(org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest userRequest, OAuth2User oAuth2User, String provider) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String profileImage = "github".equals(provider) ? oAuth2User.getAttribute("avatar_url") : oAuth2User.getAttribute("photos") ;


        Object providerIdObj = oAuth2User.getAttribute("sub") != null
                ? oAuth2User.getAttribute("sub")
                : oAuth2User.getAttribute("id");

        String providerId = providerIdObj != null ? providerIdObj.toString() : null;

        if ("github".equals(provider) && email == null) {
            String accessToken = userRequest.getAccessToken().getTokenValue();
            email = fetchGitHubEmail(accessToken);
            System.out.println("Email fetched from GitHub: " + email);
        }

        if (email == null) {
            throw new RuntimeException("OAuth authentication failed: No email received.");
        }

        String accessToken = userRequest.getAccessToken().getTokenValue();
        String profileImageUrl = profileImage == null ? this.profileImageFetcherFactory.fetchProfileImage(provider, accessToken) : profileImage;

        String finalEmail = email;
        Role defaultRole = this.roleRepository.findByName("ROLE_USUAL")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USUAL"));
        User user = this.userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(finalEmail)
                            .provider(provider)
                            .providerId(providerId)
                            .name(name)
                            .password("") // Explicitly set empty password for OAuth users
                            .roles(Set.of(defaultRole)) // Default role
                            .userCreatedDate(Instant.now())
                            .build();
                    newUser = this.userRepository.save(newUser);

                    Profile profile = Profile.builder()
                            .user(newUser)
                            .imageUrl(profileImageUrl) // Save profile image
                            .build();
                    this.profileRepository.save(profile);
                    return newUser;
                });

        // If user exists, update profile image if changed
        if (profileImageUrl != null) {
            Profile profile = this.profileRepository.findByUser_UserId(user.getUserId())
                    .orElseGet(() -> {
                        Profile newProfile = Profile.builder()
                                .user(user)
                                .imageUrl(profileImageUrl)
                                .build();
                        return this.profileRepository.save(newProfile);
                    });

            // Update image URL if different
            if (!Objects.equals(profile.getImageUrl(), profileImageUrl)) {
                profile.setImageUrl(profileImageUrl);
                this.profileRepository.save(profile);
            }
        }

        String jwtToken = jwtUtil.generateToken(user.getEmail());
        RefreshToken refreshToken = this.refreshTokenService.createRefreshToken(user.getEmail());

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("token", jwtToken);
        attributes.put("refresh_token", refreshToken.getRefreshToken());

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        // Return OIDC or OAuth2 user with JWT
        if (oAuth2User instanceof OidcUser) {
            return new DefaultOAuth2User(authorities, attributes, "sub");
        } else {
            return new DefaultOAuth2User(authorities, attributes, "id");
        }
    }

    private String fetchGitHubEmail(String accessToken) {
        try {
            String url = "https://api.github.com/user/emails";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            for (JsonNode emailNode : rootNode) {
                if (emailNode.has("email") && emailNode.get("primary").asBoolean() && emailNode.get("verified").asBoolean()) {
                    return emailNode.get("email").asText();
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch GitHub email: " + e.getMessage());
        }
        return null;
    }
}