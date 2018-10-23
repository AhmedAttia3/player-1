package com.iptv.player;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.iptv.player.components.Component;
import com.iptv.player.eventTypes.ScreenEvent;
import com.iptv.player.eventTypes.ScreenStateEvent;
import com.iptv.player.eventTypes.UserInteraction;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

public abstract class VlcPlayerActivity extends AppCompatActivity implements
    IVLCVout.OnNewVideoLayoutListener,
    MediaPlayer.EventListener,
    ConnectionClassManager.ConnectionClassStateChangeListener {

    private static final String TAG = "VlcPlayerActivity";
    private static final boolean USE_SURFACE_VIEW = true;
    private static final boolean ENABLE_SUBTITLES = true;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private static int CURRENT_SIZE = SURFACE_FIT_SCREEN;

    private FrameLayout mVideoSurfaceFrame = null;
    private SurfaceView mVideoSurface = null;
    private SurfaceView mSubtitlesSurface = null;
    private TextureView mVideoTexture = null;
    private View mVideoView = null;

    private final Handler mHandler = new Handler();
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;

    protected VlcPlayerViewModel viewModel;
    private DeviceBandwidthSampler mDeviceBandwidthSampler;
    private ConnectionClassManager mConnectionClassManager;

    private ConstraintLayout componentContainer;
    List<Component> components;
    private boolean isOnKeyDownConsumed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_player);
        viewModel = ViewModelProviders.of(this).get(VlcPlayerViewModel.class);

        componentContainer = findViewById(R.id.view_container);
        componentContainer.setOnClickListener(
            v -> viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.ON_SCREEN_TOUCH)));

        viewModel.getUserInteractionEvents().observe(this, this::onUserInteraction);

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        mVideoSurfaceFrame = findViewById(R.id.video_surface_frame);
        if (USE_SURFACE_VIEW) {
            ViewStub stub = findViewById(R.id.surface_stub);
            mVideoSurface = (SurfaceView) stub.inflate();
            if (ENABLE_SUBTITLES) {
                stub = findViewById(R.id.subtitles_surface_stub);
                mSubtitlesSurface = (SurfaceView) stub.inflate();
                mSubtitlesSurface.setZOrderMediaOverlay(true);
                mSubtitlesSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            }
            mVideoView = mVideoSurface;
        }
        else
        {
            ViewStub stub = findViewById(R.id.texture_stub);
            mVideoTexture = (TextureView) stub.inflate();
            mVideoView = mVideoTexture;
        }

        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();
        mConnectionClassManager = ConnectionClassManager.getInstance();
        mDeviceBandwidthSampler.startSampling();
    }

    @Override
    protected void onDestroy() {
        mMediaPlayer.release();
        mLibVLC.release();
        mDeviceBandwidthSampler.stopSampling();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final IVLCVout vlcVout = mMediaPlayer.getVLCVout();
        if (mVideoSurface != null) {
            vlcVout.setVideoView(mVideoSurface);
            if (mSubtitlesSurface != null)
                vlcVout.setSubtitlesView(mSubtitlesSurface);
        }
        else
            vlcVout.setVideoView(mVideoTexture);
        vlcVout.attachViews(this);

        components = getComponents();
        if (components != null) {
            for (Component component : components) {
                component.getView().setParent(componentContainer);
                component.getPresenter().setUiView(component.getView());
                component.getPresenter().setScreenStateEvent(viewModel.getScreenStateEvent());
                viewModel.addUserInteractionSource(component.getView().getUserInteractionEvents());
            }
        }

        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable mRunnable = () -> updateVideoSurfaces();

                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.post(mRunnable);
                    }
                }
            };
        }
        mVideoSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);

        mConnectionClassManager.register(this);
    }

    @Override
    protected void onStop() {
        if (mOnLayoutChangeListener != null) {
            mVideoSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            mOnLayoutChangeListener = null;
        }

        mMediaPlayer.stop();

        mMediaPlayer.getVLCVout().detachViews();

        mConnectionClassManager.remove(this);

        super.onStop();
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mMediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                    || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (CURRENT_SIZE == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    mMediaPlayer.setScale(scale);
                    mMediaPlayer.setAspectRatio(null);
                } else {
                    mMediaPlayer.setScale(0);
                    mMediaPlayer.setAspectRatio(!videoSwapped ? ""+displayW+":"+displayH
                        : ""+displayH+":"+displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mMediaPlayer.setAspectRatio("16:9");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_4_3:
                mMediaPlayer.setAspectRatio("4:3");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(1);
                break;
        }
    }

    private void updateVideoSurfaces() {
        int sw = getWindow().getDecorView().getWidth();
        int sh = getWindow().getDecorView().getHeight();

        // sanity check
        if (sw * sh == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        mMediaPlayer.getVLCVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width  = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoView.setLayoutParams(lp);
            lp = mVideoSurfaceFrame.getLayoutParams();
            lp.width  = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoSurfaceFrame.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double)mVideoVisibleWidth / (double)mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double)mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width  = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        mVideoView.setLayoutParams(lp);
        if (mSubtitlesSurface != null)
            mSubtitlesSurface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mVideoSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mVideoSurfaceFrame.setLayoutParams(lp);

        mVideoView.invalidate();
        if (mSubtitlesSurface != null)
            mSubtitlesSurface.invalidate();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public abstract List<Component> getComponents();

    public void setMedia(String url) {
        final Media media = new Media(mLibVLC, Uri.parse(url));
        mMediaPlayer.setMedia(media);
        media.release();
    }

    public void setAndPlay(String url) {
        final Media media = new Media(mLibVLC, Uri.parse(url));
        mMediaPlayer.setMedia(media);
        media.release();

        play();
    }

    public void play() {
        mMediaPlayer.play();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    public void minus(int time) {
        mMediaPlayer.setTime(mMediaPlayer.getTime() - time);
    }

    public void plus(int time) {
        mMediaPlayer.setTime(mMediaPlayer.getTime() + time);
    }

    public void seekTo(int time) {
        mMediaPlayer.setTime(time);
    }

    public long getTime() {
        return mMediaPlayer.getTime();
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.MediaChanged:
                viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.MEDIA_CHANGED));
                break;
            case MediaPlayer.Event.Opening:
                viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.OPENING));
                break;
            case MediaPlayer.Event.Buffering:
                viewModel.setScreenStateEvent(new ScreenEvent(event.getBuffering()));
                break;
            case MediaPlayer.Event.Playing:
                viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.PLAYING));
                break;
            case MediaPlayer.Event.Paused:
                viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.PAUSES));
                break;
            case MediaPlayer.Event.Stopped:
                viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.STOPPED));
                break;
            case MediaPlayer.Event.LengthChanged:
                viewModel.setScreenStateEvent(new ScreenEvent(event.getLengthChanged(), ScreenStateEvent.LENGTH_CHANGED));
                break;
            case MediaPlayer.Event.TimeChanged:
                viewModel.setScreenStateEvent(new ScreenEvent(event.getTimeChanged(), ScreenStateEvent.TIME_CHANGED));
                break;
        }
    }

    private void onUserInteraction(UserInteraction userInteraction) {
        switch (userInteraction.getEvent()) {
            case PLAY_MEDIA:
                setAndPlay(userInteraction.getMediaUri());
                break;
            case PLAY:
                play();
                break;
            case PAUSE:
                pause();
                break;
            case STOP:
                stop();
                break;
            case NEXT:
                break;
            case PREVIOUS:
                break;
            case TIME_CHANGED:
                seekTo(userInteraction.getTimeChanged());
                break;
            case TIME_MINUS:
                minus(userInteraction.getSeekValue());
                viewModel.setScreenStateEvent(new ScreenEvent(getTime(), ScreenStateEvent.TIME_CHANGED));
                break;
            case TIME_PLUS:
                plus(userInteraction.getSeekValue());
                viewModel.setScreenStateEvent(new ScreenEvent(getTime(), ScreenStateEvent.TIME_CHANGED));
                break;
            case ON_KEY_LOCK:
                viewModel.requestOnKeyLocked(userInteraction.getLockTag());
                break;
            case CLEAR_ON_KEY_LOCK:
                viewModel.clearOnKeyLock(userInteraction.getLockTag());
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        for (Component component : components) {
            if (component.getPresenter().onKeyDown(keyCode, event, viewModel.getLockTag())) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
        runOnUiThread(() -> viewModel.postScreenStateEvent(new ScreenEvent(bandwidthState)));
    }
}