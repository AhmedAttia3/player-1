package com.iptv.player;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.iptv.player.cast.CustomVolleyRequest;
import com.iptv.player.cast.ExpandedControlsActivity;
import com.iptv.player.components.Component;
import com.iptv.player.data.model.VideoItem;
import com.iptv.player.eventTypes.ScreenEvent;
import com.iptv.player.eventTypes.ScreenStateEvent;
import com.iptv.player.eventTypes.UserInteraction;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

public abstract class VlcPlayerActivity extends AppCompatActivity implements
        IVLCVout.OnNewVideoLayoutListener,
        MediaPlayer.EventListener,
        ConnectionClassManager.ConnectionClassStateChangeListener {

    private static final String TAG = "VlcPlayerActivity";
    private static final boolean USE_SURFACE_VIEW = true;
    private static final boolean ENABLE_SUBTITLES = true;

    private static SurfaceSize CURRENT_SIZE = SurfaceSize.SURFACE_FIT_SCREEN;

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
    NetworkImageView videoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_player);

        startSearchForDevicesAndCast();
        setupCastListener();
        mCastContext = CastContext.getSharedInstance(this);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();


//        try {
//            CastManager.initCastManager(this, NAMESPACE);
//        } catch (CastManager.CastManagerInitializationException e) {
//            e.printStackTrace();
//        }
//        RemoteDeviceConnector.initRemoteDeviceConnector(this, getResources().getString(R.string.app_id));

        videoImage = findViewById(R.id.videoImage);


        viewModel = ViewModelProviders.of(this).get(VlcPlayerViewModel.class);

        componentContainer = findViewById(R.id.view_container);
        componentContainer.setOnClickListener(
                v -> viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.ON_SCREEN_TOUCH)));

        viewModel.getUserInteractionEvents().observe(this, this::onUserInteraction);

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        args.add("--vout=android-display");
        args.add("--aout=opensles");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.setAudioOutput("opensles");

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
        } else {
            ViewStub stub = findViewById(R.id.texture_stub);
            mVideoTexture = (TextureView) stub.inflate();
            mVideoView = mVideoTexture;
        }

        mMediaPlayer.setEventListener(this);

        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();
        mConnectionClassManager = ConnectionClassManager.getInstance();
        mDeviceBandwidthSampler.startSampling();

        components = getComponents();
        if (components != null) {
            for (Component component : components) {
                component.getView().setParent(componentContainer);
                component.getPresenter().setUiView(component.getView());
                component.getPresenter().setScreenStateEvent(viewModel.getScreenStateEvent());
                viewModel.addUserInteractionSource(component.getView().getUserInteractionEvents());
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("onDestroy", String.valueOf(mMediaPlayer.getTime()));
        long i = 0;
        if (mMediaPlayer.getLength() != mMediaPlayer.getTime())
            i = mMediaPlayer.getTime();
        pausedIn((int) i, selected);
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
        } else {
            vlcVout.setVideoView(mVideoTexture);
        }
        vlcVout.attachViews(this);

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

        super.onStop();
    }
    Boolean isPlaying = false;
    @Override
    protected void onResume() {
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        if (mCastSession != null && mCastSession.isConnected()) {
            updatePlaybackLocation(PlaybackLocation.REMOTE);
        } else {
            updatePlaybackLocation(PlaybackLocation.LOCAL);
        }
        super.onResume();

        if (mMediaPlayer != null) {
//            if(!mMediaPlayer.isPlaying())
            mVideoSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);

            mConnectionClassManager.register(this);
            Log.e("onResumeIsPlaying", String.valueOf(isPlaying));
            if(isPlaying)
            mMediaPlayer.play();

        }
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
                if (CURRENT_SIZE == SurfaceSize.SURFACE_FIT_SCREEN) {
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
                    mMediaPlayer.setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
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
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoView.setLayoutParams(lp);
            lp = mVideoSurfaceFrame.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
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
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
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
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
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

    public abstract void pausedIn(int time, int episodeIndex);

    public static void setScreenSize(SurfaceSize size) {
        CURRENT_SIZE = size;
    }

    public void setMedia(String url) {
        final Media media = new Media(mLibVLC, Uri.parse(url));
        mMediaPlayer.setMedia(media);
        media.release();
    }


    VideoItem videoItem;
    public void setAndPlay(VideoItem videoItem) {
        viewModel.setScreenStateEvent(new ScreenEvent(videoItem.getTitle(), videoItem.getImageUrl()));
        this.videoItem = videoItem;
        loadVideoImage();
        final Media media = new Media(mLibVLC, Uri.parse(videoItem.getUrl()));
        mMediaPlayer.setMedia(media);
        media.release();
        if (mCastSession != null && mCastSession.isConnected()) {
            loadRemoteMedia((int)resumeTime, true);
        }else {
            Log.e("PlayingA", "test");
            play();
        }
    }

    private static ArrayList<VideoItem> videoItems =new ArrayList<>();
    int selected = 0;
    public void setAndPlay(ArrayList<VideoItem> episods, int selected) {
        viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.PLAYLIST));
        videoItems.clear();
        videoItems.addAll(episods);
        videoItem = videoItems.get(selected);
        viewModel.setScreenStateEvent(new ScreenEvent(videoItem.getTitle(), videoItem.getImageUrl()));
        this.selected = selected;

        final Media media = new Media(mLibVLC, Uri.parse(videoItems.get(selected).getUrl()));
        mMediaPlayer.setMedia(media);
        startOrEndList();
        media.release();
        if (mCastSession != null && mCastSession.isConnected()) {
            loadRemoteMedia((int)resumeTime, true);
        }else {

            play();
        }
    }

    private void startOrEndList() {
        if(selected==videoItems.size()-1){
            viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.END_LIST));
        }else if(selected == 0){
            viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.START_LIST));
        }else {
            viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.PLAYLIST));
        }
    }

    private void loadVideoImage() {
        ImageLoader mImageLoader = CustomVolleyRequest.getInstance(getApplicationContext())
                .getImageLoader();
        mImageLoader.get(videoItem.getImageUrl(), ImageLoader.getImageListener(videoImage, 0, 0));
        videoImage.setImageUrl(videoItem.getImageUrl(), mImageLoader);
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
                mPlaybackState = PlaybackState.PLAYING;
                videoImage.setVisibility(View.GONE);
                viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.PLAYING));
                break;
            case MediaPlayer.Event.Paused:
                mPlaybackState = PlaybackState.PAUSED;
                viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.PAUSES));
                break;
            case MediaPlayer.Event.Stopped:
                mPlaybackState = PlaybackState.IDLE;
                long i = 0;
                if (event.getTimeChanged() != event.getLengthChanged())
                    i = event.getTimeChanged();
                resumeTime = i;
                pausedIn((int) i, selected);
                viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.STOPPED));
                if(videoItems.size()>0){
                    next();
                }
                break;
            case MediaPlayer.Event.LengthChanged:
                mMediaPlayer.setTime(resumeTime);
                viewModel.setScreenStateEvent(new ScreenEvent(event.getLengthChanged(), ScreenStateEvent.LENGTH_CHANGED));
                break;
            case MediaPlayer.Event.TimeChanged:
                viewModel.setScreenStateEvent(new ScreenEvent(event.getTimeChanged(), ScreenStateEvent.TIME_CHANGED));
                break;
        }
    }

    public void setResumeTime(long resumeTime) {
        this.resumeTime = resumeTime;
    }

    long resumeTime = 0;

    private void onUserInteraction(UserInteraction userInteraction) {
        switch (userInteraction.getEvent()) {
            case PLAY_MEDIA:
//                setAndPlay(userInteraction.getMediaUri());
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
                next();
                break;
            case PREVIOUS:
                previous();
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

    private void next() {
        if(videoItems.size()>selected){
            selected++;
            Log.e("ahmed","aaaaaaaaaaaaaaaaaaaaaaaaaaa"+selected);
            videoItem = videoItems.get(selected);
            viewModel.setScreenStateEvent(new ScreenEvent(videoItem.getTitle(), videoItem.getImageUrl()));
            final Media media = new Media(mLibVLC, Uri.parse(videoItems.get(selected).getUrl()));
            mMediaPlayer.setMedia(media);
            media.release();
            startOrEndList();
            play();
        }
    }

    private void previous() {
        if(selected>0){
            selected--;
            videoItem = videoItems.get(selected);
            viewModel.setScreenStateEvent(new ScreenEvent(videoItem.getTitle(), videoItem.getImageUrl()));
            final Media media = new Media(mLibVLC, Uri.parse(videoItems.get(selected).getUrl()));
            mMediaPlayer.setMedia(media);
            media.release();
            startOrEndList();
            play();
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

    // adding cast

    private CastContext mCastContext;
    private PlaybackLocation mLocation;

    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    private void startSearchForDevicesAndCast() {
        MediaRouter router = MediaRouter.getInstance(this);

        int count = router.getRoutes().size();

        List<MediaRouter.RouteInfo> j = router.getRoutes();
        MediaRouteSelector selector = new MediaRouteSelector.Builder().addControlCategory(
                CastMediaControlIntent.categoryForCast(getString(R.string.app_id))).build();

        router.addCallback(selector, new MediaRouter.Callback() {
            public int mRouteCount = 0;

            @Override
            public void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo route) {
                super.onRouteChanged(router, route);
                int count = router.getRoutes().size();
                if (count > 0) {
                    viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.SHOW_CAST_BTN));
                } else
                    viewModel.setScreenStateEvent(new ScreenEvent(ScreenStateEvent.HIDE_CAST_BTN));
            }

        }, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    private CastSession mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;

    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
                Log.e("ahmed2", "onSessionEnded");
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
                Log.e("ahmed2", "onSessionResumed");
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
                Log.e("ahmed2", "onSessionResumeFailed");
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
                Log.e("ahmed2", "onSessionStarting");
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
                Log.e("ahmed2", "onSessionStarting");
            }

            @Override
            public void onSessionStarting(CastSession session) {
                Log.e("ahmed2", "onSessionStarting");
            }

            @Override
            public void onSessionEnding(CastSession session) {
                Log.e("ahmed2", "onSessionEnding");
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {
                Log.e("ahmed2", "onSessionResuming");
                onApplicationConnected(session);
            }

            @Override
            public void onSessionSuspended(CastSession session, int reason) {
                Log.e("ahmed2", "onSessionSuspended");
            }

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;
                if (null != mMediaPlayer) {

//                    if (mMediaPlayer.getPlayerState() == MediaPlayer.Event.Playing) {
                    Log.e("ahmed", "Ok1");
                    mMediaPlayer.pause();
                    loadRemoteMedia((int) mMediaPlayer.getTime(), true);

                    videoImage.setVisibility(View.VISIBLE);
                    return;
//                    } else {
//                        Log.e("ahmed","Ok2");
//                        mPlaybackState = PlaybackState.IDLE;
//                        updatePlaybackLocation(PlaybackLocation.REMOTE);
//                    }
                }
//                updatePlayButton(mPlaybackState);
                supportInvalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                updatePlaybackLocation(PlaybackLocation.LOCAL);
                mPlaybackState = PlaybackState.IDLE;
                mLocation = PlaybackLocation.LOCAL;
//                updatePlayButton(mPlaybackState);
//                supportInvalidateOptionsMenu();
            }
        };
    }
    Handler handler = new Handler();

    private Runnable updateData = new Runnable(){
        public void run(){
            finish();
        }
    };
    private void updatePlaybackLocation(PlaybackLocation location) {
        mLocation = location;
//        if (location == PlaybackLocation.LOCAL) {
//            if (mPlaybackState == PlaybackState.PLAYING
//                    || mPlaybackState == PlaybackState.BUFFERING) {
//                setCoverArtStatus(null);
//                startControllersTimer();
//            } else {
//                stopControllersTimer();
//                setCoverArtStatus(mSelectedMedia.getImage(0));
//            }
//        } else {
//            stopControllersTimer();
//            setCoverArtStatus(mSelectedMedia.getImage(0));
//            updateControllersVisibility(false);
//        }
    }


    private PlaybackState mPlaybackState;

    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    @Override
    protected void onPause() {
        if (mOnLayoutChangeListener != null) {
            mVideoSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            mOnLayoutChangeListener = null;
        }
        isPlaying = mMediaPlayer.isPlaying();
        Log.e("onPauseIsPlaying", String.valueOf(isPlaying));
        mMediaPlayer.pause();

        mMediaPlayer.getVLCVout().detachViews();

        mConnectionClassManager.remove(this);
        super.onPause();
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);
    }

    private void loadRemoteMedia(int position, boolean autoPlay) {
        if (mCastSession == null) {
            return;
        }
        RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent(VlcPlayerActivity.this, ExpandedControlsActivity.class);
                startActivity(intent);
                remoteMediaClient.unregisterCallback(this);
            }
        });
        remoteMediaClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(buildMediaInfo())
                .setAutoplay(autoPlay)
                .setCurrentTime(position).build());

        handler.postDelayed(updateData,1000);
    }

    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

//        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mSelectedMedia.getStudio());
        movieMetadata.putString(MediaMetadata.KEY_TITLE, videoItem.getTitle());
        movieMetadata.addImage(new WebImage(Uri.parse(videoItem.getImageUrl())));
//        movieMetadata.addImage(new WebImage(Uri.parse("https://images.unsplash.com/photo-1506744038136-46273834b3fb?ixlib=rb-1.2.1&w=1000&q=80")));

//        viewModel.setScreenStateEvent(new ScreenEvent(videoItem.getImageUrl()));
        return new MediaInfo.Builder(videoItem.getUrl())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mkv")
                .setMetadata(movieMetadata)
//                .setStreamDuration(mSelectedMedia.getDuration() * 1000)
                .build();

    }
}