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
import com.ef.bite.R;
import com.ef.bite.dataacces.mode.httpMode.HttpDashboard;
import com.ef.bite.dataacces.ChunkLoader;
import com.ef.bite.ui.chunk.ChunkListActivity;
import com.ef.bite.utils.JsonSerializeHelper;

/**
 * The basic states for dashboard
 * Created by yang on 15/5/8.
 */
public abstract class BaseDashboardFragment extends Fragment {
    protected View root;
    private RelativeLayout mLearnPhraseLayout;
    private RelativeLayout mPracticePhraseLayout;
    private RelativeLayout mMasteredPhraseLayout;
    private RelativeLayout Relative_Pharse;

    protected HttpDashboard httpDashboard;
    protected ChunkLoader chunkLoader;
    private TextView phrasesNum;
    private TextView likesNum;
    private TextView phrasesTitle;
    private TextView likesTitle;

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
        phrasesTitle = (TextView)root.findViewById(R.id.Pharse_title);
        likesTitle = (TextView)root.findViewById(R.id.Like_title);

        practice_title = (TextView)root.findViewById(R.id.home_screen_practice_title);
        practice_info = (TextView)root.findViewById(R.id.home_screen_practice_available_info);

        nextButton = (Button)root.findViewById(R.id.home_screen_next_button);

        Relative_Pharse = (RelativeLayout)root.findViewById(R.id.Relative_Pharse);
        Relative_Pharse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), ChunkListActivity.class));
            }
        });
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

        if (httpDashboard.data.phrase_count < 2) {
            phrasesTitle.setText(JsonSerializeHelper.JsonLanguageDeserialize(
                    getActivity(), "dash_screen_phrase_singular"));
        } else {
            phrasesTitle.setText(JsonSerializeHelper.JsonLanguageDeserialize(
                    getActivity(), "dash_screen_phrase_plural"));
        }

        if (httpDashboard.data.recording_like_count < 2) {
            likesTitle.setText(JsonSerializeHelper.JsonLanguageDeserialize(
                    getActivity(), "dash_screen_phrase_singular"));
        } else {
            likesTitle.setText(JsonSerializeHelper.JsonLanguageDeserialize(
                    getActivity(), "dash_screen_likes_plural"));
        }


        courseInfo.setVisibility(View.VISIBLE);
        phrasesNum.setText(String.valueOf(httpDashboard.data.phrase_count));
        likesNum.setText(String.valueOf(httpDashboard.data.recording_like_count));
    }

    public HttpDashboard getHttpDashboard() {
        return httpDashboard;
    }
}
