package com.bstech.voicechanger.fragment;


import com.bstech.voicechanger.model.Song;

import java.util.List;

public interface AudioTuneView {
    void onUpdatePlay(boolean isPlaying);

    void onServiceNull();

    void onUpdatePitchSemi(float pitchSemi, boolean isRefresh);

    void onUpdateTempo(float tempo, boolean isRefresh);

    void onUpdateRate(float rate, boolean isRefresh);

    void onAddSongSuccess(Song song);

    void onAddSongFail();

    void onUpdateTitleSong(Song song);

    void onGetDataSuccess(List<Song> songs, float pitSemi, float tempo, float rate);

    void onFailedGetData();

    void onShowInputSuccess(float value, int type);

    void onShowInputFail();

    void onShowSetRate();

    void onHideSetRate();


    interface Shuffle {
        void offShuffle();

        void onShuffle();
    }

    interface Repeat {
        void onNoRepeat();

        void onRepeatOne();

        void onRepeatAll();
    }
}
