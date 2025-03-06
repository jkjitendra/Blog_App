package com.jk.blog.oauth.impl;

import com.jk.blog.oauth.OAuthProfileImageFetcher;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("googleProfileImageFetcher")
public class GoogleProfileImageFetcher implements OAuthProfileImageFetcher {

    @Override
    public String fetchProfileImage(String accessToken) {
        try {
            String url = "https://people.googleapis.com/v1/people/me?personFields=photos";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            System.out.println("Fetched image from Google");
            return rootNode.get("photos").get(0).get("url").asText();
        } catch (Exception e) {
            System.out.println("Failed to fetch Google profile image: " + e.getMessage());
        }
        return null;
    }
}