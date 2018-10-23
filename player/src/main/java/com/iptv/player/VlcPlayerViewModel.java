package com.iptv.player;

import com.iptv.player.eventTypes.ScreenEvent;
import com.iptv.player.eventTypes.UserInteraction;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VlcPlayerViewModel extends ViewModel {

    private boolean onKeyLocked = false;
    private String lockTag = null;

    private MutableLiveData<ScreenEvent> screenStateEventLiveData = new MutableLiveData<>();
    private MediatorLiveData<UserInteraction> userInteractionEvents = new MediatorLiveData<>();

    LiveData<ScreenEvent> getScreenStateEvent() {
        return screenStateEventLiveData;
    }
    MediatorLiveData<UserInteraction> getUserInteractionEvents() {
        return userInteractionEvents;
    }

    @MainThread
    public void setScreenStateEvent(ScreenEvent screenEvent) {
        screenStateEventLiveData.setValue(screenEvent);
    }

    public void postScreenStateEvent(ScreenEvent screenEvent) {
        screenStateEventLiveData.postValue(screenEvent);
    }

    void addUserInteractionSource(LiveData<UserInteraction> userInteractionLiveData) {
        userInteractionEvents.addSource(userInteractionLiveData,
            userInteraction -> userInteractionEvents.setValue(userInteraction));
    }

    public boolean isOnKeyLocked() {
        return onKeyLocked;
    }

    public String getLockTag() {
        return lockTag;
    }

    public void requestOnKeyLocked(String lockTag) {
        this.onKeyLocked = true;
        this.lockTag = lockTag;
    }

    public void clearOnKeyLock(String lockTag) {
        if (this.lockTag != null && this.lockTag.equals(lockTag)) {
            this.onKeyLocked = false;
            this.lockTag = null;
        }
    }
}