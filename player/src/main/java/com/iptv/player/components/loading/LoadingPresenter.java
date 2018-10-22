package com.iptv.player.components.loading;

import android.view.KeyEvent;

import com.iptv.player.eventTypes.ScreenEvent;
import com.iptv.player.components.Presenter;

public class LoadingPresenter extends Presenter<LoadingView> {

    @Override
    public void onScreenEvent(ScreenEvent screenEvent) {
        switch (screenEvent.getEvent()) {
            case OPENING:
                uiView.show();
                break;
            case BUFFERING:
                if (screenEvent.getBuffering() > 99) {
                    uiView.hide();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent, String lockTag) {
        return false;
    }

}
