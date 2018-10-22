package com.iptv.player.components;

import android.view.KeyEvent;

import com.iptv.player.eventTypes.ScreenEvent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public abstract class Presenter<T extends UIView> extends ViewModel {

    protected T uiView;
    private LiveData<ScreenEvent> screenStateEvent;

    public void setUiView(T uiView) {
        this.uiView = uiView;
    }

    public void setScreenStateEvent(LiveData<ScreenEvent> screenStateEvent) {
        this.screenStateEvent = screenStateEvent;
        this.screenStateEvent.observeForever(this::onScreenEvent);
    }

    public abstract void onScreenEvent(ScreenEvent screenEvent);
    public abstract boolean onKeyDown(int keyCode, KeyEvent keyEvent, String lockTag);

    @Override
    protected void onCleared() {
        screenStateEvent.removeObserver(this::onScreenEvent);
        super.onCleared();
    }
}