package com.iptv.player.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iptv.player.eventTypes.UserInteraction;
import com.iptv.player.eventTypes.UserInteractionEvent;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static android.view.View.NO_ID;

public abstract class UIView {

    private @LayoutRes int layoutResource;
    @Nullable
    public View view;
    private boolean showing;
    protected MutableLiveData<UserInteraction> userInteractionEvents = new MutableLiveData<>();

    public void setLayout(@LayoutRes int layoutResource) {
        this.layoutResource = layoutResource;
    }

    public void setParent(@NonNull ViewGroup parent) {
        if (layoutResource != 0) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(layoutResource, parent, false);
            parent.addView(view);

            if (view != null) {
                showing = view.getVisibility() == View.VISIBLE;
            }
        }

        init();
    }

    public abstract void init();

    @Nullable
    protected final <T extends View> T findViewById(@IdRes int id) {
        if (id == NO_ID) {
            return null;
        }

        if (view != null) {
            return view.findViewById(id);
        }

        return null;
    }

    public LiveData<UserInteraction> getUserInteractionEvents() {
        return userInteractionEvents;
    }

    protected void requestOnKeyLock(String lockTag) {
        userInteractionEvents.setValue(new UserInteraction(lockTag, UserInteractionEvent.ON_KEY_LOCK));
    }

    protected void clearOnKeyLock(String lockTag) {
        userInteractionEvents.setValue(new UserInteraction(lockTag, UserInteractionEvent.CLEAR_ON_KEY_LOCK));
    }

    public void show() {
        if (!showing) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
            showing = true;
        }
    }

    public void hide() {
        if (showing) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            showing = false;
            if (view != null) {
                view.clearFocus();
            }
        }
    }

    public void toggle() {
        if (view != null) {
            view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
        showing = !showing;
    }

    public boolean isShowing() {
        return showing;
    }

    public abstract String getLockTag();
}