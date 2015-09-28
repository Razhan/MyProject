package com.ef.bite.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.ef.bite.ui.chunk.ChunkLearnActivity;
import com.ef.bite.utils.TimeFormatUtil;

import java.util.List;

/**
 * Created by ran on 9/25/15.
 */
public class AudioPlayerViewEx extends AudioPlayerView{

    private List<Integer> timeStamps;
    private int indexOfTimeStamps = 0;
    private int lastEndTime;
    private ChunkLearnActivity.AdudioCallBack mCallBack;

    public AudioPlayerViewEx(Context context) {
        super(context);
        this.mContext = context;
        initialize();
    }

    public AudioPlayerViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initialize();
    }

    public void init(List<Integer> timestamps, ChunkLearnActivity.AdudioCallBack callback) {
        //延迟1ms
        timestamps.set(0, timestamps.get(0) + 1);
        timestamps.remove(timestamps.size() - 1);

        this.lastEndTime = timestamps.get(timestamps.size() - 1);
        this.mCallBack = callback;
        this.timeStamps = timestamps;
    }

    @Override
    protected void handleProcess() {
        super.handleProcess();
        triggerDialogueItem((int) mPlayer.getCurrentPosition());
    }

    private void triggerDialogueItem(final int pos) {

        if (indexOfTimeStamps < timeStamps.size()) {
            if (pos >= timeStamps.get(indexOfTimeStamps)) {
                String temp = "Time Stamp = " + String.valueOf(timeStamps.get(indexOfTimeStamps)) + "   " +
                              "Real position = " + String.valueOf(pos);
                mCallBack.postExec(temp, indexOfTimeStamps);
                indexOfTimeStamps++;
            }
        }
    }

    @Override
    protected void reload() {
        super.reload();

        mCallBack.postExec("", -1);
        indexOfTimeStamps = 0;
    }
}
