package com.example.music.Model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class SongModel implements Serializable {
    private String title;
    private String album;
    private String artist;
    private String songUrl;
    private String imageUrl;
    private String key;

    public SongModel() {}

    public SongModel(String title, String album, String artist, String songUrl, String imageUrl) {
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.songUrl = songUrl;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
