package com.example.music;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.music.Model.SongModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class StorageUtil {
    private final String STORAGE = "STORAGE";
    private final String STORAGE_LAST_SONG = "STORAGE_LAST_SONG";

    public static final String SONG_ARRAY_LIST = "SONG_ARRAY_LIST";
    public static final String SONG_POSITION = "SONG_POSITION";
    public static final String ACTIVE_SONG = "ACTIVE_SONG";
    public static final String SONG_DURATION = "SONG_DURATION";
    public static final String DURATION_IN_MINUTES = "DURATION_IN_MINUTES";

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

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
