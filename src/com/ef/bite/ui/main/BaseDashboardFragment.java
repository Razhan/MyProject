package com.ef.bite.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ef.bite.AppConst;
import com.ef.bite.R;
import com.ef.bite.business.action.RehearseChunkOpenAction;
import com.ef.bite.business.task.GetUnlockChunkTask;
import com.ef.bite.business.task.PostExecuting;
import com.ef.bite.dataacces.mode.Chunk;
import com.ef.bite.dataacces.mode.httpMode.HttpDashboard;
import com.ef.bite.dataacces.mode.httpMode.HttpUnlockChunks;
import com.ef.bite.ui.chunk.ChunkLearnActivity;
import com.ef.bite.ui.chunk.ChunkListActivity;
import com.ef.bite.dataacces.ChunkLoader;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.utils.NetworkChecker;
import com.ef.bite.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The basic states for dashboard
 * Created by yang on 15/5/8.
 */
public abstract class BaseDashboardFragment extends Fragment {
    protected View root;
    private RelativeLayout mLearnPhraseLayout;
    private RelativeLayout mPracticePhraseLayout;
    private RelativeLayout mMasteredPhraseLayout;
    protected HttpDashboard httpDashboard;
    protected ChunkLoader chunkLoader;
    private TextView phrasesNum;
    private TextView likesNum;

    protected TextView practice_title;
    protected TextView practice_info;
    protected Button nextButton;

    private LinearLayout courseInfo;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home_sreen_all_done_more, container, false);
        setupViews();
        return root;
    }

    public void setupViews() {
        if (root == null) {
            return;
        }
        chunkLoader = new ChunkLoader(getActivity());


        courseInfo = (LinearLayout)root.findViewById(R.id.home_screen_courses_info);
        mLearnPhraseLayout = (RelativeLayout) root
                .findViewById(R.id.home_screen_progress_score_layout);
        mPracticePhraseLayout = (RelativeLayout) root
                .findViewById(R.id.home_screen_learn_layout);
        mMasteredPhraseLayout = (RelativeLayout) root
                .findViewById(R.id.home_screen_mastered_layout);
        phrasesNum = (TextView)root.findViewById(R.id.home_screen_pharses_num);
        likesNum = (TextView)root.findViewById(R.id.home_screen_likes_num);

        practice_title = (TextView)root.findViewById(R.id.home_screen_practice_title);
        practice_info = (TextView)root.findViewById(R.id.home_screen_practice_available_info);

        nextButton = (Button)root.findViewById(R.id.home_screen_next_button);

    }

    protected void toast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public RelativeLayout getmLearnPhraseLayout() {
        return mLearnPhraseLayout;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public RelativeLayout getmPracticePhraseLayout() {
        return mPracticePhraseLayout;
    }

    protected void update(HttpDashboard httpDashboard) {
        if (httpDashboard == null) {
            return;
        }
        this.httpDashboard = httpDashboard;

        courseInfo.setVisibility(View.VISIBLE);
        phrasesNum.setText(String.valueOf(httpDashboard.data.phrase_count));
        likesNum.setText(String.valueOf(httpDashboard.data.phrase_count));
    }

    public HttpDashboard getHttpDashboard() {
        return httpDashboard;
    }

    long ONE_MINITUE = 60 * 1000;
    long ONE_HOUR = 60 * ONE_MINITUE;
    long ONE_DAY = 24 * ONE_HOUR;

    /**
     * 显示将来可用的具体剩余时间
     *
     * @param availableTime
     * @param isLearn       是否是learn，或者practice
     * @return
     */
    public String getAvailableLeftTimeText(Long availableTime, boolean isLearn) {
        // String TimeText = null;
        try {
            if (availableTime == null)
                return isLearn ? JsonSerializeHelper.JsonLanguageDeserialize(
                        getActivity(), "home_screen_learn_available_tomorrow")
                        : JsonSerializeHelper.JsonLanguageDeserialize(getActivity(),
                        "home_screen_practice_available_tomorrow");
            if (availableTime >= ONE_DAY)
                return isLearn ? JsonSerializeHelper.JsonLanguageDeserialize(
                        getActivity(), "home_screen_learn_available_tomorrow")
                        : JsonSerializeHelper.JsonLanguageDeserialize(getActivity(),
                        "home_screen_practice_available_tomorrow");
            if (availableTime < ONE_DAY && availableTime >= ONE_HOUR) {
                int leftHour = (int) (availableTime / ONE_HOUR);
                return String.format(
                        isLearn ? JsonSerializeHelper.JsonLanguageDeserialize(
                                getActivity(), "home_screen_learn_available_hours")
                                : JsonSerializeHelper.JsonLanguageDeserialize(
                                getActivity(),
                                "home_screen_practice_available_hour"),
                        leftHour);
            }
            if (availableTime < ONE_HOUR && availableTime > 0) {
                int leftMinutes = (int) (availableTime / ONE_MINITUE);
                return String
                        .format(isLearn ? JsonSerializeHelper
                                        .JsonLanguageDeserialize(getActivity(),
                                                "home_screen_learn_available_tomorrow")
                                        : JsonSerializeHelper
                                        .JsonLanguageDeserialize(getActivity(),
                                                "home_screen_practice_available_minutes"),
                                leftMinutes);
            }
            if (availableTime <= 0) {
                return isLearn ? JsonSerializeHelper.JsonLanguageDeserialize(
                        getActivity(), "home_screen_learn_available_now")
                        : JsonSerializeHelper.JsonLanguageDeserialize(getActivity(),
                        "home_screen_practice_available_now");
            }
            return isLearn ? JsonSerializeHelper.JsonLanguageDeserialize(
                    getActivity(), "home_screen_learn_available_tomorrow")
                    : JsonSerializeHelper.JsonLanguageDeserialize(getActivity(),
                    "home_screen_practice_available_tomorrow");
        } catch (NullPointerException e) {
            // TODO: handle exception
            e.printStackTrace();
            return "0";
        }

    }
}
