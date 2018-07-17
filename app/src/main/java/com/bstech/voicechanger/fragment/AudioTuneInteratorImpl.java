package com.bstech.voicechanger.fragment;

import android.util.Log;

import com.bstech.voicechanger.service.MusicService;
import com.bstech.voicechanger.utils.SharedPrefs;

import static com.bstech.voicechanger.utils.Utils.COMPINE;
import static com.bstech.voicechanger.utils.Utils.STATE_OFF;
import static com.bstech.voicechanger.utils.Utils.STATE_ON;

public class AudioTuneInteratorImpl implements AudioTuneInteractor {

    private boolean isTextEmpty(String s) {
        if (s != null && s.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public void onGetData(MusicService service, OnGetDataListener listener) {
        if (service != null && service.mPlayer != null) {
            listener.onGetDataSuccess();
        }
    }


    @Override
    public void onInputValue(String value, OnInputValueListener listener, int type) {
        if (isTextEmpty(value)) {
            listener.onInputFail();
        } else {
            Log.e("xxx", "cuss");
            listener.onInputSuccess(value, type);
        }
    }

    @Override
    public void getStateCompinePitchTempo(StateCompine callback, boolean isRefresh) {
        int s = SharedPrefs.getInstance().get(COMPINE, Integer.class, STATE_OFF);
        if (s == STATE_ON) {
            callback.onTurnOnCompine(isRefresh);
        } else {
            callback.onTurnOffCompine(isRefresh);
        }
    }
}
