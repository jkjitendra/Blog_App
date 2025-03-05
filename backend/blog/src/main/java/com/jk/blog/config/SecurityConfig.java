package com.jk.blog.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.constants.SecurityConstants;
import com.jk.blog.entity.OAuthUser;
import com.jk.blog.entity.RefreshToken;
import com.jk.blog.entity.Role;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.OAuthUserRepository;
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

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthUserRepository oAuthUserRepository;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

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
                      .successHandler((request, response, authentication) -> {
                          response.setContentType("application/json");
                          response.setCharacterEncoding("UTF-8");

                          OAuth2User user = (OAuth2User) authentication.getPrincipal();
                          String token = user.getAttribute("token");
                          String refreshToken = user.getAttribute("refresh_token");

                          ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                                  .httpOnly(true)
                                  .secure(isCookieSecure) // Set to false for local testing
                                  .path("/")
                                  .maxAge(refreshExpirationTime/1000) // 7 days
                                  .sameSite("Strict")
                                  .build();
                          response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

                          response.getWriter().write("{\"token\":\"" + token + "\"}");
                          response.getWriter().flush();
                      })
              )
              .csrf(csrf -> csrf.ignoringRequestMatchers(toH2Console()).disable())
//              .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
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

        Object providerIdObj = oAuth2User.getAttribute("sub") != null
                ? oAuth2User.getAttribute("sub")
                : oAuth2User.getAttribute("id");

        String providerId = providerIdObj != null ? providerIdObj.toString() : null;

        if (email == null && "github".equals(provider)) {
            String accessToken = userRequest.getAccessToken().getTokenValue();
            email = fetchGitHubEmail(accessToken);
            System.out.println("Email fetched from GitHub: " + email);
        }

        if (email == null) {
            throw new RuntimeException("OAuth authentication failed: No email received.");
        }

        String finalEmail = email;

        // Create or update OAuth user
        OAuthUser user = oAuthUserRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    OAuthUser newUser = OAuthUser.builder()
                            .email(finalEmail)
                            .provider(provider)
                            .providerId(providerId)
                            .name(name)
                            .roles(Set.of(Role.builder().name("ROLE_USER").build())) // Default role
                            .build();
                    return oAuthUserRepository.save(newUser);
                });

        // Generate JWT Token
        String jwtToken = jwtUtil.generateToken(email);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

        //  Return OAuth2User with JWT & Refresh Token
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("token", jwtToken);
        attributes.put("refresh_token", refreshToken.getRefreshToken());

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRoles().toString()));

        // Return appropriate user type
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

            // Parse the response JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            for (JsonNode emailNode : rootNode) {
                if (emailNode.get("primary").asBoolean() && emailNode.get("verified").asBoolean()) {
                    return emailNode.get("email").asText();
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch GitHub email: " + e.getMessage());
        }
        return null;
    }
}