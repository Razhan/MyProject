package com.englishtown.android.asr.core;

import java.io.IOException;
import java.util.List;

/**
 * The core ASR Engine, to simply the usage.
 * <code>
 * ASRConfig asrConfig = new ASRConfig(hmmPath, true, base);
 * ASREngine asrEngine = ASREngineController.getInstance(getApplication()).setConfig(asrConfig);
 * </code>
 * Created by pengjianqing on 10/10/15.
 */
public interface ASREngine {
    ASREngine setConfig(ASRConfig asrConfig);

    ASREngine installEngine(String hub4wsj_sc_8kzipAssert) throws IOException;

    ASREngine initAsrEngine(String path);

    ASREngine setListener(ASRListener listener);

    ASREngine setContext(List<String> sentences);

    void startRecording(List<AsrCorrectItem> correctList, int redorderMode);

    void stopRecording();

    void startRecognize();

    void startPlayback(String audioFile);

    void stopPlayBack();

    void destroy();

    String getRecorderAudioFilePath();

    int getDuration();

    int getAmplitude();
}
