package com.ef.bite.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ef.bite.R;
import com.ef.bite.dataacces.mode.httpMode.HttpDashboard;
import com.ef.bite.utils.JsonSerializeHelper;

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

    private final long ONE_MINITUE = 60 * 1;
    private final long ONE_HOUR = 60 * ONE_MINITUE;
    private final long ONE_DAY = 24 * ONE_HOUR;

    @Override
    public void setupViews() {
        super.setupViews();
    }

    @Override
    protected void update(HttpDashboard httpDashboard) {
        super.update(httpDashboard);

        practice_title.setText(JsonSerializeHelper.JsonLanguageDeserialize(
                getActivity(), "dash_screen_no_more_phrases"));

        practice_info.setText("Practice again in " + getAvailableLeftTimeText(
                                httpDashboard.data.new_rehearsal_unlocking_seconds, false));
        nextButton.setVisibility(View.GONE);
    }

    /**
     * 显示将来可用的具体剩余时间
     *
     * @param availableTime
     * @param isLearn       是否是learn，或者practice
     * @return
     */
    public String getAvailableLeftTimeText(Integer availableTime, boolean isLearn) {
        // String TimeText = null;
        try {
            if (availableTime == null)
                return String.valueOf(1) + " days";

            if (availableTime >= ONE_DAY) {
                if (availableTime % ONE_DAY == 0) {
                    return String.valueOf(availableTime / ONE_DAY) + " days";
                } else {
                    return String.valueOf(availableTime / ONE_DAY + 1) + " days";
                }
            } else if (availableTime < ONE_DAY && availableTime >= ONE_HOUR) {
                int leftHour = (int) (availableTime / ONE_HOUR);
                return String.valueOf(leftHour) + " hours";
            } else if (availableTime < ONE_HOUR && availableTime > 0) {
                int leftMinutes = (int) (availableTime / ONE_MINITUE);
                return String.valueOf(leftMinutes) + " minutes";
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
