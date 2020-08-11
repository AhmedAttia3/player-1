package com.iptv.player.eventTypes;

import com.facebook.network.connectionclass.ConnectionQuality;

public class ScreenEvent {

    private long timeChanged;
    private long lengthChanged;
    private float buffering;
    private String title, imageUrl, bitrate;
    private ConnectionQuality connectionQuality;
    private ScreenStateEvent event;

    public ScreenEvent(ScreenStateEvent event) {
        this.event = event;
    }

    public ScreenEvent(long value, ScreenStateEvent event) {
        this.event = event;
        switch (event) {
            case LENGTH_CHANGED:
                lengthChanged = value;
                break;
            case TIME_CHANGED:
                timeChanged = value;
                break;
        }
    }

    public ScreenEvent(float value) {
        this.event = ScreenStateEvent.BUFFERING;
        buffering = value;
    }

    public ScreenEvent(String value) {
        this.event = ScreenStateEvent.SET_VIDEO_IMAGE;
        imageUrl = value;
    }

    public ScreenEvent(String title, String imageUrl) {
        this.event = ScreenStateEvent.SET_TITLE;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public ScreenEvent(ConnectionQuality connectionQuality, String bitrate) {
        this.event = ScreenStateEvent.CONNECTION_QUALITY_CHANGED;
        this.bitrate = bitrate;
        this.connectionQuality = connectionQuality;
    }

    public ScreenStateEvent getEvent() {
        return event;
    }

    public long getTimeChanged() {
        return timeChanged;
    }

    public long getLengthChanged() {
        return lengthChanged;
    }

    public float getBuffering() {
        return buffering;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public ConnectionQuality getConnectionQuality() {
        return connectionQuality;
    }

    public String getBitrate() {
        return bitrate;
    }
}
