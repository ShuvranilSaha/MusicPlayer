package com.example.subhranil.simplemusicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class songPlayingFull extends AppCompatActivity {
    private static final String TAG = songPlayingFull.class.getName();
    private static final long PROGRESS_UPDATE_INTERVAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private final Handler mHandler = new Handler();
    private final Runnable updateProgressTrack = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor();
    private final MediaController.Callback mediaCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            Log.d(TAG, "onPlaybackStateChanged: " + state);
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            if (metadata != null) {
                updateMediaDescription(metadata.getDescription());
                updateDuration(metadata);
            }
        }
    };
    private ImageView skipNext;
    private ImageView skipPrev;
    private ImageView playPause;
    private TextView start;
    private TextView end;
    private SeekBar seekBar;
    private TextView songName;
    private TextView artistName;
    private View controllers;
    private Drawable pauseDrawable;
    private Drawable playDrawable;
    private ImageView backgroundImage;
    private String currentArtUrl;
    private MediaBrowser mediaBrowser;
    private MediaController.TransportControls transportControls = getMediaController().getTransportControls();
    private ScheduledFuture<?> scheduledFuture;
    private final MediaBrowser.ConnectionCallback connectionCallback = new MediaBrowser.ConnectionCallback() {
        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected: ");
            try {
                connectToSession(mediaBrowser.getSessionToken());
            } catch (RemoteException e) {
                Log.e(TAG, "onConnected: If failed", e);
            }
        }
    };
    private PlaybackState playbackState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_playing_full);

        backgroundImage = findViewById(R.id.background_image);
        playDrawable = getDrawable(R.drawable.ic_play_arrow_white_48dp);
        pauseDrawable = getDrawable(R.drawable.ic_pause_white_48dp);
        playPause = findViewById(R.id.play_pause);
        skipNext = findViewById(R.id.next);
        skipPrev = findViewById(R.id.prev);
        start = findViewById(R.id.startText);
        end = findViewById(R.id.endText);
        seekBar = findViewById(R.id.seekBar1);
        songName = findViewById(R.id.line1);
        artistName = findViewById(R.id.line2);
        controllers = findViewById(R.id.controllers);


        skipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transportControls.skipToNext();
            }
        });
        skipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transportControls.skipToPrevious();
            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaybackState state = getMediaController().getPlaybackState();
                if (state != null) {
                    switch (state.getState()) {
                        case PlaybackState.STATE_PLAYING:
                        case PlaybackState.STATE_BUFFERING:
                            transportControls.pause();
                            stopSeekBarUpdate();
                            break;
                        case PlaybackState.STATE_PAUSED:
                        case PlaybackState.STATE_STOPPED:
                            transportControls.play();
                            scheduledSeekBarUpdate();
                            break;
                        Log.d(TAG, "onClick: " + state.getState());
                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                start.setText(DateUtils.formatElapsedTime(i / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekBarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getMediaController().getTransportControls().seekTo(seekBar.getProgress());
                scheduledSeekBarUpdate();
            }
        });

        if (savedInstanceState == null) {
            updateFromParam(getIntent());
        }
        mediaBrowser = new MediaBrowser(this, new ComponentName
                (this, MediaPlayerService.class), connectionCallback, null);
    }

    private void connectToSession(MediaSession.Token token) {
        MediaController controller = new MediaController(songPlayingFull.this, token);
        if (controller.getMetadata() == null) {
            finish();
            return;
        }
        setMediaController(controller);
        controller.registerCallback(mediaCallback);
        PlaybackState state = controller.getPlaybackState();
        updatePlaybackState(state);
        MediaMetadata metadata = controller.getMetadata();
        if (metadata != null) {
            updateMediaDescription(metadata.getDescription());
            updateDuration(metadata);
        }
        updateProgress();
        if (state != null && (state.getState() == PlaybackState.STATE_BUFFERING ||
                state.getState() == PlaybackState.STATE_PLAYING)) {
            scheduledSeekBarUpdate();
        }
    }

    private void updateFromParam(Intent intent) {
        if (intent != null) {
            MediaDescription description = intent.getParcelableExtra(MainActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION);
            if (description != null)
                updateMediaDescription(description);
        }
    }

    private void scheduledSeekBarUpdate() {
        stopSeekBarUpdate();
        if (!executorService.isShutdown()) {
            scheduledFuture = executorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(updateProgressTrack);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL, PROGRESS_UPDATE_INTERVAL, TimeUnit.MILLISECONDS
            );
        }
    }

    private void stopSeekBarUpdate() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mediaBrowser != null) {
            mediaBrowser.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaBrowser != null) {
            mediaBrowser.disconnect();
        }
        if (getMediaController() != null) {
            getMediaController().unregisterCallback(mediaCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSeekBarUpdate();
        executorService.shutdown();
    }
}
