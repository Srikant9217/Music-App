package com.example.music;


public enum PlaybackStatus {
    PLAYING, PAUSED;

    public static PlaybackStatus toStatus(String string){
        try {
            return valueOf(string);
        }catch (Exception e){
            return PAUSED;
        }
    }
}
