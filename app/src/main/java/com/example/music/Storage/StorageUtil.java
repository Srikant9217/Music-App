package com.example.music.Storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.PlaybackStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class StorageUtil {
    private final String STORAGE = "STORAGE";
    private final String SONG_ARRAY_LIST = "SONG_ARRAY_LIST";
    private final String SONG_POSITION = "SONG_POSITION";

    private final String USER_STORAGE = "USER_STORAGE";
    private final String FAVOURITE_SONGS = "FAVOURITE_SONGS";
    private final String FAVOURITE_ARTISTS = "FAVOURITE_ARTISTS";
    private final String FAVOURITE_ALBUMS = "FAVOURITE_ALBUMS";

    private final String STORAGE_LAST_SONG = "STORAGE_LAST_SONG";
    private final String ACTIVE_SONG = "ACTIVE_SONG";
    private final String SONG_DURATION = "SONG_DURATION";
    private final String DURATION_IN_MINUTES = "DURATION_IN_MINUTES";
    private final String PLAYBACK_STATUS = "PLAYBACK_STATUS";
    private final String FOCUS = "FOCUS";

    private final String SETTINGS = "SETTINGS";
    private final String FIRST_START = "FIRST_START";
    private final String FIRST_TIME = "FIRST_TIME";

    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }



    public void storeAudioList(ArrayList<SongModel> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(SONG_ARRAY_LIST, json);
        editor.apply();
    }

    public ArrayList<SongModel> loadAudioList() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(SONG_ARRAY_LIST, null);
        Type type = new TypeToken<ArrayList<SongModel>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SONG_POSITION, index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(SONG_POSITION, -1);
    }


    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public void storeFavouriteSongs(ArrayList<SongModel> arrayList) {
        preferences = context.getSharedPreferences(USER_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(FAVOURITE_SONGS, json);
        editor.apply();
    }

    public ArrayList<SongModel> loadFavouriteSongs() {
        preferences = context.getSharedPreferences(USER_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(FAVOURITE_SONGS, null);
        Type type = new TypeToken<ArrayList<SongModel>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void storeFavouriteArtists(ArrayList<ArtistModel> arrayList) {
        preferences = context.getSharedPreferences(USER_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(FAVOURITE_ARTISTS, json);
        editor.apply();
    }

    public ArrayList<ArtistModel> loadFavouriteArtists() {
        preferences = context.getSharedPreferences(USER_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(FAVOURITE_ARTISTS, null);
        Type type = new TypeToken<ArrayList<ArtistModel>>() {}.getType();
        return gson.fromJson(json, type);
    }


    public void storeFavouriteAlbums(ArrayList<AlbumModel> arrayList) {
        preferences = context.getSharedPreferences(USER_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(FAVOURITE_ALBUMS, json);
        editor.apply();
    }

    public ArrayList<AlbumModel> loadFavouriteAlbums() {
        preferences = context.getSharedPreferences(USER_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(FAVOURITE_ALBUMS, null);
        Type type = new TypeToken<ArrayList<AlbumModel>>() {}.getType();
        return gson.fromJson(json, type);
    }


    public void clearCachedUserStorage() {
        preferences = context.getSharedPreferences(USER_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }



    public void storeActiveSong(SongModel activeSong) {
        preferences = context.getSharedPreferences(STORAGE_LAST_SONG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(activeSong);
        editor.putString(ACTIVE_SONG, json);
        editor.apply();
    }

    public SongModel loadActiveSong() {
        preferences = context.getSharedPreferences(STORAGE_LAST_SONG, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(ACTIVE_SONG, null);
        Type type = new TypeToken<SongModel>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void storeSongDuration(int duration) {
        preferences = context.getSharedPreferences(STORAGE_LAST_SONG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SONG_DURATION, duration);
        editor.apply();
    }

    public int loadSongDuration() {
        preferences = context.getSharedPreferences(STORAGE_LAST_SONG, Context.MODE_PRIVATE);
        return preferences.getInt(SONG_DURATION, -1);
    }

    public void storeSongDurationInMinutes(String time) {
        preferences = context.getSharedPreferences(STORAGE_LAST_SONG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DURATION_IN_MINUTES, time);
        editor.apply();
    }

    public String loadSongDurationInMinutes() {
        preferences = context.getSharedPreferences(STORAGE_LAST_SONG, Context.MODE_PRIVATE);
        return preferences.getString(DURATION_IN_MINUTES, "null");
    }

    public void storePlaybackStatus(PlaybackStatus status) {
        preferences = context.getSharedPreferences(STORAGE_LAST_SONG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PLAYBACK_STATUS, status.toString());
        editor.apply();
    }

    public PlaybackStatus loadPlaybackStatus() {
        preferences = context.getSharedPreferences(STORAGE_LAST_SONG, Context.MODE_PRIVATE);
        String status = preferences.getString(PLAYBACK_STATUS, PlaybackStatus.PAUSED.toString());
        return PlaybackStatus.toStatus(status);
    }

    public void storeFocus(boolean bool) {
        preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(FOCUS, bool);
        editor.apply();
    }

    public boolean loadFocus() {
        preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return preferences.getBoolean(FOCUS, false);
    }


    public void storeSettingFirstStart(boolean bool) {
        preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(FIRST_START, bool);
        editor.apply();
    }

    public boolean loadSettingFirstStart() {
        preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return preferences.getBoolean(FIRST_START, true);
    }

    public void storeSettingFirstTime(boolean bool) {
        preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(FIRST_TIME, bool);
        editor.apply();
    }

    public boolean loadSettingFirstTime() {
        preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return preferences.getBoolean(FIRST_TIME, true);
    }

}
