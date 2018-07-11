package com.bstech.voicechanger.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.bstech.voicechanger.service.MusicService;
import com.bstech.voicechanger.utils.SharedPrefs;
import com.bstech.voicechanger.utils.Utils;

/**
 * Created by Giga on 7/5/2018.
 */

public class MyApplication extends Application {
    private static MyApplication mySelf;
    private MusicService service;

    private boolean isConnected = false;
    private ServiceConnection serviceConnection;

    public static MyApplication self() {
        return mySelf;
    }

    public static Context getAppContext() {
        return mySelf;
    }

    public static String getUriTree() {
        //return PreferenceManager.getDefaultSharedPreferences(mySelf).getString(Keys.URI_TREE, null);
        return SharedPrefs.getInstance().get(Utils.TREE_URI, String.class, null);
    }

    public void onCreate() {
        super.onCreate();
        mySelf = this;
        connectServiceAndPlay();
    }

    private void connectServiceAndPlay() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder s) {
                MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) s;
                service = musicBinder.getService();
                isConnected = true;

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isConnected = false;
                service = null;

            }
        };
        Intent it = new Intent(this, MusicService.class);
        bindService(it, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public MusicService getService() {
        return service;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (service != null && service.mPlayer != null && !service.isPlaying()) {
            service.stopForeground(true);
        }
        unbindService(serviceConnection);
    }
}
