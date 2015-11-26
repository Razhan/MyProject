package com.ef.bite.ui.record;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ef.efekta.asr.JSGFgen.NeighborGrammarGenerator;
import com.englishtown.android.asr.core.ASRConfig;
import com.englishtown.android.asr.core.ASREngine;
import com.englishtown.android.asr.core.ASRListener;
import com.englishtown.android.asr.core.ASREngineController;
import com.englishtown.android.asr.core.AsrCorrectItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ef.bite.R;



public class asr_test extends Activity {
    AppPreference appPreference;
    ASRConfig asrConfig;
    ASREngine asrEngine;
    private NeighborGrammarGenerator neighborGrammarGenerator;

    TextView sentences;
    TextView result;
    Button startButton;
    Button playButton;
    boolean isRecording = false;
    private String audioPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asr_test);

        appPreference = AppPreference.getInstance(getApplicationContext());
        init();

        File filesDir = getApplicationContext().getFilesDir();
        String base = filesDir.getPath();
        String hmmPath = base + "/efoffline/asr/hmm";

        asrConfig = new ASRConfig(hmmPath, true, base);
        asrEngine = ASREngineController.getInstance(getApplication()).setConfig(asrConfig);

        installAsrEngine();

//        testNeighborGrammarGenerator();

    }

    private List<String> testNeighborGrammarGenerator() {

        neighborGrammarGenerator = new NeighborGrammarGenerator(null, asrConfig.POCKETSPHINX_CFG_DEFAULT_DICT);

        ArrayList<String> list = new ArrayList<String>() {{
            add("how");
            add("are");
            add("you");
        }};

        return neighborGrammarGenerator.getSentenceNeighbors(list);
    }

    private void init() {
        sentences = (TextView) findViewById(R.id.sentences);
        result = (TextView) findViewById(R.id.result);
        startButton = (Button) findViewById(R.id.startButton);
        playButton = (Button) findViewById(R.id.playButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    asrEngine.stopRecording();
                    startButton.setText("Start");
                } else {
                    startButton.setText("Stop");
                    asrEngine.startRecording(correctItemArrayList, getParserRecorderMode(""));
                }

                isRecording = !isRecording;
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asrEngine.startPlayback(audioPath);
            }
        });
    }

    private int getParserRecorderMode(String templateCode) {
        int recorderMode = ASRConfig.RECORDER_MODE_RECORDER_ASR;
//        if (isASRTemplate(templateCode)) {
//            if (templateCode.equals("LngComp")) {
//                recorderMode = RECORDER_MODE_RECORDER_ASR;
//            } else {
//                recorderMode = RECORDER_MODE_JUST_ASR;
//            }
//        } else {
//            recorderMode = RECORDER_MODE_JUST_RECORDER;
//        }

        return recorderMode;
    }


    private void initasrEngine() {
        asrEngine.initAsrEngine(null).setListener(new ASRListener() {
            @Override
            public void onRecordComplete(String audio) {
                audioPath = audio;
                updateResultView("onRecordComplete, " + audioPath + '\n');
            }

            @Override
            public void onSuccess(final String correctItem) {
                updateResultView("onSuccess: " + correctItem.toString() + '\n');
            }

            @Override
            public void onError() {
                updateResultView("onError" + '\n');
            }

            @Override
            public void onPlaybackComplete() {
                updateResultView("onPlaybackComplete" + '\n');
            }
        });

        prepareSentencesContext();
    }

    private void updateResultView(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                result.append(s);
            }
        });
    }

    List<String> sentencesList;
    ArrayList<AsrCorrectItem> correctItemArrayList;

    private void prepareSentencesContext() {
        sentencesList = new ArrayList<String>();


        sentencesList = testNeighborGrammarGenerator();
        sentences.setText("");


        int i = 0;
        for (String s : sentencesList) {
            sentences.append(i + ": " + s + '\n');
            i++;
        }

        setContext();
    }

    private void setContext() {
        final ProgressDialog progressDialog = new ProgressDialog(asr_test.this);
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

                    asr_test.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        asrEngine.destroy();
    }

    private boolean installAsrEngine = false;

    private void installAsrEngine() {
        if (appPreference.isAsrPreInited()) {
            installAsrEngine = true;
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
                installAsrEngine = true;

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
}
