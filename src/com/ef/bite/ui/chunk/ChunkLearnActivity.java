package com.ef.bite.ui.chunk;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.*;
import android.view.animation.Animation.AnimationListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.ef.bite.AppConst;
import com.ef.bite.R;
import com.ef.bite.Tracking.ContextDataMode;
import com.ef.bite.Tracking.MobclickTracking;
import com.ef.bite.adapters.ChunkLearnListAdapter;
import com.ef.bite.animation.ResizeAnimation;
import com.ef.bite.business.ChunkBLL;
import com.ef.bite.dataacces.TutorialConfigSharedStorage;
import com.ef.bite.dataacces.mode.Chunk;
import com.ef.bite.dataacces.mode.PresentationConversation;
import com.ef.bite.logger.LogLevel;
import com.ef.bite.logger.Logger;
import com.ef.bite.utils.FontHelper;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.widget.AudioPlayerView;
import com.ef.bite.widget.AudioPlayerView.OnCompletionListener;
import com.ef.bite.widget.AudioPlayerView.OnStopClickListener;
import com.ef.bite.widget.ExpandButtonView;
import com.ef.bite.widget.FlexiListView;

import java.util.ArrayList;
import java.util.List;

public class ChunkLearnActivity extends BaseChunkActivity {

	private AudioPlayerView mWeChatPlayer;
	int maxDiameter = 0; // 音频播放器最大的直径
	int minDiameter = 0; // 音频播放器最小的直径
	private RelativeLayout mTotalLayout;
	private ImageView mTeacher;
	TextView mInfoText;
	ChunkLearnListAdapter mDialogueAdapter;
	private RelativeLayout mSlideTopLayout;
	private RelativeLayout mSlideMidLayout;
    FlexiListView mDialogueListView;
	private LinearLayout mSlideBottomLayout;
	ChunkBLL mChunkBll;

	private int mExpanded = 0; // 0 - 未展开； 1 - 正在展开； 2 - 已展开
	AudioPlayerView anim_player;
	private ViewGroup anim_mask_layout;// 动画层
	int[] start_location = new int[2];
	int[] end_location = new int[2];
	int start_width = 0;
	int start_height = 0;
	int end_width = 0;
	int end_height = 0;
	final static int ANIMATION_DURATION = 500;
	int topSlideLayoutHeight = 0; // 上层高度
	int bottomSlideLayoutHeight = 0; // 下层的高度
	int expandButtonHeight = 0; // expandbutton的高度
	private int mposition = 0;
	boolean isChunkLearning = true;
	private final static int LearnAudioDialogueValues = 1;
	private final static int LearnAudioDialogueLookUpValues = 2;

	private TutorialConfigSharedStorage configSharedStorage;
    private List<PresentationConversation> mConversation;

    private final int REFRESH = -1;
    private final int PAUSE = -2;
    private final int RESUME = -3;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mChunkModel = (Chunk) getSerializableExtra(AppConst.BundleKeys.Chunk);
		// Bundle bundle = getIntent().getExtras();
		// if (bundle != null && bundle.containsKey(AppConst.BundleKeys.Chunk))
		// {
		// String json = bundle.getString(AppConst.BundleKeys.Chunk);
		// mChunkModel = new Chunk();
		// mChunkModel.parse(json);
		// }

		mChunkBll = new ChunkBLL(mContext);
		AppConst.GlobalConfig.IsChunkPerDayLearnLoaded = true;

		configSharedStorage = new TutorialConfigSharedStorage(mContext,
				AppConst.CurrUserInfo.UserId);

        mBottomPlayer.init(getStartTImeList(mChunkModel),
                new AdudioCallBack() {
                    @Override
                    public void postExec(String str, int pos) {
                        if (pos == REFRESH) {
                            mConversation.clear();
                            return;
                        } else if (pos == RESUME) {
                            mDialogueAdapter.setCloseAllGif(false);
                            mDialogueAdapter.setisClickEvent(false);
                            mDialogueAdapter.notifyDataSetChanged();
                            return;
                        } else if (pos == PAUSE) {
                            mDialogueAdapter.closeGif();
                            return;
                        } else {
                            mConversation.add(mChunkModel.getChunkPresentation().getPresentationConversations().get(pos));
                        }

                        mDialogueAdapter.setDisplayWithAnimation(false);
                        mDialogueAdapter.setCloseAllGif(false);
                        mDialogueAdapter.notifyDataSetChanged();
                        scrollMyListViewToBottom();

//						Logger.init("test")
//								.setMethodCount(2)
//              					.hideThreadInfo()
//								.setLogLevel(LogLevel.FULL);
//						Logger.d("hello");
                    }
                }
        );


        new Handler().postDelayed(new Runnable() {
            public void run() {
                mBottomPlayer.performClick();
            }
        }, 400);

	}

    private List<Integer> getStartTImeList(Chunk chunk) {
        if (chunk == null) {
            return null;
        }

        List<Integer> res = new ArrayList<Integer>();
        List<PresentationConversation> conversation = chunk.getChunkPresentation().getPresentationConversations();
        for (int i = 0; i < conversation.size(); i++) {
            res.add(conversation.get(i).getStartTime());
        }

        res.add(conversation.get(conversation.size() - 1).getEndTime());

        return res;
    }

    private void scrollMyListViewToBottom() {
        mDialogueListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mDialogueListView.smoothScrollToPosition(mConversation.size() - 1);
            }
        });
    }

    @Override
    protected void showTutorial() {
        initComponents();
    }

    @Override
	protected void onResume() {
		super.onResume();
		if (configSharedStorage.get() != null) {
			if (configSharedStorage.get().Tutorial_Learn_Chunk) {
				if (isChunkForPreview || isChunkLearning) {
					BI_Tracking(LearnAudioDialogueValues);
				} else {
					BI_Tracking(LearnAudioDialogueLookUpValues);
				}
			}
		}
	}

	@Override
	protected void setLayoutResourceId() {
		setContentView(R.layout.activity_chunk_learn_wechat);
	}

	@Override
	protected void initComponents() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null
				&& bundle.containsKey(AppConst.BundleKeys.Is_Chunk_Learning)) {
			isChunkLearning = bundle
					.getBoolean(AppConst.BundleKeys.Is_Chunk_Learning);
		}

		// 方便测试，后期改过来
		// if (isChunkLearning) {
		// enableNext(false);
		// }

		progressbar.setVisibility(View.VISIBLE);
		progressbar.init(5, 1);

		mTotalLayout = (RelativeLayout) findViewById(R.id.chunk_presentation_layout);
		mTeacher = (ImageView) findViewById(R.id.chunk_actionbar_teacher);
		mInfoText = (TextView) findViewById(R.id.chunk_lesson_info);
		if (JsonSerializeHelper.JsonLanguageDeserialize(mContext,
				"chunk_learn_lets_learn_new_chunk") != null) {
			mInfoText.setText(JsonSerializeHelper.JsonLanguageDeserialize(
					mContext, "chunk_learn_lets_learn_new_chunk"));
		}

		mSlideTopLayout = (RelativeLayout) findViewById(R.id.chunk_lesson_side_top_layout);
		mSlideMidLayout = (RelativeLayout) findViewById(R.id.chunk_lesson_slide_mid_layout);
		mSlideBottomLayout = (LinearLayout) findViewById(R.id.chunk_lesson_side_bottom_layout);
		mWeChatPlayer = new AudioPlayerView(mContext);

        mWeChatPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void OnCompletion() {
                mDialogueAdapter.closeGif();
            }
        });

        mDialogueListView = (FlexiListView) findViewById(R.id.chunk_lesson_dialogue_listview);
		mBottomPlayer.setMiniStatus(true);

        mConversation = new ArrayList<PresentationConversation>();
        if (mChunkModel != null && mChunkModel.getChunkPresentation() != null)
			mDialogueAdapter = new ChunkLearnListAdapter(this, mConversation, mChunkModel);
		mDialogueListView.setAdapter(mDialogueAdapter);

		// 这个版本暂时不支持对话来回切换
		mDialogueListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									final int position, long id) {
				// made in Alan
				mposition = position;
				PresentationConversation conversation = mChunkModel
						.getChunkPresentation().getPresentationConversations()
						.get(position);
				Integer startTime = conversation.getStartTime();
				Integer endTime = conversation.getEndTime();
				mDialogueAdapter.initGif(position);
				if (startTime != null && endTime != null) {

					mWeChatPlayer.start(startTime);
					mWeChatPlayer.stop(endTime, new OnStopClickListener() {

                        @Override
                        public void OnStop() {
                            mDialogueAdapter.closeGif();
                        }
					});

					mBottomPlayer.pause();
				}

//				mDialogueAdapter.closeGif();
				// mDialogueAdapter.setTranslationMorn(true, position);
			}
		});

		mGoBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MobclickTracking.OmnitureTrack.ActionDialogue(1);
//				MobclickTracking.UmengTrack.ActionDialogue(1, mContext);
				finish();
//				if (isChunkForPreview) {
//					finish();
//				} else
//					new HomeScreenOpenAction().open(mContext, null);
			}
		});

//		mSlideMidLayout.getViewTreeObserver().addOnGlobalLayoutListener(
//				new OnGlobalLayoutListener() {
//					@SuppressWarnings("deprecation")
//					@Override
//					public void onGlobalLayout() {
//						topSlideLayoutHeight = mSlideTopLayout.getHeight();
//						bottomSlideLayoutHeight = mSlideBottomLayout
//								.getHeight();
//						mSlideMidLayout.getViewTreeObserver()
//								.removeOnGlobalLayoutListener(this);
//						// 设置Expand Button的位置
//						expandButtonHeight = mExpandButton.getHeight();
//						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mExpandButton
//								.getLayoutParams();
//						params.topMargin = topSlideLayoutHeight
//								+ expandButtonHeight / 2;
//						mExpandButton.setLayoutParams(params);
//						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//							mSlideMidLayout.getViewTreeObserver()
//									.removeGlobalOnLayoutListener(this);
//						} else {
//							mSlideMidLayout.getViewTreeObserver()
//									.removeOnGlobalLayoutListener(this);
//						}
//					}
//				});

		if (mChunkModel != null && mChunkModel.getChunkPresentation() != null) {
			if (mChunkModel.getIsPreinstall()) {

				mBottomPlayer.prepareInAsset(mChunkModel.getChunkPresentation()
						.getAudioFile());
				mWeChatPlayer.prepareInAsset(mChunkModel.getChunkPresentation()
						.getAudioFile());
			} else {

				mBottomPlayer.prepareInStorage(mChunkModel
						.getChunkPresentation().getAudioFile());
				mWeChatPlayer.prepareInStorage(mChunkModel
						.getChunkPresentation().getAudioFile());
			}
		}

		mBottomPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void OnCompletion() {
                mDialogueAdapter.closeGif();
                enableNext(true);
				mDialogueAdapter.setHighLight(true);
			}
		});

		mBottomPlayer.setOnCloseAudioListener(new AudioPlayerView.OnCloseAudioListener() {

					@Override
					public void OnClose() {
                        // TODO Auto-generated method stub
						mWeChatPlayer.pause();
						//mDialogueAdapter.closeTranslationGif(true);
					}
				});

		// 字体设置
		FontHelper.applyFont(mContext, mInfoText, FontHelper.FONT_Museo300);
	}

	@Override
	protected void impNextClick() {
		if (isChunkForPreview) {
			Intent intent = new Intent(mContext, ChunkLearnDetailActivity.class);
			intent.putExtra(AppConst.BundleKeys.Chunk, mChunkModel);
			intent.putExtra(AppConst.BundleKeys.Is_Chunk_For_Preview, true);
			startActivityForResult(intent, AppConst.RequestCode.CHUNK_PREVIEW);
			overridePendingTransition(R.anim.activity_in_from_right,
					R.anim.activity_out_to_left);
		} else if (isChunkLearning) {
			Intent intent = new Intent(mContext, ChunkLearnDetailActivity.class);
			intent.putExtra(AppConst.BundleKeys.Chunk, mChunkModel);
			intent.putExtra(AppConst.BundleKeys.Is_Chunk_Learning, true);
			startActivityForResult(intent, AppConst.RequestCode.CHUNK_LEARNING);
			overridePendingTransition(R.anim.activity_in_from_right,
					R.anim.activity_out_to_left);
		} else {
			finish();
			overridePendingTransition(R.anim.activity_in_from_right,
					R.anim.activity_out_to_left);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
        mDialogueAdapter.closeGif();
        if (mBottomPlayer.getStatus() == 1)
			mBottomPlayer.pause();
		if (mWeChatPlayer != null) {
			mWeChatPlayer.pause();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBottomPlayer!=null || mBottomPlayer.getStatus() == 1 || mBottomPlayer.getStatus() == 2)
			mBottomPlayer.stop();
		if(mWeChatPlayer!=null){
			mWeChatPlayer.release();
		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	protected void BI_Tracking(int i) {
		// TODO Auto-generated method stub
		super.BI_Tracking(i);
		switch (i) {
		case 1:
			MobclickTracking.OmnitureTrack
					.AnalyticsTrackState(
							ContextDataMode.LearnAudioDialogueValues.pageNameValue,
							ContextDataMode.LearnAudioDialogueValues.pageSiteSubSectionValue,
							ContextDataMode.LearnAudioDialogueValues.pageSiteSectionValue,
							mContext);
			break;

		case 2:
			MobclickTracking.OmnitureTrack
					.AnalyticsTrackState(
							ContextDataMode.LearnAudioDialogueLookUpValues.pageNameValue,
							ContextDataMode.LearnAudioDialogueLookUpValues.pageSiteSubSectionValue,
							ContextDataMode.LearnAudioDialogueLookUpValues.pageSiteSectionValue,
							mContext);
			break;
		}
	}

    public interface AdudioCallBack {
        void postExec(String str, int pos);
    }

}
