package com.iptv.player.components.controllers;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.material.button.MaterialButton;
import com.iptv.player.R;
import com.iptv.player.cast.CustomVolleyRequest;
import com.iptv.player.eventTypes.UserInteraction;
import com.iptv.player.eventTypes.UserInteractionEvent;
import com.iptv.player.components.UIView;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.mediarouter.app.MediaRouteButton;

public class ControllersView extends UIView implements View.OnClickListener {

    private static final String LOCK_TAG = "controllersComponent";

    private ImageButton minus10;
    private ImageButton plus10;
    private ImageButton next;
    private ImageButton previous;
    private ImageButton playPause;
    private boolean isPlaying;

    private TextView currentTime;
    private TextView mediaTime;
    private SeekBar seekBar;
    MediaRouteButton mediaRouteButton;
    private final Handler handler = new Handler();
    private final Runnable hideRunnable = this::hide;
    NetworkImageView videoImage;
    ImageLoader mImageLoader;
    TextView title_text;
    public ControllersView() {
        setLayout(R.layout.component_controllers);
    }

    @Override
    public void setParent(@NonNull ViewGroup parent) {
        super.setParent(parent);
        mImageLoader = CustomVolleyRequest.getInstance(parent.getContext())
                .getImageLoader();
        CastButtonFactory.setUpMediaRouteButton(parent.getContext(), mediaRouteButton);
    }

    @Override
    public void init() {
        minus10 = findViewById(R.id.minus_10);
        Objects.requireNonNull(minus10).setOnClickListener(this);
        plus10 = findViewById(R.id.plus_10);
        Objects.requireNonNull(plus10).setOnClickListener(this);
        playPause = findViewById(R.id.play_pause);
        Objects.requireNonNull(playPause).setOnClickListener(this);
        next = findViewById(R.id.next);
        Objects.requireNonNull(next).setOnClickListener(this);
        previous = findViewById(R.id.previous);
        Objects.requireNonNull(previous).setOnClickListener(this);

        next.setVisibility(View.GONE);
        previous.setVisibility(View.GONE);

        mediaRouteButton = findViewById(R.id.mediaRouteButton);
        videoImage = findViewById(R.id.videoImage);
        title_text = findViewById(R.id.title_text);

        currentTime = findViewById(R.id.current_time);
        mediaTime = findViewById(R.id.media_time);
        seekBar = findViewById(R.id.seek_bar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    startAutoHide();
                    userInteractionEvents.setValue(new UserInteraction(progress, UserInteractionEvent.TIME_CHANGED));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setDisabled(ImageButton next, boolean b) {
        next.setImageAlpha(b ? 0xFF : 0x3F);

    }

    @Override
    public void show() {
        requestOnKeyLock(LOCK_TAG);
        if (!isShowing()) {
            playPause.requestFocus();
        }
        super.show();
        startAutoHide();
    }

    @Override
    public void toggle() {
        requestOnKeyLock(LOCK_TAG);
        if (!isShowing()) {
            playPause.requestFocus();
        }
//        if(isPlaying){

            super.toggle();
            startAutoHide();
//        }
    }

    @Override
    public void hide() {
        super.hide();
        stopAutoHide();
        clearOnKeyLock(LOCK_TAG);
    }

    @Override
    public String getLockTag() {
        return LOCK_TAG;
    }

    public void startAutoHide() {
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, 5000);
    }

    public void stopAutoHide() {
        handler.removeCallbacks(hideRunnable);
    }

    @Override
    public void onClick(View v) {
        startAutoHide();
        if (v.getId() == R.id.minus_10) {
            userInteractionEvents.setValue(new UserInteraction(10000, UserInteractionEvent.TIME_MINUS));
        } else if (v.getId() == R.id.plus_10) {
            userInteractionEvents.setValue(new UserInteraction(10000, UserInteractionEvent.TIME_PLUS));
        } else if (v.getId() == R.id.play_pause) {
            if (isPlaying) {
                userInteractionEvents.setValue(new UserInteraction(UserInteractionEvent.PAUSE));
            } else {
                userInteractionEvents.setValue(new UserInteraction(UserInteractionEvent.PLAY));
            }
        }
        else if( v.getId()==R.id.next){
            userInteractionEvents.setValue(new UserInteraction(UserInteractionEvent.NEXT));
        }
        else if( v.getId()==R.id.previous){
            userInteractionEvents.setValue(new UserInteraction(UserInteractionEvent.PREVIOUS));

        }
    }

    void playList() {
        next.setVisibility(View.VISIBLE);
        previous.setVisibility(View.VISIBLE);
        setDisabled(next, true);
        setDisabled(previous, true);
        next.setEnabled(true);
        previous.setEnabled(true);
    }


    void startList() {
        setDisabled(next, true);
        next.setEnabled(true);

        setDisabled(previous, false);
        previous.setEnabled(false);

        playPause.requestFocus();
    }


    void endList() {
        setDisabled(next, false);
        next.setEnabled(false);

        setDisabled(previous, true);
        previous.setEnabled(true);
        playPause.requestFocus();
    }


    void play() {
        isPlaying = true;
        playPause.setImageResource(R.drawable.ic_pause_white);
    }

    void pause() {
        isPlaying = false;
        playPause.setImageResource(R.drawable.ic_play_arrow_white);
    }

    void stopped() {
        isPlaying = false;
        playPause.setImageResource(R.drawable.ic_play_arrow_white);
    }

    public void end() {
        isPlaying = false;
        playPause.setImageResource(R.drawable.ic_play_arrow_white);
    }

    public void showCastBtn(){
        mediaRouteButton.setVisibility(View.VISIBLE);
    }

    public void hideCastBtn(){
        mediaRouteButton.setVisibility(View.GONE);
    }


    public void showVideoImage(){
        videoImage.setVisibility(View.VISIBLE);
    }

    public void hideVideoImage(){
        videoImage.setVisibility(View.GONE);
    }

    public void setVideoImage(String url){
        mImageLoader.get(url, ImageLoader.getImageListener(videoImage, 0, 0));
        videoImage.setImageUrl(url, mImageLoader);
        showVideoImage();
    }

    public void setTitle_text(String value){
        title_text.setText(value);
    }

    void updateTime(long value) {
        seekBar.setProgress((int) value);
        String hms = String.format(Locale.getDefault(),
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(value),
            TimeUnit.MILLISECONDS.toMinutes(value) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(value) % TimeUnit.MINUTES.toSeconds(1));
        currentTime.setText(hms);
    }

    void updateLength(long lengthChanged) {
        seekBar.setMax((int) lengthChanged);
        String hms = String.format(Locale.getDefault(),
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(lengthChanged),
            TimeUnit.MILLISECONDS.toMinutes(lengthChanged) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(lengthChanged) % TimeUnit.MINUTES.toSeconds(1));
        mediaTime.setText(hms);
    }
}
