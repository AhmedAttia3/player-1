package com.iptv.player.interfaces;

import android.view.KeyEvent;

import com.iptv.player.eventTypes.ScreenEvent;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public abstract class Presenter<T extends UIView> extends ViewModel {

    protected T uiView;
    private LiveData<ScreenEvent> screenStateEvent;

    public void init(@NonNull T uiView, @NonNull LiveData<ScreenEvent> screenStateEvent) {
        this.uiView = uiView;
        if (this.screenStateEvent == null) {
            this.screenStateEvent = screenStateEvent;
        }

        screenStateEvent.observeForever(this::onScreenEvent);
    }

    public abstract void onScreenEvent(ScreenEvent screenEvent);
    public abstract boolean onKeyDown(int keyCode, KeyEvent event);

    @Override
    protected void onCleared() {
        screenStateEvent.removeObserver(this::onScreenEvent);
        super.onCleared();
    }
}