package com.bstech.voicechanger.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

/**
 * Created by Giga on 7/5/2018.
 */

public class PermissionUtil {
    public static boolean isGrandPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == 0) {
            return true;
        }
        return false;
    }
}
