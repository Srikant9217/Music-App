package com.example.music.Model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class ArtistModel implements Serializable {
    private String name;
    private String imageUrl;
    private String key;

    public ArtistModel() {}

    public ArtistModel(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
