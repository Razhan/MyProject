package com.ef.bite.ui.test;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ef.bite.R;

public class testHttpsActivity extends Activity {

    private TextView tv;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_https);

        tv = (TextView)findViewById(R.id.tv);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MyAsyncTask asyncTask = new MyAsyncTask(tv, testHttpsActivity.this);
                asyncTask.execute();
            }
        });

    }

}
