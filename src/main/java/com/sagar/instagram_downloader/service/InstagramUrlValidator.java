package com.sagar.instagram_downloader.service;

import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class InstagramUrlValidator {

    public boolean isValidReelUrl(String url) {

        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(url);

            String host = uri.getHost();
            String path = uri.getPath();

            if (host == null || path == null) {
                return false;
            }

            // Check Instagram domain
            boolean validHost =
                    host.equalsIgnoreCase("instagram.com") ||
                    host.equalsIgnoreCase("www.instagram.com");

            if (!validHost) {
                return false;
            }

            // Check Reel path
            return path.startsWith("/reel/") ||
                   path.startsWith("/reels/");

        } catch (Exception e) {
            return false;
        }
    }
}