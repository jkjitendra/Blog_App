package com.jk.blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-time}")
    private long expiration;


    private Key getSignKey() {
      byte[] keyBytes= Decoders.BASE64.decode(secret);
      return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
      Map<String, Object> claims = new HashMap<>();
      return createToken(claims, email);
    }
    private String createToken(Map<String, Object> claims, String email) {
      return Jwts.builder()
              .setClaims(claims)
              .setSubject(email)
              .setIssuedAt(new Date(System.currentTimeMillis()))
              .setExpiration(new Date(System.currentTimeMillis() + expiration))
              .signWith(getSignKey(), SignatureAlgorithm.HS256)
              .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
      }

      public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
      }

      public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
      }

      public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
      }

      private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
      }

      private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
