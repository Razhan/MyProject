package com.englishtown.android.asr.task.audiorecorder;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.englishtown.android.asr.utils.Logger;


public class SimpleAudioPlayer implements OnErrorListener, OnPreparedListener {
	
	private static final String TAG = SimpleAudioPlayer.class.getName();
	private MediaPlayer mediaPlayer;
	
	private volatile boolean prepared;
	private Object prepareMonitor = new Object();

	public SimpleAudioPlayer(Context context) {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnPreparedListener(this);
	}
	
	public boolean setAudioSrc(String src) {
		try {
//			if (!src.startsWith("file://")) {
//				src = "file://" + src;
//	        }
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			mediaPlayer.reset();
			mediaPlayer.setDataSource(src);
			mediaPlayer.prepare();
			return true;
		} catch (Exception e) {
			Logger.e(TAG, "Failed to set src on media player. Src is " + src);
			return false;
		}
	}
	
	public void play() {
		
		mediaPlayer.start();
	}
	
	public void pause() {
		if(mediaPlayer.isPlaying()) {
			mediaPlayer.pause();			
		}
	}
	
	public void stop() {
		mediaPlayer.stop();
		mediaPlayer.reset();
	}
	
	
	
	public void onDestroy() {
		if(mediaPlayer.isPlaying()) {
			mediaPlayer.stop();			
		}
		mediaPlayer.release();
		mediaPlayer = null;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		synchronized (prepareMonitor) {
			prepared = true;
			prepareMonitor.notifyAll();			
		}
		
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
        String errorExtra = "Unknown Error";
        switch (extra){
            case MediaPlayer.MEDIA_ERROR_IO:
                errorExtra = "MEDIA_ERROR_IO";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                errorExtra = "MEDIA_ERROR_MALFORMED";
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                errorExtra = "MEDIA_ERROR_UNSUPPORTED";
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                errorExtra = "MEDIA_ERROR_TIMED_OUT";
                break;
        }
        Logger.e(TAG, ">>>MediaPlayer error msg type:" + what + " | extra msg: " + errorExtra);
		return false;
	}

}
