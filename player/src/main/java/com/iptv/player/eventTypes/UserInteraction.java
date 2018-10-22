package com.iptv.player.eventTypes;

public class UserInteraction {

    private UserInteractionEvent event;
    private int timeChanged;
    private int seekValue;
    private String mediaUri;
    private String lockTag;

    public UserInteraction(UserInteractionEvent event) {
        this.event = event;
    }

    public UserInteraction(int value, UserInteractionEvent event) {
        this.event = event;
        switch (event) {
            case TIME_CHANGED:
                timeChanged = value;
                break;
            case TIME_PLUS:
            case TIME_MINUS:
                seekValue = value;
                break;
        }
    }

    public UserInteraction(String value, UserInteractionEvent event) {
        this.event = event;
        switch (event) {
            case PLAY_MEDIA:
                this.mediaUri = value;
                break;
            case ON_KEY_LOCK:
            case CLEAR_ON_KEY_LOCK:
                this.lockTag = value;
                break;
        }
    }

    public UserInteractionEvent getEvent() {
        return event;
    }

    public int getTimeChanged() {
        return timeChanged;
    }

    public int getSeekValue() {
        return seekValue;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public String getLockTag() {
        return lockTag;
    }
}
