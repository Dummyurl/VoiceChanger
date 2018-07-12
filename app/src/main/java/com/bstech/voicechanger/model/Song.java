package com.bstech.voicechanger.model;

import android.net.Uri;

/**
 * Created by Giga on 7/4/2018.
 */

public class Song {
    private String nameSong;
    private String nameArtist;
    private long duration;
    private String path;

    public Uri getUriImage() {
        return uriImage;
    }

    public void setUriImage(Uri uriImage) {
        this.uriImage = uriImage;
    }

    private Uri uriImage;

    public Song(){}


    public Song(String nameSong, String nameArtist, long duration, String path) {
        this.nameSong = nameSong;
        this.nameArtist = nameArtist;
        this.duration = duration;
        this.path = path;
    }



    public String getNameSong() {
        return nameSong;
    }

    public void setNameSong(String nameSong) {
        this.nameSong = nameSong;
    }

    public String getNameArtist() {
        return nameArtist;
    }

    public void setNameArtist(String nameArtist) {
        this.nameArtist = nameArtist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
