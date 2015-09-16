package com.ef.bite.ui.user;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ef.bite.AppConst;
import com.ef.bite.R;
import com.ef.bite.Tracking.ContextDataMode;
import com.ef.bite.Tracking.MobclickTracking;
import com.ef.bite.business.GlobalConfigBLL;
import com.ef.bite.business.task.GetProfileTask;
import com.ef.bite.business.task.LoginTask;
import com.ef.bite.business.task.PostExecuting;
import com.ef.bite.business.task.UpdateUserProfile;
import com.ef.bite.dataacces.mode.LoginMode;
import com.ef.bite.dataacces.mode.httpMode.HttpBaseMessage;
import com.ef.bite.dataacces.mode.httpMode.HttpLogin;
import com.ef.bite.model.ConfigModel;
import com.ef.bite.ui.BaseActivity;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.utils.ListUtils;
import com.ef.bite.widget.ActionbarLayout;
import com.ef.bite.widget.LoginInputLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.util.PreferencesUtils;

public class ThirdPartyLogInActivity extends BaseActivity {
    private TextView mPhoneInput;
    private Spinner level_spinner;
    private Button mNextBtn;
    private Button mNextBtn2;
    private ActionbarLayout mActionbar;
    private int mPositionLevel;
    private String mLevelChoice;
    private LinearLayout mLevelLayout;
    private LinearLayout mPhoneLayout;
    private TextView mSkip;
    private int step = 1;

    private final boolean chooseLevel = (AppConst.GlobalConfig.StudyPlans.size() > 1) ? true : false;
    private String bella_id = "";
    private boolean show_level = true;
    private boolean show_phone = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_party_log_in);

        bella_id = getIntent().getStringExtra("bella_id");
        show_level = getIntent().getBooleanExtra("show_level", true);
        show_phone = getIntent().getBooleanExtra("show_phone", true);


        mPhoneInput = (TextView)findViewById(R.id.ThirdParty_login_ef_phone);
        level_spinner = (Spinner)findViewById(R.id.ThirdParty_login_level_spinner);
        mNextBtn = (Button)findViewById(R.id.ThirdParty_login_ef_btn_next);
        mNextBtn2 = (Button)findViewById(R.id.ThirdParty_login_ef_btn_next2);

        mActionbar = (ActionbarLayout) findViewById(R.id.ThirdParty_login_ef_actionbar);
        mLevelLayout = (LinearLayout)findViewById(R.id.ThirdParty_login_ef_level_layout);
        mPhoneLayout = (LinearLayout)findViewById(R.id.ThirdParty_login_ef_phone_layout);
        mSkip = (TextView)findViewById(R.id.ThirdParty_login_ef_phone_skip);
        mNextBtn.setEnabled(false);

        choosePagesToShow();

        mPhoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNextBtn2.setEnabled(true);
            }
        });

        mActionbar.initiWithTitle(JsonSerializeHelper.JsonLanguageDeserialize(mContext, "register_ef_register_title"),
                R.drawable.arrow_goback_black, -1, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (step == 2) {
                            attempBack();
                        } else {
                            finish();
                        }
                        overridePendingTransition(R.anim.activity_in_from_left, R.anim.activity_out_to_right);
                    }
                }, null);

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(mLevelChoice, null, new CallBackInterface(){
                    @Override
                    public void exectueMethod(){
                        if (!show_phone) {
                            updateSccess(bella_id);
                        }
                    }
                });

                if (show_phone) {
                    attempNext();
                }
            }
        });

        mNextBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(null, mPhoneInput.getText().toString(), new CallBackInterface() {
                    @Override
                    public void exectueMethod() {
                        updateSccess(bella_id);
                    }
                });
            }
        });

        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(null, null, new CallBackInterface() {
                    @Override
                    public void exectueMethod() {
                        GlobalConfigBLL configbll = new GlobalConfigBLL(mContext);
                        ConfigModel appConfig = configbll.getConfigModel();
                        if (appConfig == null) {
                            appConfig = new ConfigModel();
                        }
                        appConfig.SkipPhoneNum = true;
                        configbll.setConfigModel(appConfig);

                        updateSccess(bella_id);
                    }
                });
            }
        });

        SetupSpinner();
    }

    private void choosePagesToShow() {
        if (!show_level && show_phone) {
            mLevelLayout.setVisibility(View.GONE);
            mPhoneLayout.setVisibility(View.VISIBLE);
        } else if (show_level && !chooseLevel) {
            studyPlan_OneChoice();
        }
    }

    private void studyPlan_OneChoice() {
        mLevelLayout.setVisibility(View.GONE);
        mPhoneLayout.setVisibility(View.VISIBLE);
        mLevelChoice = AppConst.GlobalConfig.StudyPlans.get(0);

        updateProfile(mLevelChoice, null, new CallBackInterface(){
            @Override
            public void exectueMethod(){
                if (!show_phone) {
                    updateSccess(bella_id);
                }
            }
        });
    }

    private void updateSccess(String id) {
        AppConst.CurrUserInfo.UserId=id;
        AppConst.CurrUserInfo.IsLogin = true;
        getUserProfile();
    }

    private void attempNext() {

        Animation fadeout = AnimationUtils.loadAnimation(mContext,
                R.anim.activity_out_to_left);
        fadeout.setDuration(200);
        fadeout.setFillAfter(true);
        Animation fadein = AnimationUtils.loadAnimation(mContext,
                R.anim.activity_in_from_right);
        fadein.setDuration(200);
        fadein.setFillAfter(true);
        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mPhoneLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLevelLayout.setVisibility(View.GONE);
            }
        });
        mLevelLayout.startAnimation(fadeout);
        mPhoneLayout.startAnimation(fadein);
        step++;

    }

    private void attempBack() {
        Animation fadeout = AnimationUtils.loadAnimation(mContext,
                R.anim.activity_out_to_right);
        fadeout.setDuration(200);
        fadeout.setFillAfter(true);
        Animation fadein = AnimationUtils.loadAnimation(mContext,
                R.anim.activity_in_from_left);
        fadein.setDuration(200);
        fadein.setFillAfter(true);
        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPhoneLayout.setVisibility(View.GONE);
                mLevelLayout.setVisibility(View.VISIBLE);
            }
        });
        mPhoneLayout.startAnimation(fadeout);
        mLevelLayout.startAnimation(fadein);
        step--;
    }

    @Override
    public void onBackPressed() {
        if (step == 2) {
            attempBack();
        } else {
            super.onBackPressed();
        }
    }

    private void SetupSpinner() {

        List<String> valueset = new ArrayList<String>();
        valueset = ListUtils.getValues(AppConst.GlobalConfig.StudyPlansMap, true);

        final ArrayAdapter<String> adapter_level = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valueset);
        adapter_level.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        level_spinner.setAdapter(adapter_level);
        level_spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                mPositionLevel = position - 1;
                if (mPositionLevel > -1) {
                    mLevelChoice = AppConst.GlobalConfig.StudyPlans.get(mPositionLevel);
                    mNextBtn.setEnabled(true);
                } else {
                    mNextBtn.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void updateProfile(final String level, String phoneNum, final CallBackInterface callback) {
        if (level == null && phoneNum == null) {
            callback.exectueMethod();
            return;
        }
        UpdateUserProfile updateTask = new UpdateUserProfile(mContext,
                new PostExecuting<HttpBaseMessage>() {
                    @Override
                    public void executing(HttpBaseMessage result) {
                        if (result != null && "0".equals(result.status)) {
                            callback.exectueMethod();
                        } else {
                            Log.e("ThirdPartyLogin", "updateProfile error");
                            finish();
                        }
                    }
                });

        try{
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("bella_id", bella_id);
            jsonObj.put("plan_id", level);
            jsonObj.put("phone", phoneNum);
            updateTask.execute(jsonObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public interface CallBackInterface {
        public void exectueMethod();
    }
}
