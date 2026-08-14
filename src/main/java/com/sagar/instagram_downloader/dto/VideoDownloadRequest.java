package com.sagar.instagram_downloader.dto;

public class VideoDownloadRequest {

    private String videoUrl;

    public VideoDownloadRequest() {
    }

    public VideoDownloadRequest(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}