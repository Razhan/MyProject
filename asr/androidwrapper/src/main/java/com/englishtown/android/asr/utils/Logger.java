package com.englishtown.android.asr.utils;

import android.util.Log;

/**
 * Created by pengjianqing on 10/9/15.
 */
public class Logger {
    private static boolean isDebug = false;

    public static final void i(String TAG, String msg) {
        if (isDebug)
            Log.i(TAG, msg);
    }

    public static void d(String tag, String s) {
        if (isDebug)
            Log.d(tag, s);
    }

    public static void e(String tag, String s) {
        if (isDebug)
            Log.e(tag, s);
    }

    public static void v(String tag, String s) {
        if (isDebug)
            Log.v(tag, s);
    }

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }
}
