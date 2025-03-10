package com.jk.blog.oauth.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.oauth.OAuthProfileImageFetcher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

            if (rootNode.has("photos") && rootNode.get("photos").isArray() && rootNode.get("photos").size() > 0) {
                System.out.println("PictureUrl inside if:- " + rootNode.get("photos").get(0).get("url").asText());
                return rootNode.get("photos").get(0).get("url").asText();
            }
            String pictureUrl =  rootNode.get("photos").get(0).get("url").asText();
            System.out.println("PictureUrl outside if:- " + pictureUrl);
            return pictureUrl;
        } catch (Exception e) {
            System.out.println("Failed to fetch Google profile image: " + e.getMessage());
        }
        return "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y";
    }
}