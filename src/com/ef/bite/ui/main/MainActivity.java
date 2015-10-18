package com.ef.bite.ui.main;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;


import com.ef.bite.AppConst;
import com.ef.bite.AppSession;
import com.ef.bite.R;
import com.ef.bite.business.LocalDashboardBLL;
import com.ef.bite.business.TutorialConfigBiz;
import com.ef.bite.business.UserScoreBiz;
import com.ef.bite.business.action.UserProfileOpenAction;
import com.ef.bite.business.task.*;
import com.ef.bite.dataacces.AchievementCache;
import com.ef.bite.dataacces.mode.PushData;
import com.ef.bite.dataacces.mode.httpMode.HttpDashboard;
import com.ef.bite.dataacces.mode.httpMode.HttpGetFriendData;
import com.ef.bite.model.ProfileModel;
import com.ef.bite.ui.BaseActivity;
import com.ef.bite.ui.DashBoardFriendView;
import com.ef.bite.ui.user.FriendNotificationActivity;
import com.ef.bite.ui.user.LeaderBoardActivity;
import com.ef.bite.ui.user.SettingsActivity;
import com.ef.bite.utils.JsonSerializeHelper;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MainScreen for dashboard
 */
public class MainActivity extends BaseActivity {
    private final String TAG = MainActivity.class.getSimpleName();

	private ImageButton mSettingBtn;
	private RelativeLayout inboxView;
	private LinearLayout mFriendLayout; // 底部朋友layout
	private FriendsContainer mFriendContainer;
	private ImageButton mFirendMore;
	private TextView home_screen_leaderboard_title;
    private ScrollView scrollView;
    private LinearLayout chunkInfo;
    private LinearLayout leaderBoard;
    private WebView webView;
    private View inflatedView;

    private LocalDashboardBLL dashboardBLL;
	private UserScoreBiz mScoreBiz;
	private HttpGetFriendData currentUserSelected;
	private List<HttpGetFriendData> mFriendList = new ArrayList<HttpGetFriendData>();
	private int masteredChunkNum;
	// 通知
	private RelativeLayout mNotificationLayout; // 通知的layout
	private TextView mNotificationCount; // 通知未读数量

	private FragmentManager mFragmentManager;

	private List<Fragment> fragments = new ArrayList<Fragment>();

	private LinearLayout mPhraseLayout;
	private int currentIndex;
    private ProgressDialog progress;

    private boolean interrupt;
    private int resumeTimes;
    private HttpDashboard mhttpDashboard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();

        TutorialConfigBiz tutorialBiz = new TutorialConfigBiz(mContext);
        interrupt = tutorialBiz.interrupt(TutorialConfigBiz.TUTORIAL_TYPE_LERN_CHUNK);

        setContentView(R.layout.activity_home_screen);
		setupViews();
		saveUserProfileForPush();

//        if (FileUtils.ExistSDCard() && (FileUtils.getSDFreeSize() >= 10)) {
//            showUpdateDialog();
//        }
	}

	private void init(){
		dashboardBLL = new LocalDashboardBLL(mContext);
		mScoreBiz = new UserScoreBiz(mContext);

        interrupt = false;
        resumeTimes = 0;
	}

	private void setupViews() {
		mSettingBtn = (ImageButton) findViewById(R.id.home_screen_setting);
		inboxView = (RelativeLayout) findViewById(R.id.inbox_view);
		mFriendLayout = (LinearLayout) findViewById(R.id.home_screen_friend_layout);
		mFirendMore = (ImageButton) findViewById(R.id.home_screen_friend_more);
		mFriendContainer = new FriendsContainer();
		mNotificationLayout = (RelativeLayout) findViewById(R.id.home_screen_notification_layout);
		mNotificationCount = (TextView) findViewById(R.id.home_screen_notification_count);
		mPhraseLayout = (LinearLayout) findViewById(R.id.home_screen_chunk_layout);
		home_screen_leaderboard_title = (TextView) findViewById(R.id.home_screen_leaderboard_title);
		home_screen_leaderboard_title.setText(JsonSerializeHelper
				.JsonLanguageDeserialize(mContext,
						"home_screen_leaderboard_title"));
//		FontHelper.applyFont(mContext, layout, FontHelper.FONT_Museo300);
		// 好友通知
		inboxView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext,
                        FriendNotificationActivity.class));
			}
		});

		mFirendMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, LeaderBoardActivity.class));
				MainActivity.this.overridePendingTransition(
                        R.anim.activity_in_from_right,
                        R.anim.activity_out_to_left);


			}
		});
		mSettingBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, SettingsActivity.class));
			}
		});
		mPhraseLayout = (LinearLayout)findViewById(R.id.home_screen_chunk_layout);

        scrollView = (ScrollView)findViewById(R.id.home_screen_scrollview);
        chunkInfo = (LinearLayout)findViewById(R.id.home_screen_chunk_layout);
        leaderBoard = (LinearLayout)findViewById(R.id.home_screen_leaderboard);
        webView = (WebView) findViewById(R.id.home_screen_webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });

        webView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(mContext, AdvertisementActivity.class);
                it.putExtra("target_url", mhttpDashboard.data.banners.get(0).getTarget_url());
                startActivity(it);
            }
        });

        initFragment();
		switchFragment(3);
	}

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int scrollViewHeight;

        scrollViewHeight = scrollView.getMeasuredHeight();
        LinearLayout.LayoutParams lp_board = (LinearLayout.LayoutParams) leaderBoard.getLayoutParams();

        if (mhttpDashboard.data.new_lesson_count > 0 || mhttpDashboard.data.new_rehearsal_count > 0) {
            webView.setVisibility(View.GONE);
            chunkInfo.getLayoutParams().height = scrollViewHeight / 3 * 2 - lp_board.topMargin;
            leaderBoard.getLayoutParams().height = scrollViewHeight / 3 * 1;
        } else {
            String URL = mhttpDashboard.data.banners.get(0).getImage_url();
            String html = "<html><body><img src=\"" + URL + "\" width=\"100%\" height=\"auto\" vertical-align=\"center\"\"/></body></html>";
            webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
            webView.setVisibility(View.VISIBLE);
            //banner size: 475 * 224
            webView.getLayoutParams().height = scrollView.getWidth() / 2;

            chunkInfo.getLayoutParams().height = scrollViewHeight / 3 * 2;
            leaderBoard.getLayoutParams().height = scrollViewHeight / 3 * 1;
        }
        scrollView.setVisibility(View.VISIBLE);
        scrollView.requestLayout();
    }


	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
        resumeTimes++;
        super.onResume();
//		updateDashboard(dashboardCache.load());
		postUserAchievement();
	}

	private void  initFragment() {
		fragments.clear();
		fragments.add(new LearnFragment());
		fragments.add(new PracticeFragment());
		fragments.add(new AllDoneFragmentMore());
        fragments.add(new AllDoneFragmentNothing());
    }

	private void switchFragment(int index) {
		if (null == mFragmentManager) {
			mFragmentManager = getSupportFragmentManager();
		}
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		for (int i = 0; i < fragments.size(); i++) {
			Fragment fragment = fragments.get(i);
			if (i == index) {
				if (!fragment.isAdded()) {
					ft.add(R.id.home_screen_chunk_layout, fragment);
				}
				ft.show(fragment);//控制所选fragment显示
				currentIndex = index;
			} else {
				if (fragment.isAdded()) {
					ft.hide(fragment);
				}else {
					ft.add(R.id.home_screen_chunk_layout,fragment);
				}
			}
		}
		ft.commitAllowingStateLoss();
	}

	private long lastExitTime = 0;
	@Override
	public void onBackPressed() {
		long nowTime = new Date().getTime();
		if (nowTime - lastExitTime > 1000) { // 1秒
			lastExitTime = nowTime;
			Toast.makeText(
					getBaseContext(),
					JsonSerializeHelper.JsonLanguageDeserialize(mContext,
							"app_exist_click_again"), Toast.LENGTH_SHORT)
					.show();

			if (AppConst.CurrUserInfo.IsLogin == true) {

			}
		} else {
			AppConst.CurrUserInfo.IsLogin = false;
			AppSession.getInstance().exit();
		}
	}

	/**
	 * execute action with different type
	 */
	private void executeAction() {
		PushData pushData = getPushData();
		if (pushData == null) {
			return;
		}
		switch (pushData.getType()) {
			case new_lesson:
				((BaseDashboardFragment)fragments.get(currentIndex)).getNextButton().performClick();
				break;
			case new_rehearsal:
                if (currentIndex == 1) {
                    ((BaseDashboardFragment) fragments.get(currentIndex)).getNextButton().performClick();
                }
				break;
			case recording_rate:
				inboxView.performClick();
				break;
			default:
				break;
		}
	}

	/**
	 * send user id to Parse Server
	 */
	public void saveUserProfileForPush() {
		if (!AppConst.CurrUserInfo.IsLogin) {
			return;
		}
		ParseInstallation.getCurrentInstallation().put("bella_id",
				AppConst.CurrUserInfo.UserId);
		ParseInstallation.getCurrentInstallation().put("device_id",
				AppConst.GlobalConfig.DeviceID);
		ParseInstallation.getCurrentInstallation().saveInBackground(
                new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            e.printStackTrace();
                        } else {
                        }
                    }
                });
	}

	/**
	 * 最下排朋友内容
	 */
	class FriendsContainer {

		List<DashBoardFriendView> friendsListViews = new ArrayList<DashBoardFriendView>();
		LinearLayout mFriendLayout;

		public void initialize(LinearLayout friendLayout) {
			int friendNum = 0;
			if (mFriendLayout != null) {
				clear();
			}
			if (mFriendList != null && mFriendList.size() > 0) {
				for (int index = 0; index < (mFriendList.size() > 4 ? 4
						: mFriendList.size()); index++) {
					final DashBoardFriendView friendView = new DashBoardFriendView(
							mContext);
					View view = friendView.getView(mFriendList.get(index));
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							0, LinearLayout.LayoutParams.MATCH_PARENT);
					params.weight = 1;
					friendLayout.addView(view, params);
					friendView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (friendView.isSelected() == false) {
								HttpGetFriendData profile = friendView
										.getProfile();
								currentUserSelected = profile;
								openProfile(profile);
							}
						}
					});
					friendsListViews.add(friendView);
					friendNum++;
				}
			}
			for (int index = friendNum; index < 4; index++) {
				DashBoardFriendView inviteView = new DashBoardFriendView(
						mContext);
				View view = inviteView.getInviteView();
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						0, LinearLayout.LayoutParams.MATCH_PARENT);
				params.weight = 1;
				friendLayout.addView(view, params);
			}

			mFriendLayout = friendLayout;
		}

		/**
		 * clear friend dashboard layout *
		 */
		public void clear() {
			if (mFriendLayout != null)
				mFriendLayout.removeAllViews();
			friendsListViews.clear();
		}

		/**
		 * Open the profile with data *
		 */
		public void openProfile(HttpGetFriendData data) {
			if (data == null)
				return;
			ProfileModel profile = null;
			if (data.bella_id.equals(AppConst.CurrUserInfo.UserId)) {
				// myself
				profile = new ProfileModel(AppConst.CurrUserInfo.UserId,
						AppConst.CurrUserInfo.Alias,
						AppConst.CurrUserInfo.Avatar,
						mScoreBiz.getUserScore(),
						masteredChunkNum, data.friend_count, false);
				AppConst.CurrUserInfo.Avatar = data.avatar;
			} else
				// one friend
				profile = new ProfileModel(currentUserSelected.bella_id,
						currentUserSelected.alias, currentUserSelected.avatar,
						currentUserSelected.score, masteredChunkNum,
						currentUserSelected.friend_count, true);
			profile.IsOpenFromHomeScreen = true;
			new UserProfileOpenAction().open(mContext, profile);

		}
	}

	/**
	 * Get dashboard info
	 */
	public void getDashboard() {
		GetDashboardTask task = new GetDashboardTask(mContext,
				new PostExecuting<HttpDashboard>() {
					@Override
					public void executing(HttpDashboard httpDashboard) {
						if ( httpDashboard != null && httpDashboard.data !=null) {
                            mhttpDashboard = httpDashboard;

							dashboardCache.save(httpDashboard);
							updateDashboard(httpDashboard);
                            progress.dismiss();
                            executeAction();
                        }
					}
				});
		task.execute();
	}

	private void postUserAchievement() {
        progress = ProgressDialog.show(MainActivity.this, null, getResources().getString(R.string.loading_data));
        progress.setIndeterminate(false);
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(false);

		AchievementCache.getInstance().postCache(new AchievementCache.OnFinishListener() {
            @Override
            public void doOnfinish(boolean result) {
                getDashboard();
            }
        });
	}

	private void updateDashboard(HttpDashboard httpDashboard){
		if(httpDashboard == null || httpDashboard.data==null){
			return;
		}

		mNotificationLayout.setVisibility(httpDashboard.data.inbox_count > 0 ? View.VISIBLE : View.GONE);
		mNotificationCount.setText(httpDashboard.data.inbox_count + "");
		masteredChunkNum=httpDashboard.data.master_count;
		updateFragment(httpDashboard);
		//show friends list
		convertFriends(httpDashboard.data.rank_friends);
		mFriendContainer.initialize(mFriendLayout);

	}

    private void updateFragment(HttpDashboard httpDashboard){
        int index;
		//switch fragment by states
		if(httpDashboard.data.new_lesson_count > 0){
            index = 0;
		} else if (httpDashboard.data.new_rehearsal_count > 0){
            index = 1;
		} else if (httpDashboard.data.unlock_enabled) {
            index = 2;
		} else {
            index = 3;
        }

        List<Fragment> fragmentList =this.getSupportFragmentManager().getFragments();
        ((BaseDashboardFragment) fragmentList.get(index)).update(httpDashboard);
        switchFragment(index);

        if (resumeTimes == 2 && interrupt && currentIndex == 0) {
            ((BaseDashboardFragment)fragments.get(currentIndex)).getNextButton().performClick();
        }
    }


	private void convertFriends(List<HttpDashboard.friend> friends){
		mFriendList.clear();
		for (HttpDashboard.friend friend : friends) {
			HttpGetFriendData friendData = new HttpGetFriendData();
			friendData.alias = friend.alias;
			friendData.avatar = friend.avatar_url;
			friendData.bella_id = friend.bella_id;
			friendData.friend_count = friend.friend_count;
			friendData.rank = friend.rank;
			friendData.score = friend.score;
			mFriendList.add(friendData);
		}

	}

	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("检测到新版本");
		builder.setMessage("是否下载更新?");
		builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				final String appName = "EnglishBite.apk";
				final String downUrl = "http://gdown.baidu.com/data/wisegame/bd47bd249440eb5f/shenmiaotaowang2.apk";

                intoDownloadManager(downUrl, appName, "Download");
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		builder.show();
	}


    private void intoDownloadManager(String url, String appName, String path) {

        DownloadManager dManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        Uri uri = Uri.parse(url);

        File app = new File(android.os.Environment.getExternalStorageDirectory() + File.separator +
                path + File.separator + appName);

        try {
            if (app.exists()) {
                app.delete();
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir(path, appName);
        request.setDescription("EnglishBite新版本下载");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");
        request.allowScanningByMediaScanner();
        request.setVisibleInDownloadsUi(true);

        long refernece = dManager.enqueue(request);

        SharedPreferences sPreferences = getSharedPreferences("downloadAPK", 0);

        sPreferences.edit().putLong("bite", refernece).commit();
    }

}