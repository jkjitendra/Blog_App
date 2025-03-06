package com.jk.blog.oauth.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.oauth.OAuthProfileImageFetcher;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("githubProfileImageFetcher")
public class GitHubProfileImageFetcher implements OAuthProfileImageFetcher {

    @Override
    public String fetchProfileImage(String accessToken) {
        try {
            String url = "https://api.github.com/user";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            System.out.println("Fetched image from GitHub");
            return rootNode.get("avatar_url").asText(); // GitHub profile image URL
        } catch (Exception e) {
            System.out.println("Failed to fetch GitHub profile image: " + e.getMessage());
        }
        return null;
    }
}