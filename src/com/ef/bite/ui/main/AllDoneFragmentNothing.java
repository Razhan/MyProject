package com.ef.bite.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ef.bite.R;
import com.ef.bite.dataacces.mode.httpMode.HttpDashboard;

/**
 * Created by ranzh on 10/3/2015.
 */
public class AllDoneFragmentNothing extends BaseDashboardFragment {

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        root = inflater.inflate(R.layout.fragment_home_sreen_all_done_nothing, container, false);
//        setupViews();
//        return root;
//    }

    @Override
    public void setupViews() {
        super.setupViews();

//        goodJob = (TextView) root.findViewById(R.id.home_screen_done_good);
//        goodJob.setText(JsonSerializeHelper.JsonLanguageDeserialize(getActivity(), "home_screen_all_done_good_job"));
//        goodJobInfo = (TextView) root.findViewById(R.id.home_screen_done_info);
//        goodJobInfo.setText(JsonSerializeHelper.JsonLanguageDeserialize(getActivity(), "home_screen_all_done_stay_tuned"));
//        FontHelper.applyFont(getActivity(), getmLearnCount(),
//                FontHelper.FONT_Museo500);
//        FontHelper.applyFont(getActivity(), getmPracticeCount(),
//                FontHelper.FONT_Museo500);
//        FontHelper.applyFont(getActivity(), getmMasteredCount(),
//                FontHelper.FONT_Museo500);
//        FontHelper.applyFont(getActivity(), goodJob,
//                FontHelper.FONT_Museo500);

//        ImageButton unlockBtn = (ImageButton) root.findViewById(R.id.home_screen_practice_unlock);
//        RelativeLayout unlockBtn = (RelativeLayout) root.findViewById(R.id.home_screen_learn_layout);
//        unlockBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getUnlockChunk();
//            }
//        });

    }

    @Override
    protected void update(HttpDashboard httpDashboard) {
        super.update(httpDashboard);

        practice_title.setText("You are done for today!");
        practice_info.setText("Do you want to learn more?");
        nextButton.setVisibility(View.GONE);
    }
}
