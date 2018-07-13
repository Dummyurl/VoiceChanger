package com.bstech.voicechanger.utils;

import android.util.Log;

import com.bstech.voicechanger.BuildConfig;


/**
 * Created by Adm on 9/7/2016.
 */
public final class Flog {
    private final static String TAG = "VoiceChanger";
    private final static boolean IS_DEBUG = BuildConfig.DEBUG;

    public static void d(String msg) {
        if (IS_DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (IS_DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void e(String msg) {
        if (IS_DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (IS_DEBUG) {
            Log.e(tag, msg);
        }
    }
}
