package com.iptv.player.components.signalStrength;

import android.os.Handler;
import android.view.ViewGroup;

import com.facebook.network.connectionclass.ConnectionQuality;
import com.google.android.material.chip.Chip;
import com.iptv.player.R;
import com.iptv.player.components.UIView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class SignalStrengthView extends UIView {

    private static final String LOCK_TAG = "signalStrengthComponent";

    private final Handler handler = new Handler();
    private final Runnable hideRunnable = this::hide;
    private Chip connectionChip;

    public SignalStrengthView() {
        setLayout(R.layout.component_signal_strength);
    }

    @Override
    public void init() {
        connectionChip = findViewById(R.id.connection_chip);
    }

    @Override
    public void show() {
        super.show();
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, 5000);
    }

    @Override
    public void toggle() {
        super.toggle();
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, 5000);
    }

    @Override
    public String getLockTag() {
        return LOCK_TAG;
    }

    public void updateView(ConnectionQuality quality, String bitrate) {
        connectionChip.setText(bitrate);
        switch (quality) {
            default:
            case UNKNOWN:
                connectionChip.setChipIconResource(R.drawable.ic_signal_defalt);
                connectionChip.setText("loading ...");
                connectionChip.setTextColor(ContextCompat.getColor(connectionChip.getContext(), R.color.network_unknown_color));
                hide();
                break;
            case POOR:
                connectionChip.setChipIconResource(R.drawable.ic_signal_poor);
//                connectionChip.setText(quality.name());
                connectionChip.setTextColor(ContextCompat.getColor(connectionChip.getContext(), R.color.network_poor_color));
                show();
                break;
            case MODERATE:
                connectionChip.setChipIconResource(R.drawable.ic_signal_moderate);
//                connectionChip.setText(quality.name());
                connectionChip.setTextColor(ContextCompat.getColor(connectionChip.getContext(), R.color.network_moderate_color));
                show();
                break;
            case GOOD:
                connectionChip.setChipIconResource(R.drawable.ic_signal_good);
//                connectionChip.setText(quality.name());
                connectionChip.setTextColor(ContextCompat.getColor(connectionChip.getContext(), R.color.network_good_color));
                show();
                break;
            case EXCELLENT:
                connectionChip.setChipIconResource(R.drawable.ic_signal_excellent);
//                connectionChip.setText(quality.name());
                connectionChip.setTextColor(ContextCompat.getColor(connectionChip.getContext(), R.color.network_excellent_color));
                show();
                break;
        }
    }
}