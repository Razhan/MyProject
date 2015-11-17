package com.englishtown.android.asr.core;


/**
 * Created by pengjianqing on 10/9/15.
 */
public class ASRCommand {
    public static final String COMMAND_SETCONTEXT = "setContext";
    public static final String COMMAND_STARTRECORDING = "startRecording";
    public static final String COMMAND_STOPRECORDING = "stopRecording";
    public static final String COMMAND_STARTRECOGNIZE = "startRecognize";
    public static final String COMMAND_STARTPLAYBACK = "startPlayback";
    public static final String COMMAND_STOPPLAYBACK = "stopPlayback";

    String command;

    public ASRCommand(String command) {
        this.command = command;
    }
}
