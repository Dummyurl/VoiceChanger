package com.bstech.voicechanger.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Giga on 7/4/2018.
 */

public class Song implements Parcelable {
    private String nameSong;
    private String nameArtist;
    private long duration;
    private String path;
    private boolean isCheck;

    protected Song(Parcel in) {
        nameSong = in.readString();
        nameArtist = in.readString();
        duration = in.readLong();
        path = in.readString();
        isCheck = in.readByte() != 0;
        uriImage = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getUriImage() {
        return uriImage;
    }

    public void setUriImage(String uriImage) {
        this.uriImage = uriImage;
    }

    private String uriImage;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(nameSong);
        parcel.writeString(nameArtist);
        parcel.writeLong(duration);
        parcel.writeString(path);
        parcel.writeByte((byte) (isCheck ? 1 : 0));
        parcel.writeString(uriImage);
    }
}
