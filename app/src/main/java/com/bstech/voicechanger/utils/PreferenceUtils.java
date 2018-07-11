package com.bstech.voicechanger.utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;

import com.bstech.voicechanger.BuildConfig;


public class PreferenceUtils {
    public static final String CATEGORY_MUSIC = "music";
    public static final String KEY_ALBUM_SIZE = "music_albumart_size";
    public static final String KEY_BG_ALPHA = "music_background_alpha";
    public static final String KEY_BL_ALWAYSON = "music_backlight_alwayson";
    public static final String KEY_IGNORE_INITPOPUP = "ignore_popup_for_init";
    public static final String KEY_IGNORE_REMOTEAPP_POPUP = "ignore_popup_for_remoteapp";
    public static final String KEY_IGNORE_VIPOPUP = "ignore_popup_for_minivi";
    public static final String KEY_MIC_SENSITIVITY = "deprecated";
    public static final String KEY_MIC_USE = "deprecated";
    public static final String KEY_NOTIFICATION_HELP = "music_notification_help_confirm";
    public static final String KEY_NOTI_HIDE = "music_notification_hide";
    public static final String KEY_RC_AUTHORIZED = "deprecated";
    public static final String KEY_RC_PACKAGENAME = "deprecated";
    public static final String KEY_SEEK_SHOW = "music_seek_show";
    public static final String KEY_VISUALIZER_RATIO = "music_visualizer_ratio";
    public static final String KEY_VI_ALPHA = "music_visualizer_alpha";
    public static final String KEY_VI_BARRATIO = "music_visualizer_barratio";
    public static final String KEY_VI_BOTTOMSET = "music_visualizer_bottomset";
    public static final String KEY_VI_CLEANMODE = "music_visualizer_cleanmode";
    public static final String KEY_VI_COLOR = "music_visualizer_color";
    public static final String KEY_VI_COLORSET = "music_visualizer_colorset";
    public static final String KEY_VI_COLORSET_NUM = "music_visualizer_colorset_num";
    public static final String KEY_VI_FLOATING_ONOFF = "music_visualizer_floating_onoff";
    public static final String KEY_VI_GRAVITY = "music_visualizer_gravity";
    public static final String KEY_VI_HEIGHT_RATIO = "music_visualizer_height";
    public static final String KEY_VI_SHOWSCREEN = "music_visualizer_showonthescreen";
    public static final String KEY_VI_STICK = "music_visualizer_stick";
    public static final String KEY_VI_WIDTH_RATIO = "music_visualizer_width";

    public static int loadIntegerValue(Context context, String key) {
        return context.getSharedPreferences(CATEGORY_MUSIC, 0).getInt(key, 0);
    }

    public static int loadIntegerValue(Context context, String key, int defValue) {
        return context.getSharedPreferences(CATEGORY_MUSIC, 0).getInt(key, defValue);
    }

    public static void saveIntegerValue(Context context, String key, int value) {
        Editor editor = context.getSharedPreferences(CATEGORY_MUSIC, 0).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static float loadFloatValue(Context context, String key, float defValue) {
        return context.getSharedPreferences(CATEGORY_MUSIC, 0).getFloat(key, defValue);
    }

    public static void saveFloatValue(Context context, String key, float value) {
        Editor editor = context.getSharedPreferences(CATEGORY_MUSIC, 0).edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static boolean loadBooleanValue(Context context, String key, boolean defValue) {
        return context.getSharedPreferences(CATEGORY_MUSIC, 0).getBoolean(key, defValue);
    }

    public static void saveBooleanValue(Context context, String key, boolean value) {
        Editor editor = context.getSharedPreferences(CATEGORY_MUSIC, 0).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void saveStringValue(Context context, String key, String value) {
        Editor editor = context.getSharedPreferences(CATEGORY_MUSIC, 0).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String loadStringValue(Context context, String key) {
        return context.getSharedPreferences(CATEGORY_MUSIC, 0).getString(key, BuildConfig.FLAVOR);
    }
}