package com.englishtown.android.asr.core;

/**
 * Created by pengjianqing on 10/10/15.
 */
public class ASRCommandStartPlayback extends ASRCommand {
    String audioFile;

    public ASRCommandStartPlayback(String audioFile) {
        super(COMMAND_STARTPLAYBACK);
        this.audioFile = audioFile;
    }
}
