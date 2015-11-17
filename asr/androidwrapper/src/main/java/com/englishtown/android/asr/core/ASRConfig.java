package com.englishtown.android.asr.core;

import java.io.File;

/**
 * Created by pengjianqing on 10/9/15.
 */
public class ASRConfig {
    public static final int RECORDER_MODE_JUST_RECORDER = 1;
    public static final int RECORDER_MODE_JUST_ASR = RECORDER_MODE_JUST_RECORDER + 1;
    public static final int RECORDER_MODE_RECORDER_ASR = RECORDER_MODE_JUST_ASR + 1;

    static public final String CFG_GLOBAL_DICT_NAME = "cmu07a.ef.dic";
    public String POCKETSPHINX_CFG_HMMDIR_HUB4;
    public String POCKETSPHINX_CFG_DEFAULT_DICT;
    public String POCKETSPHINX_CFG_DEFAULT_LM;

    private String asrHMMDir;
    private boolean isDebug;
    private String baseCacheDir;

    public ASRConfig(String asrHMMDir, boolean isDebug, String baseCacheDir) {
        this.asrHMMDir = asrHMMDir;
        this.isDebug = isDebug;
        this.baseCacheDir = baseCacheDir;

        POCKETSPHINX_CFG_HMMDIR_HUB4 = asrHMMDir + File.separator + "hub4wsj_sc_8k";
        POCKETSPHINX_CFG_DEFAULT_DICT = POCKETSPHINX_CFG_HMMDIR_HUB4 + File.separator + CFG_GLOBAL_DICT_NAME;
        POCKETSPHINX_CFG_DEFAULT_LM = POCKETSPHINX_CFG_HMMDIR_HUB4 + File.separator + "default.jsgf";
    }

    public String getAsrHMMDir() {
        return asrHMMDir;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public String getBaseCacheDir() {
        return baseCacheDir;
    }


}
