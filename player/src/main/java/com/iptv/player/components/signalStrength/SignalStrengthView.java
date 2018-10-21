package com.iptv.player.components.signalStrength;

import android.os.Handler;
import android.view.ViewGroup;

import com.facebook.network.connectionclass.ConnectionQuality;
import com.google.android.material.chip.Chip;
import com.iptv.player.R;
import com.iptv.player.interfaces.UIView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class SignalStrengthView extends UIView {

    private final Handler handler = new Handler();
    private final Runnable hideRunnable = this::hide;
    private Chip connectionChip;

    public SignalStrengthView(@NonNull ViewGroup parent) {
        super(parent, R.layout.component_signal_strength, false);
        connectionChip = findViewById(R.id.connection_chip);
    }

    @Override
    public void show() {
        super.show();
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, 5000);
    }

    public void updateView(ConnectionQuality quality) {
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
                connectionChip.setText(quality.name());
                connectionChip.setTextColor(ContextCompat.getColor(connectionChip.getContext(), R.color.network_poor_color));
                show();
                break;
            case MODERATE:
                connectionChip.setChipIconResource(R.drawable.ic_signal_moderate);
                connectionChip.setText(quality.name());
                connectionChip.setTextColor(ContextCompat.getColor(connectionChip.getContext(), R.color.network_moderate_color));
                show();
                break;
            case GOOD:
                connectionChip.setChipIconResource(R.drawable.ic_signal_good);
                connectionChip.setText(quality.name());
                connectionChip.setTextColor(ContextCompat.getColor(connectionChip.getContext(), R.color.network_good_color));
                show();
                break;
            case EXCELLENT:
                connectionChip.setChipIconResource(R.drawable.ic_signal_excellent);
                connectionChip.setText(quality.name());
                connectionChip.setTextColor(ContextCompat.getColor(connectionChip.getContext(), R.color.network_excellent_color));
                show();
                break;
        }
    }
}