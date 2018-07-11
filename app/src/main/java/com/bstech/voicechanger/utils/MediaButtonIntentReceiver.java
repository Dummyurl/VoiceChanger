package com.bstech.voicechanger.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;


public class MediaButtonIntentReceiver extends BroadcastReceiver {

    static int d = 0;
    static int nClick = 0;

    public MediaButtonIntentReceiver() {
        super();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            return;
        }

        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        // ...do something with keyEvent, super... does nothing.
        if (keyEvent == null) {
            return;
        }

        int action = keyEvent.getAction();
        final Handler handler = new Handler();
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
                if (action == KeyEvent.ACTION_DOWN) {

                    d++;
                    nClick = nClick + 1;

                    Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            // single click *******************************
                            if (nClick == 1) {
                                //Flog.d("MediaSessionCompat", "single click");
//                                if (PlaybackHelper.SONG_PAUSE) {
//                                    PlaybackController.play(context);
//                                } else {
//                                    PlaybackController.pause(context);
//                                }
                            }
                            // double click *********************************
                            if (nClick >= 2) {
                                //Flog.d("MediaSessionCompat", "double click");
//                                PlaybackController.next(context);
                            }
                            d = 0;
                            nClick = 0;
                        }
                    };
                    if (d == 1) {
                        //Flog.d("MediaSessionCompat", "getKeyCode: " + keyEvent.getKeyCode());
                        handler.postDelayed(r, 500);
                    }
                }
                break;
        }

        abortBroadcast();
    }
}
