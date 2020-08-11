package com.iptv.player.components.signalStrength;

import android.view.KeyEvent;

import com.iptv.player.eventTypes.ScreenEvent;
import com.iptv.player.components.Presenter;

public class SignalStrengthPresenter extends Presenter<SignalStrengthView> {

    @Override
    public void onViewCreated() {

    }

    @Override
    public void onScreenEvent(ScreenEvent screenEvent) {
        switch (screenEvent.getEvent()) {
            case CONNECTION_QUALITY_CHANGED:
                uiView.updateView(screenEvent.getConnectionQuality(), screenEvent.getBitrate());
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent, String lockTag) {
        return false;
    }
}
