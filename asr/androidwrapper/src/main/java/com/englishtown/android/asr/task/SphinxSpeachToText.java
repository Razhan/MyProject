package com.englishtown.android.asr.task;

import android.os.Bundle;

import com.englishtown.android.asr.core.ASRConfig;
import com.englishtown.android.asr.task.audiorecorder.RecorderAndPlaybackInterface;
import com.englishtown.android.asr.utils.Logger;


public class SphinxSpeachToText implements SpeechToTextService, RecognitionListener {

    static {
        System.loadLibrary("pocketsphinx");
    }

    private static final String TAG = SphinxSpeachToText.class.getSimpleName();

    private final RecognizerTask rec;
    private final Thread recThread;
    private Callback callback;
    private boolean init;
    private int duration = -1;
    private long startTime;

    private boolean stopped;

    public SphinxSpeachToText(ASRConfig asrConfig) {
        rec = new RecognizerTask(asrConfig);
        recThread = new Thread(rec, "RecognizerThread");
        rec.setRecognitionListener(this);
        stopped = true;
    }

    public SphinxSpeachToText(ASRConfig asrConfig, String dictPath, String lmPath) {
        rec = new RecognizerTask(asrConfig, dictPath, lmPath);
        recThread = new Thread(rec);
        rec.setRecognitionListener(this);
        stopped = true;
    }

    public void setRecorderSaveInterface(int recorderMode,
                                         RecorderAndPlaybackInterface recorderAndPlaybackInterface) {
        rec.setRecorderSaveInterface(recorderMode, recorderAndPlaybackInterface);
    }

    public void init() {
        init = true;
        recThread.start();
    }

    public void destroy() {
        if (!rec.isDone()) {
            rec.shutdown();
        }
    }

    @Override
    public synchronized void startListening(Callback callback) {
        //FIXME need check the initAsrEngine state.
        //checkState(initAsrEngine);

        this.callback = callback;
        stopped = false;
        rec.start();
        startTime = System.currentTimeMillis();
    }

    @Override
    public synchronized void stopListening() {
        rec.stop();
        duration = (int)(System.currentTimeMillis() - startTime);
        stopped = true;
    }

    @Override
    public synchronized void onPartialResults(Bundle b) {

    }

    @Override
    public synchronized void onResults(Bundle b) {
        String sphinxResult = b.getString("hyp");
        if (callback != null) {
            callback.onSttResult(sphinxResult);
        }
        callback = null;
    }

    @Override
    public synchronized void onError(int err) {
        Logger.e(TAG, "Sphinx error with code" + err);
        if (callback != null) {
            callback.onSttResult(null);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (!rec.isDone()) {
            rec.shutdown();
        }
        super.finalize();
    }

    @Override
    public synchronized boolean isListening() {
        return !stopped;
    }

    @Override
    public void setModelPath(String dictionaryPath, String languageModelPath) {
        if (rec != null) {
            rec.setModelPath(dictionaryPath, languageModelPath);
        } else {
            Logger.e(TAG, "rec is null in setModelPath()");
        }
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplitude() {
        return rec.getAmplitude();
    }
}
