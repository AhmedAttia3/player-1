package com.iptv.player.components.loading;

import android.view.ViewGroup;

import com.iptv.player.R;
import com.iptv.player.interfaces.UIView;

import androidx.annotation.NonNull;

public class LoadingView extends UIView {

    public LoadingView(@NonNull ViewGroup parent) {
        super(parent, R.layout.component_loading, true);
    }
}
