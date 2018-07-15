package com.bstech.voicechanger.fragment;


import com.bstech.voicechanger.model.Song;

import java.util.List;

public interface AudioTuneView {
    void onUpdatePlay(boolean isPlaying);

    void onServiceNull();

    void onUpdatePitchSemi(float pitchSemi);

    void onUpdateTempo(float tempo);

    void onAddSongSuccess(Song song);

    void onAddSongFail();

    void onUpdateTitleSong(Song song);

    void onUpdateRefreshTempo(float tempo);

    void onUpdateRefreshPitch(float pitch);

    void onGetDataSuccess(List<Song> songs, float pitSemi, float tempo);

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
