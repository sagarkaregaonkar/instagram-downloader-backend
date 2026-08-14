package com.sagar.instagram_downloader.resolver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sagar.instagram_downloader.dto.ReelMetadata;

@Service
public class InstagramMediaResolver {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${apify.api.token}")
    private String apifyToken;

    public InstagramMediaResolver() {

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public ReelMetadata resolveMetadata(String instagramUrl) throws Exception {

        String apiUrl =
                "https://api.apify.com/v2/acts/apify~instagram-scraper/"
                + "run-sync-get-dataset-items?token="
                + apifyToken;

        String jsonBody = """
                {
                    "directUrls": [
                        "%s"
                    ],
                    "resultsType": "posts",
                    "resultsLimit": 1
                }
                """.formatted(instagramUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() < 200 ||
            response.statusCode() >= 300) {

            throw new RuntimeException(
                    "Apify API error. HTTP "
                    + response.statusCode()
                    + ": "
                    + response.body()
            );
        }

        JsonNode root =
                objectMapper.readTree(response.body());

        if (!root.isArray() || root.isEmpty()) {

            throw new RuntimeException(
                    "Apify returned no Instagram media"
            );
        }

        JsonNode reel = root.get(0);

        String videoUrl =
                getText(reel, "videoUrl");

        String coverImage =
                getText(reel, "displayUrl");

        String caption =
                getText(reel, "caption");

        String username =
                getText(reel, "ownerUsername");

        int likes =
                getInt(reel, "likesCount");

        int comments =
                getInt(reel, "commentsCount");

        if (videoUrl == null || videoUrl.isBlank()) {

            throw new RuntimeException(
                    "No videoUrl found in Apify response"
            );
        }

        return new ReelMetadata(
                videoUrl,
                coverImage,
                caption,
                username,
                likes,
                comments
        );
    }

    private String getText(
            JsonNode node,
            String field) {

        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return "";
        }

        return value.asText();
    }

    private int getInt(
            JsonNode node,
            String field) {

        JsonNode value = node.get(field);

        if (value == null ||
            value.isNull() ||
            !value.isNumber()) {

            return 0;
        }

        return value.asInt();
    }
}