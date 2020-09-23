package com.example.music.Model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;

public class UserModel implements Serializable {
    private String UserId;
    private String name;
    private ArrayList<SongModel> favouriteSongs;
    private ArrayList<ArtistModel> favouriteArtists;
    private ArrayList<AlbumModel> favouriteAlbums;
    private ArrayList<PlaylistModel> playlists;
    private String key;

    public UserModel() {}

    public UserModel(String userId, String name) {
        UserId = userId;
        this.name = name;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<SongModel> getFavouriteSongs() {
        return favouriteSongs;
    }

    public void setFavouriteSongs(ArrayList<SongModel> favouriteSongs) {
        this.favouriteSongs = favouriteSongs;
    }

    public ArrayList<ArtistModel> getFavouriteArtists() {
        return favouriteArtists;
    }

    public void setFavouriteArtists(ArrayList<ArtistModel> favouriteArtists) {
        this.favouriteArtists = favouriteArtists;
    }

    public ArrayList<AlbumModel> getFavouriteAlbums() {
        return favouriteAlbums;
    }

    public void setFavouriteAlbums(ArrayList<AlbumModel> favouriteAlbums) {
        this.favouriteAlbums = favouriteAlbums;
    }

    public ArrayList<PlaylistModel> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(ArrayList<PlaylistModel> playlists) {
        this.playlists = playlists;
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
