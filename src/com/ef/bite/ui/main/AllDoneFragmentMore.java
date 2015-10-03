package com.ef.bite.ui.main;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ef.bite.AppConst;
import com.ef.bite.R;
import com.ef.bite.business.task.GetUnlockChunkTask;
import com.ef.bite.business.task.PostExecuting;
import com.ef.bite.dataacces.ChunkLoader;
import com.ef.bite.dataacces.mode.Chunk;
import com.ef.bite.dataacces.mode.httpMode.HttpDashboard;
import com.ef.bite.dataacces.mode.httpMode.HttpUnlockChunks;
import com.ef.bite.ui.chunk.ChunkLearnActivity;
import com.ef.bite.utils.FontHelper;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.utils.NetworkChecker;

/**
 * This fragment show when no any new or rehearsal lesson
 * Created by yang on 15/6/11.
 */
public class AllDoneFragmentMore extends BaseDashboardFragment implements View.OnClickListener {

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        root = inflater.inflate(R.layout.fragment_home_sreen_all_done_more, container, false);
//        setupViews();
//        return root;
//    }

    @Override
    public void setupViews() {
        super.setupViews();

        nextButton.setOnClickListener(this);

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
    public void onClick(View v) {
        getUnlockChunk();
    }

    @Override
    protected void update(HttpDashboard httpDashboard) {
        super.update(httpDashboard);

        practice_title.setText("You are done for today!");
        practice_info.setText("Do you want to learn more?");
        nextButton.setText("LEARN MORE");
    }

    private void getUnlockChunk(){
        new GetUnlockChunkTask(getActivity(), new PostExecuting<HttpUnlockChunks>() {
            @Override
            public void executing(HttpUnlockChunks httpUnlockChunks) {
                if(httpUnlockChunks!=null && httpUnlockChunks.status.equals("0")){
                    if(httpUnlockChunks.data.size()<1){
                        return;
                    }

                    final HttpDashboard.Lesson lesson = httpUnlockChunks.data.get(0);
                    chunkLoader.load(new ChunkLoader.Request(lesson.course_package_url,lesson.course_id,lesson.course_version), new ChunkLoader.OnFinishListener() {
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
            }
        }).execute(AppConst.CurrUserInfo.UserId);
    }

}
