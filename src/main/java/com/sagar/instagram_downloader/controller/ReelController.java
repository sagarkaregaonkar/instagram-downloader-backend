package com.sagar.instagram_downloader.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.sagar.instagram_downloader.dto.VideoDownloadRequest;

import com.sagar.instagram_downloader.dto.ReelMetadata;

import java.nio.file.Files;
import java.nio.file.Path;


import com.sagar.instagram_downloader.resolver.InstagramMediaResolver;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


import com.sagar.instagram_downloader.dto.ReelRequest;
import com.sagar.instagram_downloader.service.InstagramUrlValidator;
import com.sagar.instagram_downloader.downloader.DownloadService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController
@RequestMapping("/api/reels")
@CrossOrigin(origins = "http://localhost:5173")
public class ReelController {
	

	private final HttpClient httpClient;
    private final InstagramUrlValidator validator;
    private final DownloadService downloadService;
    private final InstagramMediaResolver mediaResolver;

    public ReelController(
            InstagramUrlValidator validator,
            DownloadService downloadService,
            InstagramMediaResolver mediaResolver) {

        this.validator = validator;
        this.downloadService = downloadService;
        this.mediaResolver = mediaResolver;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(20))
                .build();
    }

    @GetMapping("/test")
    public String test() {
        return "Instagram Downloader API is running";
    }

    @PostMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(
            @RequestBody VideoDownloadRequest request) {

        try {

            String videoUrl = request.getVideoUrl();

            if (videoUrl == null || videoUrl.isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(videoUrl))
                    .timeout(java.time.Duration.ofMinutes(5))
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofInputStream()
                    );

            if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

                response.body().close();

                return ResponseEntity
                        .status(response.statusCode())
                        .build();
            }

            InputStream inputStream = response.body();

            StreamingResponseBody stream = outputStream -> {

                try (InputStream input = inputStream) {

                    byte[] buffer = new byte[8192];

                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {

                        outputStream.write(buffer, 0, bytesRead);

                        outputStream.flush();
                    }

                }
            };

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(
                    MediaType.parseMediaType("video/mp4")
            );

            headers.setContentDispositionFormData(
                    "attachment",
                    "instagram-reel.mp4"
            );

            String contentLength =
                    response.headers()
                            .firstValue("Content-Length")
                            .orElse(null);

            if (contentLength != null) {

                try {

                    headers.setContentLength(
                            Long.parseLong(contentLength)
                    );

                } catch (NumberFormatException ignored) {
                    // Ignore invalid content length
                }
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stream);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
    
    @GetMapping("/test-download")
    public ResponseEntity<byte[]> testDownload() {

        try {

            String testVideoUrl =
                    "https://cdn.truefilesize.com/mp4/sample-1mb.mp4";

            String filePath =
                    downloadService.downloadVideo(testVideoUrl);

            Path path = Path.of(filePath);

            byte[] video = Files.readAllBytes(path);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"test-video.mp4\""
                    )
                    .body(video);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/preview")
    public ResponseEntity<ReelMetadata> preview(
            @RequestBody ReelRequest request) {

        try {

            ReelMetadata metadata =
                    mediaResolver.resolveMetadata(
                            request.getUrl()
                    );

            return ResponseEntity.ok(metadata);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
    
    @GetMapping("/cover")
    public ResponseEntity<byte[]> getCoverImage(
            @RequestParam String url) {

        try {

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/151.0.0.0 Safari/537.36"
                    )
                    .header(
                            "Accept",
                            "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"
                    )
                    .GET()
                    .build();

            HttpResponse<byte[]> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofByteArray()
                    );

            if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

                return ResponseEntity
                        .status(response.statusCode())
                        .build();
            }

            String contentType =
                    response.headers()
                            .firstValue("Content-Type")
                            .orElse("image/jpeg");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(response.body());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}