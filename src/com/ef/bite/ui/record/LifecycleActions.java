package com.ef.bite.ui.record;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;

import com.englishtown.android.asr.core.ASREngine;
import com.englishtown.android.asr.utils.Logger;

import java.io.File;
import java.io.IOException;


public class LifecycleActions {
    private static final String TAG = LifecycleActions.class.getSimpleName();

    private static final String APP_VERSION_KEY = "app_version_key";


    /**
     * Call this method to perform pre actions required by the app to work
     * properly. E.g, read back global state from disk.
     *
     * @param asrEngine
     * @param context
     */
    public static synchronized void performStartActions(Context context, ASREngine asrEngine, String baseCacheDir) {

        Logger.d(TAG, "performStartActions");


        final SharedPreferences sp = context.getSharedPreferences(
                "installationSp", Context.MODE_PRIVATE);

        int applicationVersionCode = getApplicationVersionCode(context);
        int storedApplicationVersionCode = sp.getInt(APP_VERSION_KEY, 0);

        if (applicationVersionCode != storedApplicationVersionCode) {
            updateOrFirstTimeInstall(context, sp, applicationVersionCode, storedApplicationVersionCode, asrEngine, baseCacheDir);
            Logger.d(TAG, "App will update from version " + storedApplicationVersionCode + " to " + applicationVersionCode);
            AppPreference.getInstance(context).setAsrPreInited(true);
        }
    }

    private static void updateOrFirstTimeInstall(Context context, final SharedPreferences sp,
                                                 int applicationVersionCode, int storedApplicationVersionCode, ASREngine asrEngine, String baseCacheDir) {
        try {

            File offlinePackDir = new File(baseCacheDir);
            offlinePackDir.mkdirs();
            new File(offlinePackDir, ".nomedia").mkdir();

            String zipFile = "hub4wsj_sc_8k.zip";
            asrEngine.installEngine(zipFile);

            sp.edit().putInt(APP_VERSION_KEY, applicationVersionCode).commit();
        } catch (IOException e) {
            throw new RuntimeException("Cannot start app", e);
        }
    }


    private static int s_appVersionCode = Integer.MAX_VALUE;

    public static int getApplicationVersionCode(Context context) {
        if (s_appVersionCode != Integer.MAX_VALUE)
            return s_appVersionCode;
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            s_appVersionCode = pinfo.versionCode;
            return s_appVersionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return Integer.MAX_VALUE;
        }
    }
}
