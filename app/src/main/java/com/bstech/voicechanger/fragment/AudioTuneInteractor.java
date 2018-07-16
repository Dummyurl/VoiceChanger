package com.bstech.voicechanger.fragment;

import com.bstech.voicechanger.service.MusicService;


public interface AudioTuneInteractor {
    void onGetData(MusicService service, OnGetDataListener listener);

    void onInputValue(String value, OnInputValueListener listener, int type);

    interface OnGetDataListener {
        void onGetDataSuccess();

        void onGetDataFail();
    }

    interface OnInputValueListener {
        void onInputSuccess(String value, int type);

        void onInputFail();
    }
}
