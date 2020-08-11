package com.iptv.player.components.controllers;

import android.view.KeyEvent;

import com.iptv.player.eventTypes.ScreenEvent;
import com.iptv.player.components.Presenter;

public class ControllersPresenter extends Presenter<ControllersView> {

    private boolean isPlaying = false;

    @Override
    public void onViewCreated() {

    }

    @Override
    public void onScreenEvent(ScreenEvent screenEvent) {
        switch (screenEvent.getEvent()) {
            case BUFFERING:
//                controllersView.pause();
                break;
            case PLAYLIST:
                uiView.playList();
                break;
            case PLAYING:
                isPlaying = true;
                uiView.play();
                break;
            case PAUSES:
                uiView.pause();
                break;
            case STOPPED:
                uiView.stopped();
                break;
            case END_REACHED:
                uiView.end();
                break;
            case ON_SCREEN_TOUCH:
//                if(isPlaying)
                uiView.toggle();
                break;
            case LENGTH_CHANGED:
                uiView.updateLength(screenEvent.getLengthChanged());
                break;
            case TIME_CHANGED:
                uiView.updateTime(screenEvent.getTimeChanged());
                break;
            case START_LIST:
                uiView.startList();
                break;
            case END_LIST:
                uiView.endList();
                break;
            case SHOW_CAST_BTN:
                uiView.showCastBtn();
                break;
            case HIDE_CAST_BTN:
                uiView.hideCastBtn();
            case SET_VIDEO_IMAGE:
                uiView.setVideoImage(screenEvent.getImageUrl());
            case SET_TITLE:
                uiView.setTitle_text(screenEvent.getTitle());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent, String lockTag) {
        if (lockTag == null || lockTag.equals(uiView.getLockTag())) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (isPlaying) {
                        if (!uiView.isShowing()) {
                            uiView.show();
                            return true;
                        } else {
                            uiView.startAutoHide();
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (uiView.isShowing()) {
//                        if(isPlaying)
                        uiView.hide();
                        return true;
                    }
                    break;
            }
        }
        return false;
    }
}