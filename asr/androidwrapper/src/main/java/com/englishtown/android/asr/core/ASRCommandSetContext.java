package com.englishtown.android.asr.core;

import java.util.List;

/**
 * Created by pengjianqing on 10/10/15.
 */
public class ASRCommandSetContext extends ASRCommand {
    List<String> sentences;

    public ASRCommandSetContext(List<String> sentences) {
        super(COMMAND_SETCONTEXT);
        this.sentences = sentences;
    }
}
