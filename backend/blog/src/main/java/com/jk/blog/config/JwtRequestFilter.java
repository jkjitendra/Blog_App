package com.jk.blog.config;

import com.jk.blog.entity.RefreshToken;
import com.jk.blog.entity.User;
import com.jk.blog.exception.TokenExpiredException;
import com.jk.blog.service.RefreshTokenService;
import com.jk.blog.service.impl.CustomUserDetailsService;
import com.jk.blog.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  @Autowired
  private CustomUserDetailsService userDetailsService;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header before check: " + authorizationHeader);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authorizationHeader.substring(7);
        final String username;
        System.out.println("Authorization Header:after check " + authorizationHeader);
        System.out.println("JwtToken Header: " + jwtToken);

        try {
            username = jwtUtil.extractUsername(jwtToken);
            System.out.println("Extracted username from token: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("UserDetails loaded: " + userDetails.getUsername());

                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    System.out.println("Token is valid. Setting authentication in SecurityContextHolder.");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("AuthToken set in SecurityContextHolder.");
                    System.out.println("SecurityContextHolder Authentication: " +
                            SecurityContextHolder.getContext().getAuthentication());

                } else {
                    System.out.println("Token validation failed.");
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            // Handle token refresh logic
            System.out.println("Token expired. Attempting to refresh token.");
            handleTokenRefresh(request, response, filterChain);
        } catch (Exception ex) {
            System.out.println("Exception during token validation: " + ex.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private void handleTokenRefresh(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String refreshToken = null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
            }
        }

        if (refreshToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            RefreshToken verifiedRefreshToken = refreshTokenService.verifyRefreshToken(refreshToken);
            User user = verifiedRefreshToken.getUser();
            String newAccessToken = jwtUtil.generateToken(user.getEmail());

            response.setHeader("Authorization", "Bearer " + newAccessToken);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (TokenExpiredException ex) {
            refreshTokenService.deleteRefreshToken(refreshToken);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
