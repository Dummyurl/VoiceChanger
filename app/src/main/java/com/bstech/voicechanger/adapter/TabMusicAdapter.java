package com.bstech.voicechanger.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bstech.voicechanger.fragment.ChooseDeviceFragment;
import com.bstech.voicechanger.fragment.ChooseMyStudioFragment;
import com.bstech.voicechanger.utils.Utils;

/**
 * Created by Giga on 7/19/2018.
 */
public class TabMusicAdapter extends FragmentStatePagerAdapter {

    public static final int INDEX_MY_STUDIO = 0;
    public static final int INDEX_DEVICE = 1;
    private String RECORDER = "RECORDER";
    private String AUDIO_TUNE = "AUDIO TUNE";
    private String listTab[] = {RECORDER, AUDIO_TUNE};
    private ChooseDeviceFragment chooseMyStudioFragment;
    private ChooseDeviceFragment chooseDeviceFragment;


    public TabMusicAdapter(FragmentManager fm) {
        super(fm);
        Bundle b = new Bundle();
        b.putInt(Utils.STATE_SELECT_MUSIC,INDEX_MY_STUDIO);
        chooseDeviceFragment = ChooseDeviceFragment.newInstance();
        Bundle bundle =new Bundle();
        bundle.putInt(Utils.STATE_SELECT_MUSIC,INDEX_DEVICE);
        chooseMyStudioFragment = ChooseDeviceFragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case INDEX_MY_STUDIO:

                return chooseMyStudioFragment;

            case INDEX_DEVICE:

                return chooseDeviceFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return listTab.length;
    }
}