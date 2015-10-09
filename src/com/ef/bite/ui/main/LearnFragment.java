package com.ef.bite.ui.main;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.ef.bite.AppConst;
import com.ef.bite.R;
import com.ef.bite.dataacces.ChunkLoader;
import com.ef.bite.dataacces.mode.Chunk;
import com.ef.bite.dataacces.mode.httpMode.HttpDashboard;
import com.ef.bite.ui.chunk.ChunkLearnActivity;
import com.ef.bite.utils.FontHelper;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.utils.NetworkChecker;

/**
 * This fragment show when user have new lesson
 * Created by yang on 15/6/11.
 */
public class LearnFragment extends BaseDashboardFragment implements View.OnClickListener {

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        root = inflater.inflate(R.layout.fragment_home_sreen_learn_highlight, container, false);
//        setupViews();
//        return root;
//    }

    @Override
    public void setupViews() {
        super.setupViews();
        nextButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (httpDashboard == null || httpDashboard.data ==null|| httpDashboard.data.new_lessons == null ||httpDashboard.data.new_lessons.isEmpty()) {
            return;
        }

        final HttpDashboard.Lesson lesson = httpDashboard.data.new_lessons.get(0);
        chunkLoader.load(new ChunkLoader.Request(
                        lesson.course_package_url,
                        lesson.course_id,
                        lesson.course_version),
                new ChunkLoader.OnFinishListener() {
                    @Override
                    public void doOnFinish(boolean isDone) {
                        Chunk chunk = chunkLoader.getChunk(lesson.course_id);
                        if (chunk != null) {
                            Intent intent = new Intent(getActivity(), ChunkLearnActivity.class);
                            intent.putExtra(AppConst.BundleKeys.Chunk, chunk);
                            getActivity().startActivity(intent);
//                                    new NewChunkOpenAction().open(getActivity(), chunk);
                        } else {
                            if(NetworkChecker.isConnected(getActivity())){
                                Toast.makeText(
                                        getActivity(),
                                        JsonSerializeHelper.JsonLanguageDeserialize(
                                                getActivity(), "home_screen_no_new_chunks"),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(
                                        getActivity(),
                                        JsonSerializeHelper.JsonLanguageDeserialize(
                                                getActivity(), "error_check_network_available"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    protected void update(HttpDashboard httpDashboard) {
        super.update(httpDashboard);

        practice_title.setText("\"" + httpDashboard.data.new_lessons.get(0).course_name.toUpperCase() + "\"");
        practice_info.setText("LEARN A NEW PHRASE");
        nextButton.setText("LEARN");
    }
}
