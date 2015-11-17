package com.englishtown.android.asr.task.audiorecorder;

public interface RecorderAndPlaybackListener {
   void onRecordingComplete(String fileName);
   void onPlaybackComplete();
}
