package com.sagar.instagram_downloader.downloader;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DownloadService {

    private final HttpClient httpClient;

    public DownloadService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String downloadVideo(String videoUrl) throws IOException, InterruptedException {

        Path downloadDirectory = Paths.get("downloads");

        // Create downloads folder if it doesn't exist
        Files.createDirectories(downloadDirectory);

        String fileName = "test-video.mp4";

        Path outputPath = downloadDirectory.resolve(fileName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(videoUrl))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Download failed. HTTP status: " + response.statusCode()
            );
        }

        try (InputStream inputStream = response.body()) {
        	Files.copy(
        	        inputStream,
        	        outputPath,
        	        java.nio.file.StandardCopyOption.REPLACE_EXISTING
        	);
        }

        return outputPath.toAbsolutePath().toString();
    }
}