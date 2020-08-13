package com.iptv.player.data.model;

public class VideoItem {
    String url , title, imageUrl;
    int playerTime;

    public VideoItem(String url, String title, String imageUrl, int playerTime) {
        this.url = url;
        this.title = title;
        this.imageUrl = imageUrl;
        this.playerTime = playerTime;
    }

    public void setPlayerTime(int playerTime) {
        this.playerTime = playerTime;
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

    public int getPlayerTime() {
        return playerTime;
    }
}
