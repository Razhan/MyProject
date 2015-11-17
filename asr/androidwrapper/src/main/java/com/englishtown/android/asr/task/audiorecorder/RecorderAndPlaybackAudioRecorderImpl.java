
package com.englishtown.android.asr.task.audiorecorder;

import android.app.Service;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.englishtown.android.asr.core.ASRConfig;
import com.englishtown.android.asr.utils.Logger;
import com.englishtown.android.asr.task.RecognizerTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

public class RecorderAndPlaybackAudioRecorderImpl implements RecorderAndPlaybackInterface {
    private static final String TAG = RecorderAndPlaybackAudioRecorderImpl.class.getSimpleName();

    protected static final int RECORDER_BPP = 16;

    protected static final String AUDIO_RECORDER_FOLDER = "EFAudioRecorder";

    protected static final String AUDIO_RECORDER_TEMP_FILE = "record";

    protected static final int MAX_DURATION_MSEC = 10 * 60 * 1000;

    // private AudioRecord recorder = null;
    private int bufferSize = 0;

    // private boolean isRecording = false;

    protected Context context;

    // private AudioRecord audioRecord;

    protected File audioFile;

    protected int playerStatus = PlayerConstants.STATUS_INVALID;

    protected RecorderAndPlaybackListener recorderAndPlaybackListener;

    // protected boolean useASRRecorder = true;

    // protected boolean useAudioTrack = true;

    private AudioTrack audioTrack = null;

    protected int recorderMode = ASRConfig.RECORDER_MODE_JUST_RECORDER;
    private String baseCacheDir;

    public RecorderAndPlaybackAudioRecorderImpl(Context context,
                                                RecorderAndPlaybackListener recorderAndPlaybackListener, String baseCacheDir) {
        this.context = context;
        this.recorderAndPlaybackListener = recorderAndPlaybackListener;
        this.baseCacheDir = baseCacheDir;
        audioFile = createAudioTmpFiles();

        bufferSize = AudioRecord.getMinBufferSize(RecognizerTask.RECORDER_SAMPLERATE,
                RecognizerTask.RECORDER_CHANNELS, RecognizerTask.RECORDER_AUDIO_ENCODING);

        updateRecorderMode();
    }

    private void checkTheTmpFile() {
        audioFile = createAudioTmpFiles();
    }

    @Override
    public void setRecorderMode(int recorderMode) {
        this.recorderMode = recorderMode;

        updateRecorderMode();
    }

    private void updateRecorderMode() {
        if (ASRConfig.RECORDER_MODE_JUST_ASR == this.recorderMode) {
            // do nothing,won't call save function.

        } else if (ASRConfig.RECORDER_MODE_JUST_RECORDER == this.recorderMode) {
            // useASRRecorder = false;
        } else if (ASRConfig.RECORDER_MODE_RECORDER_ASR == this.recorderMode) {
            // useASRRecorder = true;
        }
    }

    @Override
    public boolean startRecording() {
        log("startRecording");
        checkTheTmpFile();

        if (bufferSize < 8192) {
            bufferSize = 8192;
        }

        fileOutputStream = getFileOutputStream(audioFile);

        return true;
    }

    @Override
    public File getAudioTmpFiles() {
        if (null == audioFile) {
            audioFile = createAudioTmpFiles();
        }

        return audioFile;
    }

    private File createAudioTmpFiles() {
        String path = getAudioTmpFilesPath();

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }

        return file;
    }

    @Override
    public String getAudioTmpFilesPath() {
        String filepath = baseCacheDir;

        File dir = new File(filepath, AUDIO_RECORDER_FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.getAbsolutePath() + File.separator + AUDIO_RECORDER_TEMP_FILE;
    }

    @Override
    public boolean stopRecording() {
        log("stopRecording");

        //removeAudioFocus();

        if (null != fileOutputStream) {
            try {
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            fileOutputStream = null;
        }

        recordingComplete(getAudioTmpFilesPath());

        return true;
    }

    @Override
    public boolean isPlaying() {
        return PlayerConstants.STATUS_STARTED == playerStatus;
    }

    private void startPlaybackWithAudioTrack(String fileNameRaw) {

        if (fileNameRaw.startsWith("file://")) {
            fileNameRaw = fileNameRaw.replace("file://", "");
        }

        final String fileName = fileNameRaw;

        final int bufferSizeOut = AudioTrack.getMinBufferSize(RecognizerTask.RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        try {

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    RecognizerTask.RECORDER_SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSizeOut, AudioTrack.MODE_STREAM);
            audioTrack.setPlaybackRate(RecognizerTask.RECORDER_SAMPLERATE);

            audioTrack.play();

        } catch (Exception e) {
            e.printStackTrace();
            playbackComplete();
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                FileInputStream inputStream;
                try {
                    inputStream = new FileInputStream(fileName);
                    // byte[] buffer = new byte[bufferSizeOut];
                    int read = 0;
                    // while ((read = inputStream.read(buffer)) > 0) {
                    // audioTrack.write(buffer, 0, bufferSizeOut);
                    // }

                    ByteBuffer myByteBuffer = ByteBuffer.allocate(bufferSizeOut * 2);
                    myByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    FileChannel in = inputStream.getChannel();

                    short[] shortBuffer = new short[bufferSizeOut];
                    while ((read = in.read(myByteBuffer)) > 0) {
                        myByteBuffer.flip();
                        ShortBuffer myShortBuffer = myByteBuffer.asShortBuffer();
                        log("read length=" + read + ",bufferSizeOut=" + bufferSizeOut);
                        try {
                            myShortBuffer.get(shortBuffer, 0, read / 2);
                            audioTrack.write(shortBuffer, 0, read / 2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                playbackComplete();
            }

        }).start();
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

        startPlaybackWithAudioTrack(fileName);

        return true;
    }

    @Override
    public boolean stopPlayback() {
        log("stopPlayback");

        if (null != audioTrack) {
            log("stopPlayback 3");
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }

        return true;
    }

    @Override
    public boolean pausePlayback() {
        log("pausePlayback");

        return true;
    }

    @Override
    public boolean resumePlayback() {
        log("resumePlayback");

        return true;
    }

    @Override
    public void recordingComplete(String filePath) {
        log("recordingComplete");

        if (null != recorderAndPlaybackListener) {
            recorderAndPlaybackListener.onRecordingComplete(filePath);
        }
    }

    @Override
    public void playbackComplete() {
        playerStatus = PlayerConstants.STATUS_PLACKBACK_COMPLETED;
        log("playbackComplete");

        if (null != recorderAndPlaybackListener) {
            recorderAndPlaybackListener.onPlaybackComplete();
        }
    }

    protected void log(String msg) {
        Logger.i(TAG, msg);
    }

    @Override
    public void release() {
    }

    /**
     * Interface definition for a callback to be invoked when the audio focus of
     * the system is updated.
     */
    public interface OnAudioFocusChangeListener {
        /**
         * Called on the listener to notify it the audio focus for this listener
         * has been changed. The focusChange value indicates whether the focus
         * was gained, whether the focus was lost, and whether that loss is
         * transient, or whether the new focus holder will hold it for an
         * unknown amount of time. When losing focus, listeners can use the
         * focus change information to decide what behavior to adopt when losing
         * focus. A music player could for instance elect to lower the volume of
         * its music stream (duck) for transient focus losses, and pause
         * otherwise.
         *
         * @param focusChange the type of focus change, one of
         *                    {@link AudioManager#AUDIOFOCUS_GAIN},
         *                    {@link AudioManager#AUDIOFOCUS_LOSS},
         *                    {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT} and
         *                    {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}.
         */
        public void onAudioFocusChange(int focusChange);
    }

    OnAudioFocusChangeListener mOnAudioFocusChangeListener = new OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    // resume playback
                    log("AUDIOFOCUS_GAIN,startPlayer()");
                    // startPlayer();

                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    // Lost focus for an unbounded amount of time: stop playback
                    // and release media player
                    // stopPlayer();
                    log("AUDIOFOCUS_LOSS,pausePlayer()");
                    // pausePlayer();
                    pausePlayback();

                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // Lost focus for a short time, but we have to stop
                    // playback. We don't release the media player because
                    // playback
                    log("AUDIOFOCUS_LOSS_TRANSIENT,pausePlayer()");
                    pausePlayback();

                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Lost focus for a short time, but it's ok to keep playing
                    // at an attenuated level
                    log("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    pausePlayback();

                    break;
            }
        }
    };

    private FileOutputStream fileOutputStream;

    private FileChannel fileChannel;

    private FileOutputStream getFileOutputStream(File audioFile) {
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(audioFile);
            fileChannel = outputStream.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return outputStream;
    }

    @Override
    public void saveAudioRecorderFile(short[] buf, int length) {
        Logger.v(TAG, "saveAudioRecorderFile,length:" + length);

        FileOutputStream outputStream = fileOutputStream;

        if (null == outputStream) {
            return;
        }

        if (length <= 0) {
            try {
                outputStream.close();
                fileChannel.close();
                fileChannel = null;
                outputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //
        // ByteBuffer byteBuf = ByteBuffer.allocate(2*length);
        //
        // int i = 0;
        // while (length >= i) {
        // byteBuf.putShort(buf[i]);
        // i++;
        // }

        try {
            ByteBuffer myByteBuffer = ByteBuffer.allocate(length * 2);
            myByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            ShortBuffer myShortBuffer = myByteBuffer.asShortBuffer();
            myShortBuffer.put(buf);
            fileChannel.write(myByteBuffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
