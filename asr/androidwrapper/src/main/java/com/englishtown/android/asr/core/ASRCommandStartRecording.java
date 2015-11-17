package com.englishtown.android.asr.core;


import java.util.List;

/**
 * Created by pengjianqing on 10/10/15.
 */
public class ASRCommandStartRecording extends ASRCommand {
    List<AsrCorrectItem> correctList;
    int redorderMode = ASRConfig.RECORDER_MODE_JUST_ASR;

    public ASRCommandStartRecording(List<AsrCorrectItem> correctList, int redorderMode) {
        super(COMMAND_STARTRECORDING);

        this.correctList = correctList;
        this.redorderMode = redorderMode;
    }
}
