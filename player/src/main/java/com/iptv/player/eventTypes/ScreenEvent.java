package com.iptv.player.eventTypes;

import android.view.KeyEvent;

import com.facebook.network.connectionclass.ConnectionQuality;

public class ScreenEvent {

    private long timeChanged;
    private long lengthChanged;
    private float buffering;
    private ConnectionQuality connectionQuality;
    private KeyEvent keyEvent;
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

    public ScreenEvent(ConnectionQuality connectionQuality) {
        this.event = ScreenStateEvent.CONNECTION_QUALITY_CHANGED;
        this.connectionQuality = connectionQuality;
    }

    public ScreenEvent(KeyEvent keyEvent) {
        this.event = ScreenStateEvent.ON_KEY_DOWN;
        this.keyEvent = keyEvent;
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

    public ConnectionQuality getConnectionQuality() {
        return connectionQuality;
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }
}
