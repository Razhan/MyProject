package com.englishtown.android.asr.core;

/**
 * Created by pengjianqing on 10/9/15.
 */
public interface ASRListener {
    void onRecordComplete(String o);

    void onSuccess(String o);

    void onError();

    void onPlaybackComplete();
}
