package com.ef.bite.ui.main;

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

    private final long ONE_MINITUE = 60 * 1000;
    private final long ONE_HOUR = 60 * ONE_MINITUE;
    private final long ONE_DAY = 24 * ONE_HOUR;

    @Override
    public void setupViews() {
        super.setupViews();
    }

    @Override
    protected void update(HttpDashboard httpDashboard) {
        super.update(httpDashboard);

        practice_title.setText("You are done for today!");
        practice_info.setText("Do you want to learn more?");
        nextButton.setVisibility(View.GONE);
    }



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
