package com.iptv.player.interfaces;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iptv.player.eventTypes.UserInteraction;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static android.view.View.NO_ID;

public abstract class UIView {

    public View view;
    private boolean showing;
    protected MutableLiveData<UserInteraction> userInteractionEvents = new MutableLiveData<>();

    public UIView(@NonNull ViewGroup parent) {
        this(parent, 0, false);
    }

    public UIView(@NonNull ViewGroup parent, @LayoutRes int layoutResource, boolean isShowing) {
        if (layoutResource != 0) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(layoutResource, parent, false);
            parent.addView(view);
        }

        this.showing = isShowing;
    }

    @Nullable
    protected final <T extends View> T findViewById(@IdRes int id) {
        if (id == NO_ID) {
            return null;
        }
        return view.findViewById(id);
    }

    public LiveData<UserInteraction> getUserInteractionEvents() {
        return userInteractionEvents;
    }

    public void show() {
        view.setVisibility(View.VISIBLE);
        showing = true;
    }

    public void hide() {
        view.setVisibility(View.GONE);
        showing = false;
        view.clearFocus();
    }

    public void toggle() {
        view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        showing = !showing;
    }

    public boolean isShowing() {
        return showing;
    }
}