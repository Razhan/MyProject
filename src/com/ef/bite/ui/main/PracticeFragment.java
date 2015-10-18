package com.ef.bite.ui.main;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ef.bite.R;
import com.ef.bite.business.action.RehearseChunkOpenAction;
import com.ef.bite.dataacces.ChunkLoader;
import com.ef.bite.dataacces.mode.Chunk;
import com.ef.bite.dataacces.mode.httpMode.HttpDashboard;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.utils.NetworkChecker;
import com.ef.bite.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * This fragment show when user have some lessons of rehearsal
 * Created by yang on 15/6/11.
 */
public class PracticeFragment extends BaseDashboardFragment implements View.OnClickListener {

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        root = inflater.inflate(R.layout.fragment_home_sreen_practice_highlight, container, false);
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
        if (httpDashboard == null) {
            return;
        }
        final List<ChunkLoader.Request> requests = new ArrayList<ChunkLoader.Request>();
        for (HttpDashboard.Lesson lesson : httpDashboard.data.new_rehearsals) {
            requests.add(new ChunkLoader.Request(
                    lesson.course_package_url,
                    lesson.course_id,
                    lesson.course_version));
        }

        chunkLoader.load(requests,
                new ChunkLoader.OnFinishListener() {
                    @Override
                    public void doOnFinish(boolean isDone) {
                        List<Chunk> chunks = chunkLoader.getChunkList(requests);
                        addChunkStatus(chunks, httpDashboard);
                        if (chunks != null && chunks.size() > 0) {
                            new RehearseChunkOpenAction().open(getActivity(), chunks);
                        } else {
                            if(NetworkChecker.isConnected(getActivity())){
                                Toast.makeText(
                                        getActivity(),
                                        JsonSerializeHelper.JsonLanguageDeserialize(
                                                getActivity(), "home_screen_no_rehearse_chunks"),
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

    private void addChunkStatus(List<Chunk> chunks, HttpDashboard httpDashboard) {
        if(chunks==null||httpDashboard==null){
            return;
        }
        for (Chunk chunk : chunks) {
            for (int i = 0; i < httpDashboard.data.new_rehearsals.size(); i++) {
                HttpDashboard.Lesson lesson = httpDashboard.data.new_rehearsals.get(i);
                if (chunk!=null && lesson!=null && StringUtils.isEquals(StringUtils.nullStrToEmpty(chunk.getChunkCode()), StringUtils.nullStrToEmpty(lesson.course_id))) {
                    chunk.setRehearsalStatus(lesson.rehearsal_status);
                    break;
                }
            }
        }
    }

    @Override
    protected void update(HttpDashboard httpDashboard) {
        super.update(httpDashboard);

        practice_title.setText(String.valueOf(httpDashboard.data.new_rehearsal_count) + " " + JsonSerializeHelper.JsonLanguageDeserialize(
                getActivity(), "dash_screen_phrase_plural"));
        practice_info.setText(JsonSerializeHelper.JsonLanguageDeserialize(
                getActivity(), "dash_screen_practice_now"));
        nextButton.setText(JsonSerializeHelper.JsonLanguageDeserialize(
                getActivity(), "dash_screen_practice_title"));
    }

}
