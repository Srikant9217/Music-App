package com.example.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.example.music.Model.SongModel;
import com.squareup.picasso.Picasso;

public class SongController extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private ImageView imageView;
    private TextView textViewTitle;
    private TextView textViewArtist;
    private ImageView imageViewFavourite;
    private TextView textViewLiveTime;
    private TextView textViewTotalTime;
    private SeekBar seekBar;
    private ProgressBar progressBar;
    private ImageView playPauseButton;

    private SongModel activeSong;
    private String activeImageUrl = "";
    private int songDuration;
    private String songDurationInMinutes;

    private StorageUtil storage;

    private FavouriteSongs favouriteSongs;

    private ConstraintLayout layout;
    private ConstraintSet constraintSetCollapsed = new ConstraintSet();
    private ConstraintSet constraintSetExpanded = new ConstraintSet();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_song_controller_collapsed, container, false);
        layout = v.findViewById(R.id.song_controller_layout);
        constraintSetCollapsed.clone(layout);
        constraintSetExpanded.clone(getActivity(), R.layout.fragment_song_controller_expanded);

        imageView = v.findViewById(R.id.image_view_song_controller_song_image);
        textViewTitle = v.findViewById(R.id.text_view_song_controller_title);
        textViewArtist = v.findViewById(R.id.text_view_song_controller_artist);
        imageViewFavourite = v.findViewById(R.id.image_view_song_controller_favourite);
        textViewLiveTime = v.findViewById(R.id.text_view_song_controller_live_time);
        textViewTotalTime = v.findViewById(R.id.text_view_song_controller_total_time);
        seekBar = v.findViewById(R.id.seek_bar_song_controller);
        progressBar = v.findViewById(R.id.progress_bar_song_controller);
        playPauseButton = v.findViewById(R.id.image_view_song_controller_play_pause);

        seekBar.setOnSeekBarChangeListener(this);

        favouriteSongs = FavouriteSongs.getInstance(getActivity());

        intiController();

        register_receiver();

        return v;
    }

    private void intiController() {
        storage = new StorageUtil(getActivity());
        activeSong = storage.loadActiveSong();
        songDuration = storage.loadSongDuration();
        songDurationInMinutes = storage.loadSongDurationInMinutes();
        if (activeSong != null) {
            loadSongData();
        }
    }


    public void expandController(View v) {
        TransitionManager.beginDelayedTransition(layout);
        constraintSetExpanded.applyTo(layout);
        v.setBackgroundColor(Color.BLACK);
    }

    public void collapseController(View v) {
        TransitionManager.beginDelayedTransition(layout);
        constraintSetCollapsed.applyTo(layout);
        v.setBackgroundColor(getResources().getColor(R.color.songControllerBackground));
    }

    public void openOptions(View v) {

    }

    public void openArtist(View v) {

    }

    public void likeSong(View v) {
        boolean isFavourite = favouriteSongs.favourite(activeSong, getActivity());
        if (isFavourite) {
            imageViewFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
        } else {
            imageViewFavourite.setImageResource(R.drawable.ic_baseline_not_favorite);
        }

    }

    public void repeatSong(View v) {

    }

    public void previousSong(View v) {

    }

    public void loadPlayPauseButton(View v) {
        ImageView iconPlayPause = v.findViewById(R.id.image_view_song_controller_play_pause);
        if (storage.loadPlaybackStatus() == PlaybackStatus.PLAYING) {
            iconPlayPause.setImageResource(R.drawable.ic_baseline_pause_24);
        } else if (storage.loadPlaybackStatus() == PlaybackStatus.PAUSED) {
            iconPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    public void playPauseSong(View v) {

    }

    public void nextSong(View v) {

    }

    public void shufflePlaylist(View v) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b) {
            switch (seekBar.getId()) {
                case R.id.seek_bar_song_controller:
                    ((MainActivity) getActivity()).seekTo(i);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void loadSongData() {
        Picasso.with(getActivity())
                .load(activeSong.getImageUrl())
                .fit()
                .centerCrop()
                .into(imageView);
        textViewTitle.setText(activeSong.getTitle());
        textViewArtist.setText(activeSong.getArtist());

        seekBar.setMax(songDuration);
        textViewTotalTime.setText(songDurationInMinutes);
        loadPlayPauseButton(playPauseButton);
        if (storage.loadFavouriteSongs() != null) {
            if (favouriteSongs.isFavourite(activeSong)) {
                imageViewFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
            }else {
                imageViewFavourite.setImageResource(R.drawable.ic_baseline_not_favorite);
            }
        }
    }

    private void updateSongData(Intent intent) {
        activeSong = (SongModel) intent.getSerializableExtra("activeSong");
        songDuration = intent.getIntExtra("duration", 1);
        songDurationInMinutes = intent.getStringExtra("totalTime");

        if (!activeImageUrl.equals(activeSong.getImageUrl())) {
            activeImageUrl = activeSong.getImageUrl();
            Picasso.with(getActivity())
                    .load(activeImageUrl)
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }
        textViewTitle.setText(activeSong.getTitle());
        textViewArtist.setText(activeSong.getArtist());
        seekBar.setMax(songDuration);
        textViewTotalTime.setText(songDurationInMinutes);
        loadPlayPauseButton(playPauseButton);
        if (storage.loadFavouriteSongs() != null) {
            if (favouriteSongs.isFavourite(activeSong)) {
                imageViewFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
            }else {
                imageViewFavourite.setImageResource(R.drawable.ic_baseline_not_favorite);
            }
        }

        storage.storeActiveSong(activeSong);
        storage.storeSongDuration(songDuration);
        storage.storeSongDurationInMinutes(songDurationInMinutes);
    }

    private void updateProgressData(Intent intent) {
        int progress = intent.getIntExtra("currentPosition", 2);
        seekBar.setProgress(progress);
        progressBar.setProgress(100 * progress / songDuration);
        textViewLiveTime.setText(intent.getStringExtra("currentDuration"));
    }

    private void resetProgressData(){
        seekBar.setProgress(0);
        progressBar.setProgress(0);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case MediaPlayerService.UPDATE_SONG_DATA:
                    updateSongData(intent);
                    break;
                case MediaPlayerService.UPDATE_PROGRESS_DATA:
                    updateProgressData(intent);
                    break;
                case MediaPlayerService.RESET_PROGRESS_DATA:
                    resetProgressData();
                    break;
            }
        }
    };

    private void register_receiver() {
        IntentFilter filter1 = new IntentFilter(MediaPlayerService.UPDATE_SONG_DATA);
        ((MainActivity) getActivity()).registerReceiver(receiver, filter1);

        IntentFilter filter2 = new IntentFilter(MediaPlayerService.UPDATE_PROGRESS_DATA);
        ((MainActivity) getActivity()).registerReceiver(receiver, filter2);
    }
}