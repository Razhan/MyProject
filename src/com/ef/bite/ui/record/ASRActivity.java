package com.ef.bite.ui.record;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.trinea.android.common.util.PreferencesUtils;
import com.ef.bite.AppConst;
import com.ef.bite.AppSession;
import com.ef.bite.R;
import com.ef.bite.Tracking.ContextDataMode;
import com.ef.bite.Tracking.MobclickTracking;
import com.ef.bite.business.UserScoreBiz;
import com.ef.bite.business.UserServerAPI;
import com.ef.bite.business.task.PostExecuting;
import com.ef.bite.business.task.UploadRecordingTask;
import com.ef.bite.dataacces.AchievementCache;
import com.ef.bite.dataacces.ConfigSharedStorage;
import com.ef.bite.dataacces.TutorialConfigSharedStorage;
import com.ef.bite.dataacces.mode.Achievement;
import com.ef.bite.dataacces.mode.HintDefinition;
import com.ef.bite.dataacces.mode.PresentationConversation;
import com.ef.bite.dataacces.mode.httpMode.HttpBaseMessage;
import com.ef.bite.lang.Closure;
import com.ef.bite.model.ConfigModel;
import com.ef.bite.ui.chunk.BaseChunkActivity;
import com.ef.bite.ui.guide.GuideReviewRecordingActivity;
import com.ef.bite.ui.guide.RecordGuideAvtivity;
import com.ef.bite.ui.main.MainActivity;
import com.ef.bite.ui.popup.BaseDialogFragment;
import com.ef.bite.ui.popup.BasePopupWindow;
import com.ef.bite.ui.popup.ChunkDonePopWindow;
import com.ef.bite.ui.popup.LevelUpPopWindow;
import com.ef.bite.ui.popup.ScoresUpDialogFragment;
import com.ef.bite.utils.FileStorage;
import com.ef.bite.utils.IFileStorage;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.utils.ScoreLevelHelper;
import com.ef.bite.utils.StringUtils;
import com.ef.bite.utils.TimeFormatUtil;
import com.ef.bite.widget.DonutProgress;
import com.ef.bite.widget.GifImageView;
import com.ef.bite.widget.GifMovieView;
import com.ef.bite.widget.HeaderView;
import com.ef.bite.widget.PagesIndicator;
import com.ef.efekta.asr.JSGFgen.NeighborGrammarGenerator;
import com.ef.efekta.asr.textnormalizer.StringUtil;
import com.ef.efekta.asr.textnormalizer.TextNormalizer;
import com.ef.efekta.asr.textnormalizer.WordFinder;
import com.englishtown.android.asr.core.ASRConfig;
import com.englishtown.android.asr.core.ASREngine;
import com.englishtown.android.asr.core.ASREngineController;
import com.englishtown.android.asr.core.ASRListener;
import com.englishtown.android.asr.core.AsrCorrectItem;
import com.litesuits.android.async.SimpleTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Record user's practice and upload it
 * Created by yang on 15/3/19.
 */
public class ASRActivity extends BaseChunkActivity {

    private ASRConfig asrConfig;
    private ASREngine asrEngine;
    private NeighborGrammarGenerator neighborGrammarGenerator;
    private String audioPath;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private ArrayList<AsrCorrectItem> correctItemArrayList;
    private String phrase = "";
    private String normalizedPhrase = "";

    private FileStorage recStorage;
    private File mRecAudioFile;
    private List<String> sentencesList;

    // private MediaRecorder mMediaRecorder;
    private static String PREFIX = "rec";// The prefix name of recording file
    private static int DURATION = 10;// Recording limit time,default is 10s
    private static int CREDITS = 15;// Reward credits
    private static int TOTALCREDITS = 100;

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

    private Handler mHandler;

    public static final int MSG_ASR_REC_START = 0;
    public static final int MSG_ASR_REC_END = 1;
    public static final int MSG_ASR_PB_START = 2;
    public static final int MSG_ASR_PB_COMPLETE = 3;
    public static final int MSG_ASR_REC_SUCCESS = 4;
    public static final int MSG_ASR_REC_COMPLETE = 5;

    private final static int MultiChoiceLearn_mean = 1;
    private final static int MultiChoiceLearn_use = 2;
    private final static int PhraseLearnedMessageValues = 3;
    private final static int LevelUpMessageValues = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chunk_recording);
        super.onCreate(savedInstanceState);
        initHandler();
        setupViews();

        recStorage = new FileStorage(this, AppConst.CacheKeys.Storage_Recording);
        try {
            recStorage.clearAll();
            mRecAudioFile = File.createTempFile(PREFIX, ".mp3",
                    recStorage.getStorageDir());

        } catch (IOException e) {
            e.printStackTrace();
        }

        File filesDir = getApplicationContext().getFilesDir();
        String base = filesDir.getPath();
        String hmmPath = base + "/efoffline/asr/hmm";

        asrConfig = new ASRConfig(hmmPath, true, base);
        asrEngine = ASREngineController.getInstance(getApplication()).setConfig(asrConfig);

        ASTInitAsyncTask asyncTask = new ASTInitAsyncTask();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Prepare ASR Engine...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        asyncTask.execute();

        openGuide();
        tracking();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        sentencesList = savedInstanceState.getStringArrayList("sentencesList");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("sentencesList", (ArrayList<String>) sentencesList);

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
                        String tip = (String) msg.obj;
                        boolean result = changeColor(
                                StringUtils.getDIff(Arrays.asList(tip.split(" ")),
                                        Arrays.asList(phrase.toUpperCase().split(" ")))
                        );
                        if (result) {
                            tipsView.setText(JsonSerializeHelper.JsonLanguageDeserialize(mContext,
                                    "record_asr_correct"));
                            submitBtn.setVisibility(View.VISIBLE);
                            scoreView.setVisibility(View.VISIBLE);
                        } else {
                            tipsView.setText(JsonSerializeHelper.JsonLanguageDeserialize(mContext,
                                    "record_asr_incorrect"));
                            submitBtn.setVisibility(View.INVISIBLE);
                            scoreView.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case MSG_ASR_REC_COMPLETE:
                        playBtn.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void installAsrEngine() {

        if (PreferencesUtils.getBoolean(this,
                AppConst.CacheKeys.APP_PREFERENCE_ASR_PREINITED)) {
            initasrEngine(mRecAudioFile.getAbsolutePath());
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                FirstTimeInstall(asrEngine, asrConfig.getBaseCacheDir());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initasrEngine(mRecAudioFile.getAbsolutePath());
                    }
                });
            }
        }).start();
    }

    private static void FirstTimeInstall(ASREngine asrEngine, String baseCacheDir) {
        try {

            File offlinePackDir = new File(baseCacheDir);
            offlinePackDir.mkdirs();
            new File(offlinePackDir, ".nomedia").mkdir();

            String zipFile = "hub4wsj_sc_8k.zip";
            asrEngine.installEngine(zipFile);

        } catch (IOException e) {
            throw new RuntimeException("Cannot start app", e);
        }
    }

    private void initasrEngine(String mp3path) {
        asrEngine.initAsrEngine(mp3path).setListener(new ASRListener() {
            @Override
            public void onRecordComplete(String audio) {
                audioPath = audio;
                if (audioPath != null) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_ASR_REC_COMPLETE;
                    msg.obj = audioPath;

                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onSuccess(final String correctItem) {
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
//                mHandler.sendEmptyMessage(MSG_ASR_PB_COMPLETE);
            }
        });

        prepareSentencesContext();
    }

    private void prepareSentencesContext() {

        if (sentencesList == null || sentencesList.size() < 1) {
            sentencesList = NeighborGrammarGenerator();
        }

        setContext(sentencesList);
    }

    private void setContext(final List<String> sentencesList) {
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
                }
            }
        }).start();

    }

    private List<String> NeighborGrammarGenerator() {

        neighborGrammarGenerator = new NeighborGrammarGenerator(null, asrConfig.POCKETSPHINX_CFG_DEFAULT_DICT);
        List<String> list = Arrays.asList(normalizedPhrase
                .split(" "));

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
                "record_asr_instruction"));
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
                    playBtn.setVisibility(View.VISIBLE);
                    stopProgress();
                    asrEngine.stopRecording();
                } else {
                    playBtn.setVisibility(View.GONE);
                    asrEngine.stopPlayBack();

                    audioView.setVisibility(View.VISIBLE);
                    submitBtn.setVisibility(View.INVISIBLE);
                    switchProgressColor(true);
                    startProgress(1000 * DURATION, new Closure() {
                        @Override
                        public void execute(Object result) {
                            recBtn.performClick();
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
                    recBtn.setClickable(true);

                    asrEngine.stopPlayBack();
                } else {
                    if (audioPath == null) {
                        tipsView.setText(JsonSerializeHelper.JsonLanguageDeserialize(mContext,
                                "record_asr_nothingness"));
                        return;
                    }

                    playBtn.setBackgroundResource(R.drawable.ic_stop);
                    switchProgressColor(false);
                    startProgress(getDuration(), new Closure() {
                        @Override
                        public void execute(Object result) {
                            playBtn.performClick();
                        }
                    });
                    asrEngine.stopRecording();
                    recBtn.setClickable(false);
                    audioView.setVisibility(View.GONE);
                    asrEngine.startPlayback(audioPath);
                }
                isPlaying = !isPlaying;
            }
        });

        if (mChunkModel != null) {
            List<HintDefinition> list = mChunkModel.getHintDefinitions();
            String str = list.get(list.size() - 1).getExample()
                    .split("\n")[0]
                    .substring(3)
                    .replace("<h>", "")
                    .replace("</h>", "");

            TextNormalizer normalizer = new TextNormalizer(WordFinder.getPattern());
            normalizedPhrase = normalizer.normalize(str, true);
            phrase = normalizer.normalize(str, false);
        }

        titleView.setText(phrase);

        skipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addScore(TOTALCREDITS);
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
        }, 100, duration / 360);
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
        if (asrEngine != null) {

            double ratio = (double) asrEngine.getAmplitude() / BASE;
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

        if (audioPath == null) {
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

//                            new Handler().postDelayed(new Runnable() {
//                                public void run() {
////                                    openUsersRecordAct();
//                                    startActivity(new Intent(mContext, MainActivity.class));
//                                    finish();
//                                }
//                            }, 500);
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

    private boolean changeColor(Map<Integer, Integer> map) {

        SpannableStringBuilder builder = new SpannableStringBuilder(phrase);

        //ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色
        ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);

        builder.setSpan(redSpan, 0, phrase.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            ForegroundColorSpan greenSpan = new ForegroundColorSpan(Color.GREEN);
            builder.setSpan(greenSpan, entry.getKey(), entry.getValue(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        titleView.setText(builder);

        if (map.size() > Arrays.asList(phrase.split(" ")).size() / 2) {
            return true;
        } else {
            return false;
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
    private void addScore(final int score) {
        UserScoreBiz userScoreBiz = new UserScoreBiz(this);
        final int userScore = userScoreBiz.getUserScore();
        userScoreBiz.increaseScore(score);

        // Assemble achievement
        Achievement achievement = new Achievement();
        achievement.setBella_id(AppConst.CurrUserInfo.UserId);
        achievement.setPlan_id(AppConst.CurrUserInfo.CourseLevel);
        achievement.setCourse_id(mChunkModel.getChunkCode());
        achievement.setScore(score);
        achievement.setStart_client_date(PreferencesUtils.getString(this, AppConst.CacheKeys.TIME_START));
        achievement.setEnd_client_date(TimeFormatUtil.getUTCTimeStr());
        achievement.setUpdate_progress_type(Achievement.TYPE_COMPLETE_LEARN);
        // Post to server
        postUserAchievement(achievement);
        ChunkDonePopWindow donDialog = new ChunkDonePopWindow(
                ASRActivity.this, mChunkModel.getChunkText(),
                mChunkModel.getChunkCode(), mContext);
        donDialog.setOnCloseListener(new BasePopupWindow.OnCloseListener() {
            @Override
            public void onClose() {
                // 积分增加dialog
                ScoresUpDialogFragment scoreUpDilaog = new ScoresUpDialogFragment(
                        ASRActivity.this,
                        ScoreLevelHelper
                                .getDisplayLevel(userScore),
                        ScoreLevelHelper
                                .getCurrentLevelScore(userScore),
                        ScoreLevelHelper
                                .getCurrentLevelExistedScore(userScore),
                        score, mContext);
                // 检查是否levelup
                final int up2Level = score > ScoreLevelHelper
                        .getLevelUpScore(userScore) ? ScoreLevelHelper
                        .getDisplayLevel(userScore
                                + score) : 0;
                scoreUpDilaog
                        .setOnDismissListener(new BaseDialogFragment.OnDismissListener() {
                            @Override
                            public void onDismiss(
                                    DialogInterface dialog) {
                                if (up2Level <= 0) {
                                    opennextActivity();
                                } else {
                                    LevelUpPopWindow levelUp = new LevelUpPopWindow(
                                            ASRActivity.this,
                                            up2Level);
                                    levelUp.setOnDismissListener(new LevelUpPopWindow.OnDismissListener() {
                                        @Override
                                        public void onDismiss() {
                                            opennextActivity();
                                        }
                                    });
                                    levelUp.open();
//                                    lvl = String.valueOf(up2Level);
                                    BI_Tracking(LevelUpMessageValues);

                                }
                            }
                        });
                scoreUpDilaog.show(getSupportFragmentManager(),
                        "scores up");
            }
        });
        donDialog.open();


    }

    private void opennextActivity() {
        AppConst.GlobalConfig.IsChunkPerDayLearned = true;
        // new HomeScreenOpenAction().open(mContext, null);
        // Intent intent = new Intent(mContext, ReviewActivity.class);
        AppSession.getInstance().clear();
        TutorialConfigSharedStorage tutorialConfigSharedStorage = new TutorialConfigSharedStorage(
                mContext, AppConst.CurrUserInfo.UserId);
        if (tutorialConfigSharedStorage.get().Tutorial_Review_Record) {
            Intent intent = new Intent(mContext, ReviewActivity.class);
            intent.putExtra(AppConst.BundleKeys.Chunk, mChunkModel);
            intent.putExtra(AppConst.BundleKeys.Is_Chunk_Learning, true);
            intent.putExtra(AppConst.BundleKeys.Hide_Bottom_Lay, 1);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(mContext,
                    GuideReviewRecordingActivity.class);
            intent.putExtra(AppConst.BundleKeys.Chunk, mChunkModel);
            intent.putExtra(AppConst.BundleKeys.Hide_Bottom_Lay, 1);
            startActivity(intent);
            finish();
        }
    }

    public void postUserAchievement(final Achievement achievement) {

        SimpleTask<HttpBaseMessage> task = new SimpleTask<HttpBaseMessage>() {

            @Override
            protected void onPreExecute() {
                if(StringUtils.isEquals(Achievement.TYPE_COMPLETE_LEARN,achievement.getUpdate_progress_type())){
                    dashboardCache.removeLesson(achievement.getCourse_id());
                }else {
                    dashboardCache.removeRehearsal(achievement.getCourse_id());
                }
            }

            @Override
            protected HttpBaseMessage doInBackground() {
                try {
                    UserServerAPI serverAPI = new UserServerAPI(mContext);
                    return serverAPI.postAchievement(new ArrayList<Achievement>(Arrays.asList(achievement)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(HttpBaseMessage result) {
                if (result != null && StringUtils.isEquals(result.status, "0")) {

                } else {
                    AchievementCache.getInstance().setPersnalCache(achievement);
                }
            }
        };

        task.execute();

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

    public class ASTInitAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            installAsrEngine();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            PreferencesUtils.putBoolean(ASRActivity.this,
                    AppConst.CacheKeys.APP_PREFERENCE_ASR_PREINITED, true);
        }
    }
}

