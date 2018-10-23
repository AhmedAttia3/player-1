package com.iptv.player.components.loading;

import com.iptv.player.R;
import com.iptv.player.components.UIView;

public class LoadingView extends UIView {

    public LoadingView() {
        setLayout(R.layout.component_loading);
    }

    @Override
    public void init() {

    }

    @Override
    public String getLockTag() {
        return null;
    }
}
