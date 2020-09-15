package com.example.music.Model;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

public class PlaylistModel {
    private String name;
    private ArrayList<String> songs;
    private String key;

    public PlaylistModel() {}

    public PlaylistModel(String name, ArrayList<String> songs) {
        this.name = name;
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<String> songs) {
        this.songs = songs;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }
}
