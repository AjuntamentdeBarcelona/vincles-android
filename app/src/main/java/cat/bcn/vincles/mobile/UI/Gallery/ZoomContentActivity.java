package cat.bcn.vincles.mobile.UI.Gallery;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.File;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.Utils.ImageUtils;

public class ZoomContentActivity extends BaseActivity implements Player.EventListener, PlaybackControlView.VisibilityListener, OnViewTapListener, View.OnClickListener {

    View back;
    SimpleExoPlayerView exoPlayerView;
    ExoPlayer exoPlayer;
    String filePath = "";
    boolean isVideo = false;
    boolean isPlaying = true;
    long currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide both the navigation bar and the status bar.
        LoginActivity.screenOrientation = -1;
        View decorView = getWindow().getDecorView();
        int uiOptions = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    |  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        decorView.setSystemUiVisibility(uiOptions);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getLong("currentPosition");
            isPlaying = savedInstanceState.getBoolean("isPlaying");
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            filePath = bundle.getString("filePath");
            String mimeType = bundle.getString("mimeType");
            if (mimeType != null) {
                isVideo = mimeType.contains("video");
            }
        }


        setContentView(isVideo ? R.layout.zoom_content_activity_video: R.layout.zoom_content_activity_picture);

        if (isVideo) {
            currentPosition = 0;
            isPlaying = true;
            exoPlayerView = findViewById(R.id.video_view);
            setupExoPlayer(this, true);
        } else {
            PhotoView photoView = findViewById(R.id.photo_view);
            ImageUtils.setImageToImageView(new File(filePath), photoView, this, false);

            /*
            Glide.with(this)
                    .load(new File(filePath))
                    .into(photoView);
                    */
            photoView.setOnViewTapListener(new OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {

                }
            });

            findViewById(R.id.background).setOnClickListener(this);
            photoView.setOnViewTapListener(this);
        }

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVideo) {
            setupExoPlayer(this, true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putLong("currentPosition", currentPosition);
        bundle.putBoolean("isPlaying", isPlaying);
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            isPlaying = exoPlayer.getPlayWhenReady();
            currentPosition = exoPlayer.getCurrentPosition();
            exoPlayer.release();
            exoPlayer = null;
            exoPlayerView.setPlayer(null);
        }
    }

    private void setupExoPlayer(Context context, boolean isOnResume) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
            exoPlayer.addListener(this);
            exoPlayerView.setPlayer((SimpleExoPlayer) exoPlayer);
            exoPlayer.setPlayWhenReady(true);
        }
        if (isOnResume) {
            prepareExoPlayerFromFileUri(Uri.parse(filePath));
            exoPlayer.seekTo(currentPosition);
            exoPlayer.setPlayWhenReady(isPlaying);
            exoPlayerView.setControllerVisibilityListener(this);
        }
    }

    private void prepareExoPlayerFromFileUri(Uri uri){

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        exoPlayer.setPlayWhenReady(false);
        exoPlayer.prepare(audioSource);


    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            isPlaying = false;
            currentPosition = 0;
            prepareExoPlayerFromFileUri(Uri.parse(filePath));
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onVisibilityChange(int visibility) {
        back.setVisibility(visibility);
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        back.setVisibility(back.getVisibility()==View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.background) {
            back.setVisibility(back.getVisibility()==View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }
}
