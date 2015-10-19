package com.example.maciej.eventag.Helpers;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static com.example.maciej.eventag.Models.Constants.*;

public class RestClient {

    private AsyncHttpClient client;

    public RestClient(String accessKey) {
        this.client = new AsyncHttpClient();
        this.client.addHeader("Content-Type", "application/json");
        this.client.addHeader("test", "test");
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        this.client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        this.client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
