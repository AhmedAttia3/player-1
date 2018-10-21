package com.iptv.player;

import com.iptv.player.eventTypes.ScreenEvent;
import com.iptv.player.eventTypes.UserInteraction;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VlcPlayerViewModel extends ViewModel {

    private MutableLiveData<ScreenEvent> screenStateEventLiveData = new MutableLiveData<>();
    private MediatorLiveData<UserInteraction> userInteractionEvents = new MediatorLiveData<>();

    LiveData<ScreenEvent> getScreenStateEvent() {
        return screenStateEventLiveData;
    }

    @MainThread
    void setScreenStateEvent(ScreenEvent screenEvent) {
        screenStateEventLiveData.setValue(screenEvent);
    }

    void postScreenStateEvent(ScreenEvent screenEvent) {
        screenStateEventLiveData.postValue(screenEvent);
    }

    public void addUserInteractionSource(LiveData<UserInteraction> userInteractionLiveData) {
        userInteractionEvents.addSource(userInteractionLiveData,
            userInteraction -> userInteractionEvents.setValue(userInteraction));
    }

    MediatorLiveData<UserInteraction> getUserInteractionEvents() {
        return userInteractionEvents;
    }
}