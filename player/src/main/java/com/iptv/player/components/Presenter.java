package com.iptv.player.components;

import android.util.SparseArray;
import android.view.KeyEvent;

import com.iptv.player.eventTypes.ScreenEvent;

import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public abstract class Presenter<T extends UIView> extends ViewModel {

    private SparseArray<Object> data = new SparseArray<>();
    protected T uiView;
    private LiveData<ScreenEvent> screenStateEvent;

    public void setUiView(T uiView) {
        this.uiView = uiView;
    }

    public void setScreenStateEvent(LiveData<ScreenEvent> screenStateEvent) {
        this.screenStateEvent = screenStateEvent;
        this.screenStateEvent.observeForever(this::onScreenEvent);
    }

    void setData(int key, Object value) {
        data.put(key, value);
    }

    public Object getData(int key) {
        return data.get(key);
    }

    @Override
    protected void onCleared() {
        screenStateEvent.removeObserver(this::onScreenEvent);
        super.onCleared();
    }

    public abstract void onScreenEvent(ScreenEvent screenEvent);
    public abstract boolean onKeyDown(int keyCode, KeyEvent keyEvent, String lockTag);
}