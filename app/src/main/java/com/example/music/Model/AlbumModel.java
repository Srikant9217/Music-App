package com.example.music.Model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Objects;

public class AlbumModel implements Serializable {
    private String name;
    private String imageUrl;
    private String key;

    public AlbumModel() {}

    public AlbumModel(String name, String imageUrl) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlbumModel that = (AlbumModel) o;
        return name.equals(that.name) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, imageUrl, key);
    }
}
