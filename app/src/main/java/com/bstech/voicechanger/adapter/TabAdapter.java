package com.bstech.voicechanger.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bstech.voicechanger.fragment.AudioTuneFragment;
import com.bstech.voicechanger.fragment.RecorderFragment;

/**
 * Created by Giga on 7/4/2018.
 */

public class TabAdapter extends FragmentStatePagerAdapter {

    private  String RECORDER = "RECORDER";
    private  String AUDIO_TUNE = "AUDIO TUNE";

    private static final int INDEX_RECORDER = 0;
    private static final int INDEX_AUDIO_TUNE = 1;

    private String listTab[] = {RECORDER, AUDIO_TUNE};

    private AudioTuneFragment audioTuneFragment;
    private RecorderFragment recorderFragment;

    public TabAdapter(FragmentManager fm) {
        super(fm);
        audioTuneFragment = new AudioTuneFragment();
        recorderFragment = new RecorderFragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case INDEX_RECORDER:
                return recorderFragment;

            case INDEX_AUDIO_TUNE:
                return audioTuneFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return listTab.length;
    }
}
