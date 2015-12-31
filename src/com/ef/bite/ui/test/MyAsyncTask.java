package com.ef.bite.ui.test;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;


/**
 * Created by ranzh on 12/23/2015.
 */
public class MyAsyncTask extends AsyncTask<Void, Void, String> {

    private TextView textView;
    private Context context;
    private String TAG = MyAsyncTask.class.getSimpleName();

    public MyAsyncTask(TextView textView, Context context) {
        super();
        this.textView = textView;
        this.context = context;

    }

    @Override
    protected String doInBackground(Void... params) {
        String res = "";
        try {
            res = initSSLAllWithHttpClient();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;

    }

    @Override
    protected void onPostExecute(String s) {
        textView.setText(s);
    }

    public String initSSLAllWithHttpClient() throws ClientProtocolException, IOException {
        int timeOut = 10 * 1000;
        HttpParams param = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(param, timeOut);
        HttpConnectionParams.setSoTimeout(param, timeOut);
        HttpConnectionParams.setTcpNoDelay(param, true);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        registry.register(new Scheme("https", TrustAllSSLSocketFactory.getDefault(), 443));
        registry.register(new Scheme("https", TrustCertainHostNameFactory.getDefault(context), 443));
        ClientConnectionManager manager = new ThreadSafeClientConnManager(param, registry);
        DefaultHttpClient httpClient = new DefaultHttpClient(manager, param);
//        HttpClient httpClient = new DefaultHttpClient();
//
//        HttpGet request = new HttpGet("https://b2cglobaluat.englishtown.com/api/bella/friend/is/afd094e3-bcd7-407d-812b-42fc5f3b9767/afd094e3-bcd7-407d-812b-42fc5f3b9767/");
//
//        // HttpGet request = new HttpGet("https://www.alipay.com/");
//        HttpResponse response = client.execute(request);


        String jsonParams = "{'bella_id':'afd094e3-bcd7-407d-812b-42fc5f3b9767'}";

        HttpContext localContext = new BasicHttpContext();
        httpClient.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, timeOut);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                timeOut);
        HttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost("https://bella-qa-web-lb-1254561975.us-west-2.elb.amazonaws.com/api/bella/2/profile/");
            httpPost.addHeader("Content-type",
                    "application/json; charset=utf-8");

            if (jsonParams != null)
                httpPost.setEntity(new StringEntity(jsonParams, HTTP.UTF_8));
            response = httpClient.execute(httpPost, localContext);


            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            Log.e("HTTPS TEST", result.toString());

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return "";
    }

}
