package com.iptv.player.data.model;

public class VideoItem {
    String url , title, imageUrl;

    public VideoItem(String url, String title, String imageUrl) {
        this.url = url;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
