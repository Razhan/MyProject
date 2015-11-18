package com.ef.bite.ui.record;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.trinea.android.common.util.PreferencesUtils;
import com.czt.mp3recorder.MP3Recorder;
import com.ef.bite.AppConst;
import com.ef.bite.R;
import com.ef.bite.Tracking.ContextDataMode;
import com.ef.bite.Tracking.MobclickTracking;
import com.ef.bite.business.UserScoreBiz;
import com.ef.bite.business.task.PostExecuting;
import com.ef.bite.business.task.UploadRecordingTask;
import com.ef.bite.dataacces.mode.PresentationConversation;
import com.ef.bite.lang.Closure;
import com.ef.bite.ui.chunk.BaseChunkActivity;
import com.ef.bite.ui.guide.RecordGuideAvtivity;
import com.ef.bite.ui.main.MainActivity;
import com.ef.bite.utils.FileStorage;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.widget.DonutProgress;
import com.ef.bite.widget.HeaderView;
import com.ef.bite.widget.PagesIndicator;
import com.ef.efekta.asr.JSGFgen.NeighborGrammarGenerator;
import com.englishtown.android.asr.core.ASRConfig;
import com.englishtown.android.asr.core.ASREngine;
import com.englishtown.android.asr.core.ASREngineController;
import com.englishtown.android.asr.core.ASRListener;
import com.englishtown.android.asr.core.AsrCorrectItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Record user's practice and upload it
 * Created by yang on 15/3/19.
 */
public class ASRActivity extends BaseChunkActivity {

    private AppPreference appPreference;
    private ASRConfig asrConfig;
    private ASREngine asrEngine;
    private NeighborGrammarGenerator neighborGrammarGenerator;
    private String audioPath;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private ArrayList<AsrCorrectItem> correctItemArrayList;
    private String phrase = "Record user's practice and upload it";


    private File mRecAudioFile;// Recording output file
    // private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private FileStorage recStorage;
    private static String PREFIX = "rec";// The prefix name of recording file
    private static int DURATION = 10;// Recording limit time,default is 10s
    private static int CREDITS = 15;// Reward credits
    private Button recBtn;// Recording button
    private Button playBtn;// Replay button
    private Button submitBtn;// Submit button
    private TextView titleView;
    private TextView tipsView;
    private TextView skipView;
    private TextView scoreView;
    private DonutProgress recProgress;
    private Timer progressTimer;
    private Timer counterTimer;
    private ImageView audioView;
    private ProgressDialog progressDialog;
    private MP3Recorder mMediaRecorder;

    private Handler mHandler;

    public static final int MSG_ASR_REC_START = 0;
    public static final int MSG_ASR_REC_END = 1;
    public static final int MSG_ASR_PB_START = 2;
    public static final int MSG_ASR_PB_COMPLETE = 3;
    public static final int MSG_ASR_REC_SUCCESS = 4;
    public static final int MSG_ASR_REC_COMPLETE = 5;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chunk_recording);
        super.onCreate(savedInstanceState);
        recStorage = new FileStorage(this, AppConst.CacheKeys.Storage_Recording);
        initHandler();
        setupViews();

        appPreference = AppPreference.getInstance(getApplicationContext());

        File filesDir = getApplicationContext().getFilesDir();
        String base = filesDir.getPath();
        String hmmPath = base + "/efoffline/asr/hmm";

        asrConfig = new ASRConfig(hmmPath, true, base);
        asrEngine = ASREngineController.getInstance(getApplication()).setConfig(asrConfig);

        installAsrEngine();

//        openGuide();
        tracking();

    }

    private void initHandler() {

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case MSG_ASR_REC_START:
                        break;
                    case MSG_ASR_REC_END:
                        break;
                    case MSG_ASR_PB_START:
                        break;
                    case MSG_ASR_PB_COMPLETE:
                        playBtn.performClick();
                        break;
                    case MSG_ASR_REC_SUCCESS:
                        String tip = (String)msg.obj;
                        titleView.setText(tip);
                        break;
                    case MSG_ASR_REC_COMPLETE:
                        tipsView.setText("recoding complete");
                        playBtn.setVisibility(View.VISIBLE);
                        submitBtn.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        };

    }

    private void installAsrEngine() {
        if (appPreference.isAsrPreInited()) {
            initasrEngine();

            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Install ASR Engine...");

        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                LifecycleActions.performStartActions(getApplicationContext(), asrEngine, asrConfig.getBaseCacheDir());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();

                        initasrEngine();
                    }
                });
            }
        }).start();
    }

    private void initasrEngine() {
        asrEngine.initAsrEngine().setListener(new ASRListener() {
            @Override
            public void onRecordComplete(String audio) {
                audioPath = audio;
                Log.d("onRecordComplete", "onRecordComplete, " + audioPath + '\n');
                if (audioPath != null) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_ASR_REC_COMPLETE;
                    msg.obj = audioPath;

                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onSuccess(final String correctItem) {
                Log.d("onSuccess", "onSuccess: " + correctItem + '\n');

                Message msg = mHandler.obtainMessage();
                msg.what = MSG_ASR_REC_SUCCESS;
                msg.obj = correctItem;

                mHandler.sendMessage(msg);
            }

            @Override
            public void onError() {
                Log.d("onError", "onError" + '\n');
            }

            @Override
            public void onPlaybackComplete() {
                Log.d("onPlaybackComplete", "onPlaybackComplete" + '\n');

                mHandler.sendEmptyMessage(MSG_ASR_PB_COMPLETE);
            }
        });

        prepareSentencesContext();
    }

    private void prepareSentencesContext() {

        List<String> sentencesList = testNeighborGrammarGenerator();

        setContext(sentencesList);
    }

    private void setContext(final List<String> sentencesList) {
        final ProgressDialog progressDialog = new ProgressDialog(ASRActivity.this);
        progressDialog.setMessage("Preparing context...");
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (null == correctItemArrayList) {
                    correctItemArrayList = new ArrayList<>();
                    int i = 0;
                    for (String s : sentencesList) {
                        correctItemArrayList.add(new AsrCorrectItem(i, s));
                        i++;
                    }

                    asrEngine.setContext(sentencesList);

                    ASRActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }).start();

    }

    private List<String> testNeighborGrammarGenerator() {

        neighborGrammarGenerator = new NeighborGrammarGenerator(null, asrConfig.POCKETSPHINX_CFG_DEFAULT_DICT);
        List<String> list = Arrays.asList(phrase.split(" "));

        return neighborGrammarGenerator.getSentenceNeighbors(list);
    }


    // Initialize views
    private void setupViews() {

        PagesIndicator indicator = (PagesIndicator) findViewById(R.id.indicator);
        indicator.setIndicator(4, 4);

        HeaderView headerView = (HeaderView) findViewById(R.id.header);
        progressbar = headerView.getProgressbar();

        titleView = (TextView) findViewById(R.id.tv_title);
        tipsView = (TextView) findViewById(R.id.tv_tips);
        tipsView.setText(JsonSerializeHelper.JsonLanguageDeserialize(mContext,
                "recording_view_record"));
        scoreView = (TextView) findViewById(R.id.tv_score);
        audioView = (ImageView) findViewById(R.id.iv_audio);
        recProgress = (DonutProgress) findViewById(R.id.recorder_progress);
        recBtn = (Button) findViewById(R.id.btn_rec);
        playBtn = (Button) findViewById(R.id.btn_play);
        submitBtn = (Button) findViewById(R.id.btn_post);
        submitBtn.setText(JsonSerializeHelper.JsonLanguageDeserialize(mContext,
                "record_txt_post"));
        skipView = (TextView) findViewById(R.id.tv_skip);
        skipView.setText(JsonSerializeHelper.JsonLanguageDeserialize(mContext,
                "recoding_view_skip"));

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadRecording();
            }
        });

        recBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    audioView.setVisibility(View.INVISIBLE);
                    stopProgress();
                    asrEngine.stopRecording();
                } else {
                    playBtn.setVisibility(View.GONE);
                    audioView.setVisibility(View.VISIBLE);
                    submitBtn.setVisibility(View.INVISIBLE);
                    switchProgressColor(true);
                    asrEngine.stopPlayBack();
                    startProgress(1000 * DURATION, new Closure() {
                        @Override
                        public void execute(Object result) {
//                            onRecStop();// do something after counting stop
                        }
                    });
                    startCounter();
                    updateDecibelStatus();

                    asrEngine.startRecording(correctItemArrayList, ASRConfig.RECORDER_MODE_RECORDER_ASR);
                }

                isRecording = !isRecording;
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    playBtn.setBackgroundResource(R.drawable.ic_play);
                    stopProgress();
                    startProgress(0, null);

                    asrEngine.stopPlayBack();
                } else {
                    playBtn.setBackgroundResource(R.drawable.ic_stop);
                    switchProgressColor(false);
                    int i = getDuration();
                    startProgress(getDuration(), null);

                    asrEngine.startPlayback(audioPath);
                }

                isPlaying = !isPlaying;
            }
        });

        if (mChunkModel != null) {

            List<PresentationConversation> list = mChunkModel.getChunkPresentation().getPresentationConversations();
            for (PresentationConversation p : list) {
                if (p.getSentence().contains(mChunkModel.getChunkText())) {
                    phrase = p.getSentence();
                    break;
                }
            }
        }
        titleView.setText(phrase);

        skipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUsersRecordAct();
                finish();
            }
        });

        mGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * draw progress circle
     */
    private void startProgress(long duration, final Closure callback) {
        if (duration <= 0) {
            recProgress.setProgress(0);
            return;
        }
        recProgress.setProgress(0);
        progressTimer = new Timer();
        progressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recProgress.setProgress(recProgress.getProgress() + 1);
                        if (recProgress.getProgress() >= 360) {
                            progressTimer.cancel();
                            if (callback != null) {
                                callback.execute(null);
                            }
                        }
                    }
                });
            }
        }, 500, duration / 360);
    }

    /**
     * update counter
     */
    private void startCounter() {
        counterTimer = new Timer();
        counterTimer.schedule(new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tipsView.setText("" + (DURATION - count));
                        count++;
                        if (DURATION == count) {
                            counterTimer.cancel();
                        }
                    }
                });
            }
        }, 500, 1000);
    }

    private void stopProgress() {
        if (progressTimer != null) {
            progressTimer.cancel();
        }
        if (counterTimer != null) {
            counterTimer.cancel();
        }
    }

    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateDecibelStatus();
        }
    };

    private int BASE = 1;// the reference amplitude values
    private int DELAY = 100;// Sampling interval time

    private void updateDecibelStatus() {
        if (mMediaRecorder != null) {
            double ratio = (double) mMediaRecorder.getVolume() / BASE;
            double db = 0;// decibel
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            updateAudioView(db);
            // Log.d("---dB", "dB：" + db);
            mHandler.postDelayed(mUpdateMicStatusTimer, DELAY);
        } else {
            updateAudioView(0);
        }
    }

    private void updateAudioView(double db) {
        if (db > 45) {
            audioView.setBackgroundResource(R.drawable.audio_level_3);
        } else if (db > 35) {
            audioView.setBackgroundResource(R.drawable.audio_level_2);
        } else if (db > 25) {
            audioView.setBackgroundResource(R.drawable.audio_level_1);
        } else {
            audioView.setBackgroundResource(R.drawable.audio_level_0);
        }
    }

    private void switchProgressColor(boolean isActived) {
        recProgress.setFinishedStrokeColor(getResources().getColor(
                isActived ? R.color.bella_prgress_fg
                        : R.color.bella_prgress_fg_2));
        recProgress.setUnfinishedStrokeColor(getResources().getColor(
                isActived ? R.color.bella_prgress_bg
                        : R.color.bella_prgress_bg_2));
        recBtn.setBackgroundResource(isActived ? R.drawable.mic_active
                : R.drawable.mic_inactive);
    }

    private void uploadRecording() {
        if (mChunkModel == null) {
            toast("No Course file");
            return;
        }

        if (mRecAudioFile == null) {
            toast("No recording file");
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(JsonSerializeHelper.JsonLanguageDeserialize(
                mContext, "record_msg_uploading"));
        progressDialog.setCancelable(false);
        progressDialog.show();
        UploadRecordingTask task = new UploadRecordingTask(this, mRecAudioFile,
                mChunkModel.getChunkCode(),
                String.valueOf(getDuration() / 1000), "15",
                new PostExecuting<Boolean>() {
                    @Override
                    public void executing(Boolean result) {
                        progressDialog.dismiss();
                        if (result) {
                            toast(JsonSerializeHelper.JsonLanguageDeserialize(
                                    mContext, "record_msg_upload_completed"));
                            increasingScore(CREDITS);//
                            addScore(CREDITS);

                            MobclickTracking.OmnitureTrack.ActionTrackingRecordingSuccessful(mContext);

                            new Handler().postDelayed(new Runnable() {
                                public void run() {
//                                    openUsersRecordAct();
                                    startActivity(new Intent(mContext, MainActivity.class));
                                    finish();
                                }
                            }, 500);
                        } else {
                            toast(JsonSerializeHelper.JsonLanguageDeserialize(
                                    mContext, "record_msg_upload_failed"));
                        }
                    }
                });
        task.execute();
    }

    /**
     * Return recording duration
     *
     * @return
     */
    private int getDuration() {

        int duration = asrEngine.getDuration();

        if (duration == -1) {
            return 10 * 1000;
        } else {
            return duration;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopProgress();
        asrEngine.destroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Increasing score animation
     *
     * @param increasedScore
     */
    protected void increasingScore(int increasedScore) {
        // Score Increasing Text initialized
        scoreView.setVisibility(View.VISIBLE);
        scoreView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
                .getDimension(R.dimen.balloon_label_size));
        scoreView.setText("+" + Integer.toString(increasedScore));
        int cur_color = getResources()
                .getColor(R.color.bella_color_orange_dark);
        scoreView.setTextColor(cur_color);
        // Start user level increasing animation
        mLevel.startIncreasingScore(increasedScore, cur_color);
        // Start the increasing text animation
        scoreView.setVisibility(View.VISIBLE);
        ValueAnimator disappearAnim = ValueAnimator.ofFloat(1, 0);
        disappearAnim
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float alpha = (Float) animation.getAnimatedValue();
                        scoreView.setAlpha(alpha);
                    }
                });
        disappearAnim.setDuration(1000);
        disappearAnim.start();
    }

    /**
     * update score in DB
     *
     * @param score
     */
    private void addScore(int score) {
        UserScoreBiz userScoreBiz = new UserScoreBiz(this);
        userScoreBiz.increaseScore(score);
    }

    private void tracking() {
        MobclickTracking.OmnitureTrack.AnalyticsTrackState(
                ContextDataMode.RecordingValues.pageNameValue,
                ContextDataMode.RecordingValues.pageSiteSubSectionValue,
                ContextDataMode.RecordingValues.pageSiteSectionValue, this);
    }

    private void openGuide() {
        if (PreferencesUtils.getBoolean(this,
                AppConst.CacheKeys.Storage_Record_guide)) {
            return;
        }

        PreferencesUtils.putBoolean(this,
                AppConst.CacheKeys.Storage_Record_guide, true);
        startActivity(new Intent(this, RecordGuideAvtivity.class));

    }

    private void openUsersRecordAct() {
        Intent intent = new Intent(ASRActivity.this,
                UserRecordingActivity.class);
        intent.putExtra(AppConst.BundleKeys.Chunk, mChunkModel);
        intent.putExtra(AppConst.BundleKeys.BELLAID,
                AppConst.CurrUserInfo.UserId);
        startActivity(intent);
    }
}

