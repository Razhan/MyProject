package com.englishtown.android.asr.core;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.ef.efekta.asr.JSGFgen.GenerateJSGF;
import com.ef.efekta.asr.JSGFgen.GroupGenerator;
import com.ef.efekta.asr.JSGFgen.NeighborGenerator;
import com.ef.efekta.asr.JSGFgen.WildcardGenerator;
import com.ef.efekta.asr.textnormalizer.TextNormalizer;
import com.ef.efekta.asr.textnormalizer.WordFinder;
import com.englishtown.android.asr.utils.Logger;
import com.englishtown.android.asr.task.SpeechToTextService;
import com.englishtown.android.asr.task.SphinxSpeachToText;
import com.englishtown.android.asr.utils.Zip;
import com.englishtown.android.asr.task.audiorecorder.RecorderAndPlaybackAudioRecorderImpl;
import com.englishtown.android.asr.task.audiorecorder.RecorderAndPlaybackInterface;
import com.englishtown.android.asr.task.audiorecorder.RecorderAndPlaybackListener;
import com.englishtown.android.asr.task.audiorecorder.RecorderAndPlaybackMediaRecorderImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ASREngineController implements SpeechToTextService.Callback, ASREngine {
    private static final String TAG = "ASREngineController";

    public static final int MSG_ASR_STT_RESULT = 0;
    public static final int MSG_ASR_STT_RESULT_ERR = 1;
    public static final int MSG_ASR_REC_COMPLETE = 2;
    public static final int MSG_ASR_PB_COMPLETE = 3;


    private SphinxSpeachToText sphinxStt = null;
    private TextNormalizer normalizer = new TextNormalizer(WordFinder.getPattern());

    private Context appContext;
    private ASRMonitor asrMonitor = new ASRMonitor(sphinxStt);

    private ASRSessionData sessionData = null;

    private int previewRecorderMode = -1;
    private boolean isRecorderHandlerInited = false;
    private RecorderAndPlaybackInterface recorderAndPlaybackHandler;
    private int recorderMode = ASRConfig.RECORDER_MODE_RECORDER_ASR;

    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    List<AsrCorrectItem> correctList;
    String dicPath;
    String lmPath;

    ASRConfig asrConfig;
    ASRListener asrListener;

    private static ASREngineController instance = null;

    public static synchronized ASREngine getInstance(Context appContext) {
        if (instance == null) {
            instance = new ASREngineController(appContext);
        }

        return instance;
    }

    /**
     * @param appContext
     */
    private ASREngineController(Context appContext) {
        this.appContext = appContext;
    }

    @Override
    public ASREngine setConfig(ASRConfig asrConfig) {
        this.asrConfig = asrConfig;
        Logger.setDebug(this.asrConfig.isDebug());

        return instance;
    }

    @Override
    public ASREngine installEngine(String hub4wsj_sc_8kzipAssert) throws IOException {
        File file = new File(asrConfig.getAsrHMMDir());
        if (!file.exists()) {
            file.mkdirs();
        }

        Zip.unpackToDir(
                appContext.getAssets().open(hub4wsj_sc_8kzipAssert),
                asrConfig.getAsrHMMDir());

        return instance;
    }

    @Override
    public ASREngineController initAsrEngine() {
        sphinxStt = new SphinxSpeachToText(asrConfig);
        sphinxStt.init();
        asrMonitor = new ASRMonitor(sphinxStt);

        initRecorderHandlerIfNeed();

        return instance;
    }

    @Override
    public ASREngine setListener(ASRListener asrListener) {
        this.asrListener = asrListener;

        return instance;
    }

    @Override
    public ASREngine setContext(List<String> sentences) {
        ASRCommandSetContext asrCommand = new ASRCommandSetContext(sentences);
        handleASRCommand(asrCommand);

        return instance;
    }

    @Override
    public void startRecording(List<AsrCorrectItem> correctList, int redorderMode) {
        ASRCommandStartRecording asrCommand = new ASRCommandStartRecording(correctList, redorderMode);
        handleASRCommand(asrCommand);
    }

    @Override
    public void stopRecording() {
        ASRCommandStopRecording asrCommand = new ASRCommandStopRecording();
        handleASRCommand(asrCommand);
    }

    @Override
    public void startRecognize() {
        ASRCommandStartRecognize asrCommand = new ASRCommandStartRecognize();
        handleASRCommand(asrCommand);
    }

    @Override
    public void startPlayback(String audioFile) {
        ASRCommandStartPlayback asrCommand = new ASRCommandStartPlayback(audioFile);
        handleASRCommand(asrCommand);
    }

    @Override
    public void stopPlayBack() {
        ASRCommandStopPlayback asrCommand = new ASRCommandStopPlayback();
        handleASRCommand(asrCommand);
    }

    @Override
    public int getAmplitude() {
        return sphinxStt.getAmplitude();
    }


    @Override
    public void destroy() {
        stopSession();

        if (sphinxStt != null) {
            sphinxStt.destroy();
        }

        if (null != recorderAndPlaybackHandler) {
            recorderAndPlaybackHandler.release();
            isRecorderHandlerInited = false;
        }
    }

    @Override
    public String getRecorderAudioFilePath() {
        if (null != recorderAndPlaybackHandler){
            return recorderAndPlaybackHandler.getAudioTmpFilesPath();
        }

        return null;
    }

    @Override
    public int getDuration() {
        return sphinxStt.getDuration();
    }

    private void initRecorderHandlerIfNeed() {
        // if already inited and the same mode,don't need reinit again.
        if (isRecorderHandlerInited && recorderMode == previewRecorderMode) {
            return;
        }

        if (null != recorderAndPlaybackHandler) {
            recorderAndPlaybackHandler.release();
        }

        RecorderAndPlaybackListener listener = new RecorderAndPlaybackListener() {

            @Override
            public void onRecordingComplete(String fileName) {
                Logger.i(TAG, "onRecordingComplete,fileName=" + fileName);
                if (ASRConfig.RECORDER_MODE_RECORDER_ASR == recorderMode) {
                    handleASREvent(MSG_ASR_REC_COMPLETE, fileName);
                } else {
                    handleASREvent(MSG_ASR_REC_COMPLETE, fileName);
                }
            }

            @Override
            public void onPlaybackComplete() {
                Logger.i(TAG, "onPlaybackComplete");
                handleASREvent(MSG_ASR_PB_COMPLETE, null);
            }

        };

        if (recorderMode == ASRConfig.RECORDER_MODE_JUST_RECORDER) {
            recorderAndPlaybackHandler = new RecorderAndPlaybackMediaRecorderImpl(
                    appContext.getApplicationContext(), listener, asrConfig.getBaseCacheDir());
            if (null != sphinxStt) {
                sphinxStt.setRecorderSaveInterface(recorderMode, null);
            }
        } else if (recorderMode == ASRConfig.RECORDER_MODE_JUST_ASR) {
            if (null != sphinxStt) {
                sphinxStt.setRecorderSaveInterface(recorderMode, null);
            }
        } else if (recorderMode == ASRConfig.RECORDER_MODE_RECORDER_ASR) {
            recorderAndPlaybackHandler = new RecorderAndPlaybackAudioRecorderImpl(
                    appContext.getApplicationContext(), listener, asrConfig.getBaseCacheDir());
            if (null != sphinxStt) {
                sphinxStt.setRecorderSaveInterface(recorderMode,
                        recorderAndPlaybackHandler);
            }
        }

        updateRecorderMode();

        previewRecorderMode = recorderMode;
        isRecorderHandlerInited = true;
    }

    public void handleASRCommand(ASRCommand asrCommand) {
        Logger.i(TAG, "handleASRCommand: " + asrCommand.toString());

        if (sphinxStt == null)
            return;

        try {
            String action = asrCommand.command;

            if (action.compareTo("setContext") == 0) {
                if (sessionData != null) {
                    sessionData.statAsrSession();
                }
                sessionData = new ASRSessionData();
                ASRCommandSetContext commandSetContext = (ASRCommandSetContext) asrCommand;

                List<String> sentences = commandSetContext.sentences;
                prepareContext(sentences);

                resetAsr(0);

                sessionData.statAsrBasic();
            } else if (action.compareTo("startRecording") == 0) {
                Logger.v(TAG, "startRecording");
                ASRCommandStartRecording commandStartRecording = (ASRCommandStartRecording) asrCommand;
                correctList = commandStartRecording.correctList;
                recorderMode = commandStartRecording.redorderMode;

                initRecorderHandlerIfNeed();

                while (inprogress.get()) {
                    Logger.i(TAG, "waiting reset asr");
                    synchronized (inprogress) {
                        inprogress.wait(1000);
                    }
                    Logger.i(TAG, "got reset asr");
                }

                if (recorderMode == ASRConfig.RECORDER_MODE_JUST_RECORDER) {
                    recorderAndPlaybackHandler.startRecording();
                } else if (recorderMode == ASRConfig.RECORDER_MODE_JUST_ASR) {
                    sphinxStt.startListening(this);
                    asrMonitor.start();
                } else if (recorderMode == ASRConfig.RECORDER_MODE_RECORDER_ASR) {
                    sphinxStt.startListening(this);
                    asrMonitor.start();
                }
            } else if (action.compareTo("stopRecording") == 0) {
                Logger.v(TAG, "stopRecording");

                stop();
            } else if (action.compareTo("startRecognize") == 0) {
                Logger.v(TAG, "startRecognize");

            } else if (action.compareTo("startPlayback") == 0) {
                ASRCommandStartPlayback commandStartPlayback = (ASRCommandStartPlayback) asrCommand;
                String fileName = commandStartPlayback.audioFile;
                Logger.i(TAG, "startPlayback fileName:" + fileName);

                if (null != recorderAndPlaybackHandler) {
                    recorderAndPlaybackHandler.startPlayback(fileName);
                }
            } else if (action.compareTo("stopPlayback") == 0) {
                Logger.v(TAG, "stopPlayback");

                if (null != recorderAndPlaybackHandler) {
                    recorderAndPlaybackHandler.stopPlayback();
                }
            } else {
                Logger.e(TAG, "unexpected " + action);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Gson " + e);
            e.printStackTrace();
        }
    }

    public String getTempLmPath() {
        String filepath = asrConfig.getBaseCacheDir();

        File dir = new File(filepath, "ASR" + File.separator + "JSGF");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.getAbsolutePath() + File.separator
                + "temp.jsgf";
    }

    private void prepareContext(List<String> sentences) throws FileNotFoundException {
        List<String> strs = new ArrayList<String>();
        for (String t : sentences) {
//            t = normalizer.normalize(t);
//            Logger.i(TAG, "get txt:" + t);
//
//            if (t.contains("_")) {
//                sessionData.setContainWildcard();
//            }

            strs.add(t);
        }

        // TODO Implement the update diction function.
        // ServiceProvider.getASRSyncService().applyUpdatedDict();
        dicPath = asrConfig.POCKETSPHINX_CFG_DEFAULT_DICT;
        lmPath = getTempLmPath();
        if (sessionData.isContainingWildcard()) {
            GenerateJSGF.genJSGF(strs, lmPath, "", new WildcardGenerator());
        } else {
            GenerateJSGF.genJSGF(strs, lmPath, "", new NeighborGenerator());
        }

        // If get paths from package, needs:
        // dicPath =
        // asr.get("dicPath").getAsString().replaceFirst("file://", "");
        // lmPath =
        // asr.get("lmPath").getAsString().replaceFirst("file://", "");
    }

    private void stop() {
        if (recorderMode == ASRConfig.RECORDER_MODE_JUST_RECORDER) {
            recorderAndPlaybackHandler.stopRecording();
        } else if (recorderMode == ASRConfig.RECORDER_MODE_JUST_ASR) {
            asrMonitor.stop();
            if (sphinxStt.isListening()) {
                sphinxStt.stopListening();
            }
        } else if (recorderMode == ASRConfig.RECORDER_MODE_RECORDER_ASR) {
            asrMonitor.stop();
            if (sphinxStt.isListening()) {
                sphinxStt.stopListening();
            }
        }

        if (null != recorderAndPlaybackHandler) {
            recorderAndPlaybackHandler.stopPlayback();
        }
    }

    @Override
    public void onSttResult(String normalizedResult) {
        Logger.i(TAG, "onSttResult:" + normalizedResult);

//        handleASREvent(ASREngineController.MSG_ASR_REC_COMPLETE, null);

        if (normalizedResult == null) {
            sessionData.accumulateResult(ASRSessionData.RESULT_NULL);
            handleASREvent(ASREngineController.MSG_ASR_STT_RESULT_ERR, null);
        } else {
//            if (null != correctList) {
//                for (AsrCorrectItem i : correctList) {
//                    String normalized = normalizer.normalize(i.words, true);
//                    Logger.i(TAG, " cmp:" + sessionData.getCmp() + " matching:" + normalized + " ==> " + normalizedResult);
//
//                    if (normalized.contains("_")) {
//                        if (wildcardMatch(normalized, normalizedResult)) {
//                            sessionData.accumulateResult(ASRSessionData.RESULT_MATCHED);
//                            handleASREvent(MSG_ASR_STT_RESULT, i);
//                            return;
//                        }
//                    } else {
//                        normalized = normalized.replaceAll(" ", "'");
//                        normalizedResult = normalizedResult.replaceAll(" ", "'");
//                        if (normalized.compareToIgnoreCase(normalizedResult) == 0) {
//                            sessionData.accumulateResult(ASRSessionData.RESULT_MATCHED);
//                            handleASREvent(MSG_ASR_STT_RESULT, i);
//                            return;
//                        }
//                    }
//
//                }
//            }
//            sessionData.accumulateResult(ASRSessionData.RESULT_NOT_MATCHED);
//            handleASREvent(MSG_ASR_STT_RESULT_ERR, null);

            sessionData.accumulateResult(ASRSessionData.RESULT_MATCHED);
            handleASREvent(ASREngineController.MSG_ASR_STT_RESULT, normalizedResult);
        }
    }

    private boolean wildcardMatch(String target, String result) {
        result = result.replaceAll(" ", "'");
        result = result.toLowerCase();
        target = target.toLowerCase();

        Logger.i(TAG, "wildcardMatch:" + target + " ==> " + result);

        String[] strs = target.split("_");
        for (String str : strs) {
            Logger.i(TAG, "str:" + str);

            if (str == null || str.equals("")) {
                continue;
            }

            str = str.trim();
            str = str.replaceAll(" ", "'");
            if (!result.contains(str)) {
                return false;
            }
        }
        return true;
    }

    private void handleASREvent(int what, Object o) {

        switch (what) {
            case MSG_ASR_STT_RESULT: {
                asrListener.onSuccess((String) o);
                break;
            }

            case MSG_ASR_STT_RESULT_ERR: {
                asrListener.onError();
                break;
            }

            case MSG_ASR_REC_COMPLETE: {
                if (null == o){
                    //asrListener.onRecordComplete(recorderAndPlaybackHandler.getAudioTmpFilesPath());
                }else {
                    asrListener.onRecordComplete((String)o);
                }
                break;
            }

            case MSG_ASR_PB_COMPLETE: {
                asrListener.onPlaybackComplete();
                break;
            }
        }

        if (what == MSG_ASR_STT_RESULT) {
            resetAsr(2);
        }
    }

    final AtomicBoolean inprogress = new AtomicBoolean(false);
    boolean enableResetFeature = true;

    private void resetAsr(int mode) {
        Logger.i(TAG, "setContext mode:" + mode + " dic:" + dicPath + ", lm:" + lmPath);

        if (mode == 0) {
            executorService.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (inprogress.get()) {
                                Logger.e(TAG, "shouldn't be here!!!");
                            }
                            inprogress.set(true);
                            sphinxStt.setModelPath(dicPath, lmPath);

                            synchronized (inprogress) {
                                inprogress.set(false);
                                inprogress.notify();
                            }

                            Logger.i(TAG, "initAsrEngine setContext async done.");
                        }
                    }
            );

        } else if (mode == 1 && enableResetFeature) {
            inprogress.set(true);
            sphinxStt.setModelPath(dicPath, lmPath);
            inprogress.set(false);

            Logger.i(TAG, "reset setContext done.");

        } else if (mode == 2 && enableResetFeature) {
            executorService.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!inprogress.get()) {
                                inprogress.set(true);
                                sphinxStt.setModelPath(dicPath, lmPath);

                                synchronized (inprogress) {
                                    inprogress.set(false);
                                    inprogress.notify();
                                }

                                Logger.i(TAG, "reset setContext async done.");
                            }
                        }
                    }

            );

        }

        showMemeryInfo();
    }

    private void showMemeryInfo() {
        if (asrConfig.isDebug()) {
            final ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
            final int pid = android.os.Process.myPid();
            android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(new int[]{pid});
            Logger.d(TAG, "Mem USage: getTotalPrivateDirty:" + (float) memoryInfoArray[0].getTotalPrivateDirty() / 1024 + "MB" +
                    " Dalvik:" + memoryInfoArray[0].dalvikPrivateDirty / 1024 + "MB" +
                    " Native:" + memoryInfoArray[0].nativePrivateDirty / 1024 + "MB" +
                    " Other :" + memoryInfoArray[0].otherPrivateDirty / 1024 + "MB");
            Logger.d(TAG, "Mem USage: getTotalPss         :" + (float) memoryInfoArray[0].getTotalPss() / 1024 + "MB" +
                    " Dalvik:" + memoryInfoArray[0].dalvikPss / 1024 + "MB" +
                    " Native:" + memoryInfoArray[0].nativePss / 1024 + "MB" +
                    " Other :" + memoryInfoArray[0].otherPss / 1024 + "MB");
            Logger.d(TAG, "Mem USage: getTotalSharedDirty :" + (float) memoryInfoArray[0].getTotalSharedDirty() / 1024 + "MB" +
                    " Dalvik:" + memoryInfoArray[0].dalvikSharedDirty / 1024 + "MB" +
                    " Native:" + memoryInfoArray[0].nativeSharedDirty / 1024 + "MB" +
                    " Other :" + memoryInfoArray[0].otherSharedDirty / 1024 + "MB");
        }
    }

    private void getParserRecorderMode(String templateCode) {
        if (isASRTemplate(templateCode)) {
            if (templateCode.equals("LngComp")) {
                recorderMode = ASRConfig.RECORDER_MODE_RECORDER_ASR;
            } else {
                recorderMode = ASRConfig.RECORDER_MODE_JUST_ASR;
            }
        } else {
            recorderMode = ASRConfig.RECORDER_MODE_JUST_RECORDER;
        }

        Logger.v(TAG, "getParserRecorderMode,recorderMode=" + recorderMode);
    }

    /**
     * need update the recorder mode according to the js callback by yongwei
     */
    private void updateRecorderMode() {
        if (null != recorderAndPlaybackHandler) {
            recorderAndPlaybackHandler.setRecorderMode(recorderMode);
        }
    }

    private static boolean isASRTemplate(String templateCode) {
        return (templateCode.equals("FlaExe")
                || templateCode.equals("RolePlayAudio")
                || templateCode.equals("RolePlayVideo")
                || templateCode.equals("LngComp"));
    }

    static class ASRMonitor extends TimerTask implements Runnable {
        SphinxSpeachToText sphinxSpeachToText;

        Timer timer;

        private static final int ASR_TIMEOUT_DURATION = 3 * 60 * 1000;

        public ASRMonitor(SphinxSpeachToText sphinxSpeachToText) {
            this.sphinxSpeachToText = sphinxSpeachToText;
        }

        public void start() {
            Logger.v(TAG, "ASR Timeout start");

            timer = new Timer();
            timer.schedule(this, ASR_TIMEOUT_DURATION);
        }

        public void stop() {
            Logger.v(TAG, "ASR Timeout stop");

            // stop may be called even though start has not, e.g. when the controller is destroyed.
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        @Override
        public void run() {
            Logger.v(TAG, "ASR Timeout beego");

            if (sphinxSpeachToText.isListening()) {
                sphinxSpeachToText.stopListening();
            }
        }
    }

    public static class ASRSessionData {
        public static final String S_ASR_BASIC = "asr-basic";
        public static final String S_ASR_INFO = "asr-info";
        public static final String S_ASR_SESSION = "asr-session";

        public static final int RESULT_NULL = 0;
        public static final int RESULT_MATCHED = 1;
        public static final int RESULT_NOT_MATCHED = 2;

        public ASRSessionData() {
            this.containingWildcard = false;
            this.countNull = 0;
            this.countMatched = 0;
            this.countUnmatched = 0;
            this.lastMatch = 0;
        }

        public void setContainWildcard() {
            Logger.i(TAG, "containing WildCard");
            containingWildcard = true;
        }

        public boolean isContainingWildcard() {
            return containingWildcard;
        }

        public int getTotal() {
            return countMatched + countUnmatched + countNull;
        }

        public int getCmp() {
            return countMatched + countUnmatched;
        }

        private void accumulateResult(int mode) {
            if (mode == RESULT_NULL) { // onSttResult null
                countNull++;
                lastMatch++;
            } else if (mode == RESULT_MATCHED) { // onSttResult matched
                countMatched++;
                lastMatch = 0;
            } else if (mode == RESULT_NOT_MATCHED) { // onSttResult unmatched
                countUnmatched++;
                lastMatch++;
            }
            Logger.i(TAG, " total:" + getTotal() + " cmp:" + getCmp() + ",null:" + countNull + " matched:" + countMatched + ",unmatched:" + countUnmatched + " lastMatch:" + lastMatch);
        }

        public void statAsrBasic() {
            Logger.d(TAG, "statAsr -> Basic");
        }

        public void statAsrSession() {
            Logger.d(TAG, "statAsr -> Session,"
                    + " " + Long.valueOf(countNull)
                    + " " + Long.valueOf(countMatched)
                    + " " + Long.valueOf(countUnmatched));
            if (!sentAccumulateResult) {
                sentAccumulateResult = true;

                if (countNull == 0 && countMatched == 0 && countUnmatched == 0) {
                    Logger.d(TAG, "statAsr -> Skip");
                    return;
                }

                Logger.d(TAG, "statAsr -> Session, sending..., countNull = " + countNull + ", countMatched = " + countMatched + ", countUnmatched = " + countUnmatched);
//                if (countNull > 0)
//                    StatUtil.sendEvent(S_ASR_SESSION, "count-null", "0", Long.valueOf(countNull));
//
//                StatUtil.sendEvent(S_ASR_SESSION, "count-matched", "0", Long.valueOf(countMatched));
//                StatUtil.sendEvent(S_ASR_SESSION, "count-unmatched", "0", Long.valueOf(countUnmatched));
            }
        }

        private boolean containingWildcard;

        private int countNull;
        private int countMatched;
        private int countUnmatched;
        private int lastMatch;

        private boolean sentAccumulateResult = false;
    }

    public void stopSession() {
        if (sessionData != null) {
            sessionData.accumulateResult(3);
            sessionData.statAsrSession();
        }

        stop();
        Logger.v(TAG, "stopSession");
    }
}