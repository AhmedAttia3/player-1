package com.iptv.player.components.loading;

import com.iptv.player.R;
import com.iptv.player.components.UIView;

public class LoadingView extends UIView {

    private static final String LOCK_TAG = "loadingComponent";

    public LoadingView() {
        super(R.layout.component_loading, false);
    }

    @Override
    public String getLockTag() {
        return LOCK_TAG;
    }
}
