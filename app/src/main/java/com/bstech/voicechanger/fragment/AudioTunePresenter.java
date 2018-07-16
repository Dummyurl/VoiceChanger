package com.bstech.voicechanger.fragment;

import android.content.Context;
import android.net.Uri;

import com.bstech.voicechanger.model.Song;

import java.util.List;


public interface AudioTunePresenter {
    void playAudio(List<Song> list, int index);

    void onPlayIndexAudio(List<Song> songs, int index);

    void onPlayNext();

    void onPlayPrevious();

    void onFastNextSong();

    void onFastPreviousSong();

    void onShuffle();

    void onRepeat();

    void onSetTempo(float tempo);

    void onSetPitchSemi(float pitchSemi);

    void onSetRate(float rate);

    void onSortedList(List<Song> songs);

    void onAddSongToListPlay(Song song, List<Song> songs, Uri uri, Context context);

    void refreshPitchSemi();

    void refreshTempo();

    void refreshRate();

    void getData();

    void inputValue(String value,int typeInput);

}
