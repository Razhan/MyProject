package com.ef.bite.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ef.bite.R;
import com.ef.bite.ui.BaseActivity;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.widget.ActionbarLayout;

public class AdvertisementActivity extends BaseActivity {

    private ActionbarLayout mActionbar;
    private WebView mWebView;
    private String target_url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);

        Intent intent = getIntent();
        target_url = intent.getStringExtra("target_url");

        mActionbar = (ActionbarLayout) findViewById(R.id.ads_actionbar);
        mActionbar.initiWithTitle("ADS",
                R.drawable.arrow_goback_black, -1, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, null);

        mWebView = (WebView)findViewById(R.id.ads_webview);
        mWebView.loadUrl(target_url);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
