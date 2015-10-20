package com.example.maciej.eventag.Helpers;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.entity.StringEntity;

import static com.example.maciej.eventag.Models.Constants.*;

public class RestClient {

    private Context context;
    private AsyncHttpClient client;

    public RestClient(Context context, String accessKey) {
        this.context = context;
        this.client = new AsyncHttpClient();
        this.client.addHeader("Content-Type", "application/json");
        this.client.addHeader("Facebook", accessKey);
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        this.client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, StringEntity entity, AsyncHttpResponseHandler responseHandler) {
        this.client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
