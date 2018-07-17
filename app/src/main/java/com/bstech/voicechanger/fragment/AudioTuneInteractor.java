package com.bstech.voicechanger.fragment;

import com.bstech.voicechanger.service.MusicService;


public interface AudioTuneInteractor {
    void onGetData(MusicService service, OnGetDataListener listener);

    void onInputValue(String value, OnInputValueListener listener, int type);

    /* Get state compine pitch and tempo and show in UI */
    void getStateCompinePitchTempo(StateCompine callback,boolean isRefresh);

    interface OnGetDataListener {
        void onGetDataSuccess();

        void onGetDataFail();
    }

    interface OnInputValueListener {
        void onInputSuccess(String value, int type);

        void onInputFail();
    }

    interface StateCompine {
        void onTurnOffCompine(boolean isRefresh);

        void onTurnOnCompine(boolean isRefresh);
    }
}
