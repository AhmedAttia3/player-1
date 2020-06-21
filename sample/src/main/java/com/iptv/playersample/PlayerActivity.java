package com.iptv.playersample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.iptv.player.SurfaceSize;
import com.iptv.player.VlcPlayerActivity;
import com.iptv.player.components.Component;
import com.iptv.player.components.controllers.ControllersPresenter;
import com.iptv.player.components.controllers.ControllersView;
import com.iptv.player.components.loading.LoadingPresenter;
import com.iptv.player.components.loading.LoadingView;
import com.iptv.player.components.signalStrength.SignalStrengthPresenter;
import com.iptv.player.components.signalStrength.SignalStrengthView;
import com.iptv.player.eventTypes.ScreenEvent;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

public class PlayerActivity extends VlcPlayerActivity {

    private static final String SAMPLE_URL = "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v";
    private static final String HLS_URL = "http://78.46.64.2/media/movies1/15316.mkv";

    public static void start(Context context) {
        Intent starter = new Intent(context, PlayerActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenSize(SurfaceSize.SURFACE_FILL);
        setAndPlay(HLS_URL);
    }

    @Override
    public List<Component> getComponents() {

        List<Component> components = new ArrayList<>();

        Component<ControllersView> controllersViewComponent = new Component<>(
            new ControllersView(),
            ViewModelProviders.of(this).get(ControllersPresenter.class)
        );

        Component<LoadingView> loadingViewComponent = new Component<>(
            new LoadingView(),
            ViewModelProviders.of(this).get(LoadingPresenter.class)
        );

        Component<SignalStrengthView> signalStrengthViewComponent = new Component<>(
            new SignalStrengthView(),
            ViewModelProviders.of(this).get(SignalStrengthPresenter.class)
        );

        components.add(controllersViewComponent);
        components.add(loadingViewComponent);
        components.add(signalStrengthViewComponent);

        return components;
    }
}