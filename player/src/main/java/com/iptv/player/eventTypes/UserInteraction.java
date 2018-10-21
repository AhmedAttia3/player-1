package com.iptv.player.eventTypes;

public class UserInteraction {

    private int timeChanged;
    private int seekValue;
    private UserInteractionEvent event;

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

    public int getTimeChanged() {
        return timeChanged;
    }

    public int getSeekValue() {
        return seekValue;
    }

    public UserInteractionEvent getEvent() {
        return event;
    }
}
