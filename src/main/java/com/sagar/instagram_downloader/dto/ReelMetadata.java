package com.sagar.instagram_downloader.dto;

public class ReelMetadata {

    private String videoUrl;
    private String coverImage;
    private String caption;
    private String username;
    private int likes;
    private int comments;

    public ReelMetadata() {
    }

    public ReelMetadata(
            String videoUrl,
            String coverImage,
            String caption,
            String username,
            int likes,
            int comments) {

        this.videoUrl = videoUrl;
        this.coverImage = coverImage;
        this.caption = caption;
        this.username = username;
        this.likes = likes;
        this.comments = comments;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }
}