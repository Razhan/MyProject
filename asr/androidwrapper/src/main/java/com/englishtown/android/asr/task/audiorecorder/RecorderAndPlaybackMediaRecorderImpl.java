
package com.englishtown.android.asr.task.audiorecorder;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.net.Uri;
import android.text.TextUtils;

import com.englishtown.android.asr.core.ASRConfig;
import com.englishtown.android.asr.utils.Logger;

import java.io.IOException;

public class RecorderAndPlaybackMediaRecorderImpl extends RecorderAndPlaybackAudioRecorderImpl {

    private MediaRecorder mediaRecorder;

    private MediaPlayer mediaPlayer;

    public RecorderAndPlaybackMediaRecorderImpl(Context context,
            RecorderAndPlaybackListener recorderAndPlaybackListener, String baseCacheDir) {
        super(context, recorderAndPlaybackListener, baseCacheDir);
    }

    @Override
    public boolean startRecording() {
        log("startRecording");

        if (null != mediaRecorder) {
            stopRecording();
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setMaxDuration(MAX_DURATION_MSEC);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setOutputFile(getAudioTmpFilesPath());
        mediaRecorder.setOnErrorListener(new OnErrorListener() {

            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                log("onError: what=" + what + ",extra=" + extra);
            }
        });

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {

            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                log("onInfo: what=" + what + ",extra=" + extra);

                switch (what) {
                    case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                        stopRecording();
                        break;
                    default:
                }

            }
        });
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean stopRecording() {
        log("stopRecording");

        if (null != mediaRecorder) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        recordingComplete(audioFile.getPath());

        return true;
    }

    @Override
    public void release() {
        super.release();

        if (null != mediaRecorder) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        releaseMediaPlayer();
    }

    @Override
    public boolean startPlayback(String fileName) {

        if (TextUtils.isEmpty(fileName) || fileName.equals("null")) {
            fileName = getAudioTmpFilesPath();
        }

        if (!fileName.startsWith("file://")) {
            fileName = "file://" + fileName;
        }

        log("startPlayback, fileName:" + fileName);

        if (recorderMode == ASRConfig.RECORDER_MODE_JUST_ASR) {
            playbackComplete();
            releaseMediaPlayer();

            return true;
        }

        mediaPlayer = createMediaPlayer();

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                log("onError,what=" + what + ",extra=" + extra);
                playerStatus = PlayerConstants.STATUS_ERROR;
                return false;
            }
        });

        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                playerStatus = PlayerConstants.STATUS_PREPARED;
                mediaPlayer.start();
            }
        });

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                playbackComplete();
                releaseMediaPlayer();
            }
        });

        try {
            String path = "file://" + getAudioTmpFilesPath();
            path = fileName;
            log("startPlayback,path=" + path);
            mediaPlayer.setDataSource(context, Uri.parse(path));
            playerStatus = PlayerConstants.STATUS_INITIALIZED;
            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private MediaPlayer createMediaPlayer() {
        log("createMediaPlayer");
        MediaPlayer player = null;
        if (null != mediaPlayer) {
            releaseMediaPlayer();
            player = new MediaPlayer();
            player.setScreenOnWhilePlaying(true);

        } else {
            player = new MediaPlayer();
            player.setScreenOnWhilePlaying(true);
        }

        return player;
    }

    private void releaseMediaPlayer() {
        log("releaseMediaPlayer");
        if (null != mediaPlayer) {
            mediaPlayer.release();
            mediaPlayer = null;
            playerStatus = PlayerConstants.STATUS_INVALID;
        }
    }

    @Override
    public boolean resumePlayback() {
        log("resumePlayback");
        if (null != mediaPlayer) {
            mediaPlayer.start();
            playerStatus = PlayerConstants.STATUS_STARTED;
        }

        return true;
    }

    @Override
    public boolean stopPlayback() {
        log("stopPlayback");

        if (null != mediaPlayer) {
            mediaPlayer.stop();
            playerStatus = PlayerConstants.STATUS_STOPPED;
            playbackComplete();
        }

        return true;
    }

    protected void log(String msg) {
        Logger.i(RecorderAndPlaybackMediaRecorderImpl.class.getSimpleName(), msg);
    }
}
