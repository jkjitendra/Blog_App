package com.jk.blog.oauth;

import com.jk.blog.oauth.impl.GitHubProfileImageFetcher;
import com.jk.blog.oauth.impl.GoogleProfileImageFetcher;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProfileImageFetcherFactory {

    private final Map<String, OAuthProfileImageFetcher> fetchers;

    public ProfileImageFetcherFactory(GitHubProfileImageFetcher githubFetcher, GoogleProfileImageFetcher googleFetcher) {
        this.fetchers = Map.of(
                "github", githubFetcher,
                "google", googleFetcher
        );
    }

    public String fetchProfileImage(String provider, String accessToken) {
        OAuthProfileImageFetcher fetcher = fetchers.get(provider.toLowerCase());
        if (fetcher != null) {
            return fetcher.fetchProfileImage(accessToken);
        }
        return null; // Return null if the provider is not supported
    }
}