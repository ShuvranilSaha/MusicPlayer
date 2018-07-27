package com.example.subhranil.simplemusicplayer;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;

public class SongPlayingActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = SongPlayingActivity.class.getName();
    MediaPlayer mediaPlayer;
    List<SongFile> trackList = new ArrayList<>();
    Handler handler;
    Runnable runnable;
    StorageUtility utility = new StorageUtility(this);
    private ImageView skipNext;
    private ImageView skipPrev;
    private ImageView playPause;
    private SeekBar seekBar;
    private TextView songName;
    private TextView artistName;
    private View controllers;
    private Drawable pauseDrawable;
    private Drawable playDrawable;
    private ImageView backgroundImage;
    private TextView startTime;
    private TextView endTime;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_playing_full);
        trackList = utility.loadSongs();
        mediaPlayer = new MediaPlayer();
        handler = new Handler();

        currentPosition = getIntent().getIntExtra("index", 0);
        seekBar = findViewById(R.id.seekBar1);
        startTime = findViewById(R.id.startText);
        endTime = findViewById(R.id.endText);
        songName = findViewById(R.id.line1);
        artistName = findViewById(R.id.line2);
        playPause = findViewById(R.id.play_pause);
        pauseDrawable = getDrawable(R.drawable.ic_pause_white_48dp);
        playDrawable = getDrawable(R.drawable.ic_play_arrow_white_48dp);
        backgroundImage = findViewById(R.id.background_image);
        skipNext = findViewById(R.id.next);
        skipPrev = findViewById(R.id.prev);
        controllers = findViewById(R.id.controllers);

        setSongName(trackList.get(currentPosition).getData(), trackList.get(currentPosition).getTitle());
        playSong(currentPosition);
        backgroundImage.setImageBitmap(utility.getAlbumArt(trackList.get(currentPosition).getAlbumArt(), this));


        skipNext.setOnClickListener(this);
        skipPrev.setOnClickListener(this);
        playPause.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    startTime.setText(DateUtils.formatElapsedTime(i / 1000));
                    mediaPlayer.seekTo(i);

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

    private void playLoop() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    playLoop();
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    private void playSong(int currentPosition) {
        try {
            if (mediaPlayer != null) {
                FileInputStream fis = new FileInputStream(trackList.get(currentPosition).getData());
                FileDescriptor fileD = fis.getFD();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(fileD);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        int duration = mediaPlayer.getDuration();
                        endTime.setText(DateUtils.formatElapsedTime(duration / 1000));
                        seekBar.setMax(mediaPlayer.getDuration());
                        controllers.setVisibility(VISIBLE);
                        mediaPlayer.start();
                        playLoop();
                        playPause.setImageDrawable(pauseDrawable);

                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onClick " + e.getLocalizedMessage());
        }
    }

    private void setSongName(String songPath, String name) {
        Log.d(TAG, "setSongName: setting the songName ");
        songName.setText(name);
        artistName.setText(trackList.get(currentPosition).getTitle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: view Clicked ");

        switch (view.getId()) {
            case R.id.play_pause:
                Log.d(TAG, "onClick: " + view.getId());
                if (mediaPlayer.isPlaying()) {
                    controllers.setVisibility(VISIBLE);
                    playPause.setVisibility(VISIBLE);
                    playPause.setImageDrawable(playDrawable);

                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                    playPause.setImageDrawable(pauseDrawable);

                }
                setSongName(trackList.get(currentPosition).getData(), trackList.get(currentPosition).getTitle());
                backgroundImage.setImageBitmap(utility.getAlbumArt(trackList.get(currentPosition).getAlbumArt(), this));
                break;
            case R.id.next:
                Log.d(TAG, "onClick: " + view.getId());
                currentPosition++;
                if (currentPosition > (trackList.size() - 1)) {
                    currentPosition = 0;
                }
                setSongName(trackList.get(currentPosition).getData(), trackList.get(currentPosition).getTitle());
                backgroundImage.setImageBitmap(utility.getAlbumArt(trackList.get(currentPosition).getAlbumArt(), this));
                controllers.setVisibility(VISIBLE);
                playLoop();
                skipNext.setImageResource(R.drawable.ic_skip_next_white_48dp);
                playPause.setImageDrawable(pauseDrawable);

                playSong(currentPosition);
                break;
            case R.id.prev:
                Log.d(TAG, "onClick: " + view.getId());
                currentPosition--;
                if (currentPosition < 0) {
                    currentPosition = trackList.size() - 1;
                }
                setSongName(trackList.get(currentPosition).getData(), trackList.get(currentPosition).getTitle());
                backgroundImage.setImageBitmap(utility.getAlbumArt(trackList.get(currentPosition).getAlbumArt(), this));
                controllers.setVisibility(VISIBLE);
                skipPrev.setImageResource(R.drawable.ic_skip_previous_white_48dp);
                playLoop();
                skipPrev.setVisibility(VISIBLE);
                playPause.setImageDrawable(pauseDrawable);

                playSong(currentPosition);
                break;
            default:
                Log.d(TAG, "onClick: " + view.getId());
                setSongName(trackList.get(currentPosition).getData(), trackList.get(currentPosition).getTitle());
                playSong(currentPosition);
                playPause.setImageDrawable(pauseDrawable);
                controllers.setVisibility(VISIBLE);
        }
    }

}
