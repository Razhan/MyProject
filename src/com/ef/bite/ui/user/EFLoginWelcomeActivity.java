package com.ef.bite.ui.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ef.bite.AppConst;
import com.ef.bite.AppSession;
import com.ef.bite.R;
import com.ef.bite.Tracking.ContextDataMode;
import com.ef.bite.Tracking.MobclickTracking;
import com.ef.bite.business.GlobalConfigBLL;
import com.ef.bite.business.UserScoreBiz;
import com.ef.bite.business.task.LoginTask;
import com.ef.bite.business.task.PostExecuting;
import com.ef.bite.dataacces.mode.LoginMode;
import com.ef.bite.dataacces.mode.httpMode.HttpCourseRequest;
import com.ef.bite.dataacces.mode.httpMode.HttpLogin;
import com.ef.bite.model.ConfigModel;
import com.ef.bite.ui.BaseActivity;
import com.ef.bite.utils.AppLanguageHelper;
import com.ef.bite.utils.FontHelper;
import com.ef.bite.utils.JsonSerializeHelper;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import com.facebook.android.AsyncFacebookRunner;
//import com.facebook.android.DialogError;
//import com.facebook.android.Facebook;
//import com.facebook.android.FacebookError;

@SuppressWarnings("deprecation")
public class EFLoginWelcomeActivity extends BaseActivity {
    private ImageView mAppNameImg;
    private TextView mAppNameInfo;
    private Button mSignup;
    private Button mfacebook;
    private TextView login_in;
    private TextView login_main_not_registered;
    // LinearLayout mSignupLayout;
    private UserScoreBiz mUserScoreBiz;
    private ProgressDialog progress;
    private List<HttpCourseRequest> httpCourseRequests = new ArrayList<HttpCourseRequest>();
    private int flagout = 0;
    private final static int External_Home = 1;
    //    private Facebook facebook = new Facebook(AppConst.ThirdPart.Facebook_Login_Appkey);
    private String access_token;
    private long expires;
    private int curLoginTimes = 0;

    private boolean skip = false;
    private boolean show_level = false;
    private boolean show_phone = false;

    CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_eflogin_welcome);

        getKeyHash();

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("onSuccess", "onSuccess");
                String token = loginResult.getAccessToken().getToken();
                attemp2Login(token);
            }

            @Override
            public void onCancel() {
                Log.i("onCancel", "onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i("onError", exception.toString());

                if (exception instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                }
            }
        });


        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        String textString = JsonSerializeHelper.JsonLanguageDeserialize(
                mContext, "login_main_already_a_member");
        String[] texts = textString.split("<h>");
        mAppNameImg = (ImageView) findViewById(R.id.ef_welcome_app_name);
        try {
            login_main_not_registered = (TextView) findViewById(R.id.login_main_not_registered);
            login_main_not_registered.setText(texts[0]);
            login_in = (TextView) findViewById(R.id.login_main_loginin);
            login_in.setText(texts[1].replaceAll("</h>", ""));
            // mSignupLayout = (LinearLayout)
            // findViewById(R.id.eflogin_welcome_signup_layout);
        } catch (IndexOutOfBoundsException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        mfacebook = (Button) findViewById(R.id.eflogin_facebook_btn_login);
        mAppNameInfo = (TextView) findViewById(R.id.ef_welcome_app_info);
        mAppNameInfo.setText(JsonSerializeHelper.JsonLanguageDeserialize(
                mContext, "login_main_5_min_day"));
        mSignup = (Button) findViewById(R.id.eflogin_welcome_btn_login);
        mSignup.setText(JsonSerializeHelper.JsonLanguageDeserialize(mContext,
                "login_main_sign_up"));
        mUserScoreBiz = new UserScoreBiz(mContext);
        // translation
        if (AppLanguageHelper.getSystemLaunguage(mContext).equals(
                AppLanguageHelper.ZH_CN)) { // 中文
            mAppNameImg.setImageResource(R.drawable.ef_welcome_app_name_zh);
            mAppNameInfo.setTypeface(null, Typeface.BOLD);
            mAppNameInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
                    .getDimension(R.dimen.welcome_app_info_zh_size));
        } else if (AppLanguageHelper.getSystemLaunguage(mContext).equals(
                AppLanguageHelper.FR)) {
            mAppNameImg.setImageResource(R.drawable.ef_welcome_app_name_fr);
            mAppNameInfo.setTypeface(null, Typeface.NORMAL);
            mAppNameInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
                    .getDimension(R.dimen.welcome_app_info_en_size));
        } else { // 英文
            mAppNameImg.setImageResource(R.drawable.ef_welcome_app_name);
            mAppNameInfo.setTypeface(null, Typeface.NORMAL);
            mAppNameInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
                    .getDimension(R.dimen.welcome_app_info_en_size));
        }
        // Font setting
        login_in.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        FontHelper.applyFont(mContext, mAppNameInfo, FontHelper.FONT_OpenSans);
        // FontHelper.applyFont(mContext, mSignupLayout,
        // FontHelper.FONT_OpenSans);

        // 注册用户
        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mContext,
                                EFRegisterActivity.class),
                        AppConst.RequestCode.EF_REGISTER);
                overridePendingTransition(R.anim.activity_in_from_right,
                        R.anim.activity_out_to_left);
            }
        });

        login_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, EFLoginActivity.class);
                startActivityForResult(intent, AppConst.RequestCode.EF_LOGIN);
                overridePendingTransition(R.anim.activity_in_from_right,
                        R.anim.activity_out_to_left);
            }
        });

        mfacebook.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                AsyncFacebookRunner asyncFacebookRunner = new AsyncFacebookRunner(facebook);
//                if (facebook.isSessionValid()) {
//                    try {
//
//                    } catch (Exception e) {
//                        // TODO: handle exception
//                    }
//                }
//
//                facebook.authorize(EFLoginWelcomeActivity.this,
//                        new Facebook.DialogListener() {
//
//                            @Override
//                            public void onComplete(Bundle values) {
//                                // TODO Auto-generated method stub
//                                progress.setMessage(JsonSerializeHelper
//                                        .JsonLanguageDeserialize(mContext,
//                                                "loading_data"));
//                                Log.i("onComplete", "onComplete");
//                                // if (access_token != null) {
//                                // attemp2Login(access_token);
//                                // } else {
//                                progress.show();
//                                PreferencesUtils
//                                        .putString(
//                                                mContext,
//                                                AppConst.CacheKeys.Facebook_Access_Token,
//                                                facebook.getAccessToken());
//                                PreferencesUtils
//                                        .putLong(
//                                                mContext,
//                                                AppConst.CacheKeys.Facebook_Access_Expires,
//                                                facebook.getAccessExpires());
//                                attemp2Login(facebook.getAccessToken());
//                                // }
//
//                            }
//
//                            @Override
//                            public void onFacebookError(FacebookError e) {
//                                // TODO Auto-generated method stub
//                                e.printStackTrace();
//                            }
//
//                            @Override
//                            public void onError(DialogError e) {
//                                // TODO Auto-generated method stub
//                                e.printStackTrace();
//                            }
//
//                            @Override
//                            public void onCancel() {
//                                // TODO Auto-generated method stub
//                                Log.i("onCancel", "onCancel");
//                            }
//                        });
//                // }
//            }

            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(EFLoginWelcomeActivity.this, Arrays.asList("public_profile", "user_friends"));
//                startActivity(new Intent(mContext, ThirdPartyLogInActivity.class).putExtra("token", "123"));
            }
        });
    }

    private void getKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.ef.bite",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
//        MobclickTracking.UmengTrack.setPageStart(
//                ContextDataMode.ExternalHomeValues.pageNameValue,
//                ContextDataMode.ExternalHomeValues.pageSiteSubSectionValue,
//                ContextDataMode.ExternalHomeValues.pageSiteSectionValue,
//                mContext);

        BI_Tracking(External_Home);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
//        MobclickTracking.UmengTrack.setPageEnd(
//                ContextDataMode.ExternalHomeValues.pageNameValue,
//                ContextDataMode.ExternalHomeValues.pageSiteSubSectionValue,
//                ContextDataMode.ExternalHomeValues.pageSiteSectionValue,
//                mContext);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        facebook.authorizeCallback(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConst.RequestCode.EF_LOGIN
                && resultCode == AppConst.ResultCode.LOGIN_SUCCESS) {
            finish();
        }
        if (requestCode == AppConst.RequestCode.EF_REGISTER
                && resultCode == AppConst.ResultCode.REGISTER_SUCCESS)
            finish();
    }

    @Override
    public void onBackPressed() {
        if (flagout == 0) {
            Toast.makeText(
                    getBaseContext(),
                    JsonSerializeHelper.JsonLanguageDeserialize(mContext,
                            "app_exist_click_again"), Toast.LENGTH_SHORT)
                    .show();
            flagout = 1;
        } else {
            AppSession.getInstance().exit();
        }
    }

    @Override
    protected void BI_Tracking(int i) {
        switch (i) {
            case 1:
                MobclickTracking.OmnitureTrack.AnalyticsTrackState(
                        ContextDataMode.ExternalHomeValues.pageNameValue,
                        ContextDataMode.ExternalHomeValues.pageSiteSubSectionValue,
                        ContextDataMode.ExternalHomeValues.pageSiteSectionValue,
                        mContext);
                break;
        }
    }

    private void attemp2Login(final String access_token) {
        LoginTask loginTask = new LoginTask(mContext,
                new PostExecuting<HttpLogin>() {
                    @Override
                    public void executing(HttpLogin result) {
                        progress.dismiss();
                        if (result != null && result.status != null
                                && result.status.equals("0")) {
                            progress.setMessage(JsonSerializeHelper.JsonLanguageDeserialize(mContext, "loging_getting_profile"));

                            setParameter(result);

                            if (result.data.is_new_user || show_level || show_phone) {
//                            if (true) {
                                startActivity(new Intent(mContext, ThirdPartyLogInActivity.class)
                                                .putExtra("show_level", show_level)
                                                .putExtra("show_phone", show_phone)
                                                .putExtra("bella_id", result.data.bella_id)
                                );
                            } else {
                                //Marked as logined
                                AppConst.CurrUserInfo.UserId=result.data.bella_id;
                                AppConst.CurrUserInfo.IsLogin = true;
                                getUserProfile();
                            }
                        }
                    }
                });

        LoginMode loginModel = new LoginMode();
        loginModel.login_type = LoginTask.LOGIN_TYPE_FACEBOOK;
        loginModel.provider_type = LoginTask.LOGIN_PROVIDER_FACEBOOK;
        loginModel.access_token = access_token;
//        loginModel.access_token = "CAAL8DF8jGG0BABbLKanoMF3IesZBq1Iurc3FuxLF4Wa2BpZC8ivRcZABzvweAPnQZANVE0qHWhUMT2HZBVozfjOS5sIvsdiyaN9X3O9cIgT6MdZCD6oaLlZCPZAZB21ajtj4OyQ22PyKH4ZAgDMgTq1wqc5xnYPABOglZBhoUmsYu9ZA2nNlGgpBfdIAEwfzHb2CpUoZD";
        loginTask.execute(new LoginMode[]{loginModel});
    }

    private void setParameter(HttpLogin result) {
        GlobalConfigBLL configbll = new GlobalConfigBLL(mContext);
        ConfigModel appConfig = configbll.getConfigModel();

        if (appConfig == null ) {
            skip = false;
        } else {
            skip = appConfig.SkipPhoneNum;
        }

        if (!skip && (result.data.phone == null || result.data.phone.equals(""))) {
            show_phone = true;
        }

        if (result.data.plan_id == null || result.data.plan_id.equals("")) {
            show_level = true;
        }
    }
}
