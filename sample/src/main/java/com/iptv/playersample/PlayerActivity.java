package com.iptv.playersample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.iptv.player.VlcPlayerActivity;
import com.iptv.player.components.controllers.ControllersPresenter;
import com.iptv.player.components.controllers.ControllersView;
import com.iptv.player.components.loading.LoadingPresenter;
import com.iptv.player.components.loading.LoadingView;
import com.iptv.player.components.signalStrength.SignalStrengthPresenter;
import com.iptv.player.components.signalStrength.SignalStrengthView;
import com.iptv.player.eventTypes.ScreenEvent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

public class PlayerActivity extends VlcPlayerActivity {

    private static final String SAMPLE_URL = "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v";

    public static void start(Context context) {
        Intent starter = new Intent(context, PlayerActivity.class);
        context.startActivity(starter);
    }

    ControllersView controllersView;
    ControllersPresenter controllersPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setAndPlay("http://magictv.live:25461/live/miko/hami111/450.ts");
        setAndPlay(SAMPLE_URL);
    }

    @Override
    public void initComponents(ViewGroup parent, LiveData<ScreenEvent> screenStateEvent) {

        controllersView = new ControllersView(parent);
        controllersPresenter = ViewModelProviders.of(this).get(ControllersPresenter.class);
        controllersPresenter.init(controllersView, screenStateEvent);

        LoadingView loadingView = new LoadingView(parent);
        ViewModelProviders.of(this).get(LoadingPresenter.class)
            .init(loadingView, screenStateEvent);

        SignalStrengthView signalStrengthView = new SignalStrengthView(parent);
        ViewModelProviders.of(this).get(SignalStrengthPresenter.class)
            .init(signalStrengthView, screenStateEvent);
    }

    @Override
    public void initUserInteractionEvents() {
        viewModel.addUserInteractionSource(controllersView.getUserInteractionEvents());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (controllersPresenter.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}