package com.example.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String SERVICE_STATUS = "serviceStatus";
    public static final String PLAY_NEW_AUDIO = "playNewAudio";

    private MediaPlayerService player;
    boolean serviceBound = false;

    private StorageUtil storage;
    private ArrayList<SongModel> oldSongs;
    private int oldAudioIndex;

    private View navHostFragment;
    private SongController songControllerFragment;
    private BottomNavigationView navView;
    private boolean expanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        navHostFragment = findViewById(R.id.nav_host_fragment);
        songControllerFragment = new SongController();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_controller_container, songControllerFragment)
                .commit();

        storage = new StorageUtil(getApplicationContext());
        oldSongs = storage.loadAudioList();
        oldAudioIndex = storage.loadAudioIndex();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent playerIntent = new Intent(this, MediaPlayerService.class);
        startService(playerIntent);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void expandController(View v) {
        navView.setVisibility(View.GONE);
        songControllerFragment.expandController(v);
        navHostFragment.setVisibility(View.GONE);
        expanded = true;
    }

    public void collapseController(View v) {
        songControllerFragment.collapseController(v);
        navView.setVisibility(View.VISIBLE);
        navHostFragment.setVisibility(View.VISIBLE);
        expanded = false;
    }

    public void openOptions(View v) {
        Toast.makeText(MainActivity.this, "Menu", Toast.LENGTH_SHORT).show();
    }

    public void openArtist(View v) {
        Toast.makeText(MainActivity.this, "Artist", Toast.LENGTH_SHORT).show();
    }

    public void seekTo(int position) {
        player.seekToSong(position);
    }

    public void likeSong(View v) {
        songControllerFragment.likeSong(v);
    }

    public void repeatSong(View v) {
        Toast.makeText(MainActivity.this, "Repeat", Toast.LENGTH_SHORT).show();
    }

    public void previousSong(View v) {
        player.previousSong();
    }

    public void playPauseSong(View v) {
        if (!serviceBound) {
            if (oldSongs != null && !oldSongs.isEmpty()) {
                playSong(oldSongs, oldAudioIndex);
            }
        } else {
            PlaybackStatus status = player.playPause();
            songControllerFragment.loadPlayPauseButton(v, status);
        }
    }

    public void nextSong(View v) {
        player.nextSong();
    }

    public void shufflePlaylist(View v) {
        Toast.makeText(MainActivity.this, "Shuffle Playlist", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (expanded) {
            View v = findViewById(R.id.image_view_song_controller_down);
            collapseController(v);
        } else {
            super.onBackPressed();
        }
    }

    public void playSong(ArrayList<SongModel> songList, int position) {
        storage.storeAudioList(songList);
        storage.storeAudioIndex(position);
        Intent broadcastIntent = new Intent(PLAY_NEW_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle
            outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(SERVICE_STATUS, serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean(SERVICE_STATUS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound) {
            unbindService(serviceConnection);
        }
    }
}