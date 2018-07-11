package com.bstech.voicechanger.fragment;

/**
 * Created by Giga on 7/9/2018.
 */

public interface AudioTunePresenter {
    void playAudio();

    void playNextAudio();

    void playPreviousAudio();

    void fastNextAudio();

    void fastPreviousAudio();

    void shuffleAudio();

    void repeatAudio();
}
