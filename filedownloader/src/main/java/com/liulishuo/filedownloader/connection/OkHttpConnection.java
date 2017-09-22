package com.liulishuo.filedownloader.connection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/05/04 10:38
 * 修改人员：Robi
 * 修改时间：2017/05/04 10:38
 * 修改备注：
 * Version: 1.0.0
 */
@Deprecated
public class OkHttpConnection implements FileDownloadConnection {

    private static OkHttpClient sOkHttpClient;
    protected Request.Builder requestBuilder;
    private Response mResponse;
    private Request mRequest;

    public OkHttpConnection(String originUrl, FileDownloadUrlConnection.Configuration configuration) throws IOException {
        this(new URL(originUrl), configuration);
    }

    public OkHttpConnection(URL url, FileDownloadUrlConnection.Configuration configuration) throws IOException {
        if (sOkHttpClient == null) {
            long wTime = 20_000;
            long rTime = 20_000;
            long cTime = 20_000;

//            if (configuration != null) {
//                wTime = configuration.getReadTimeout();
//                rTime = configuration.getReadTimeout();
//                cTime = configuration.getConnectTimeout();
//            }

            sOkHttpClient = new OkHttpClient.Builder()
                    .writeTimeout(wTime, TimeUnit.MILLISECONDS)
                    .readTimeout(rTime, TimeUnit.MILLISECONDS)
                    .connectTimeout(cTime, TimeUnit.MILLISECONDS)
                    .build();
        }

        requestBuilder = new Request.Builder().url(url);
    }

    @Override
    public void addHeader(String name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public boolean dispatchAddResumeOffset(String etag, long offset) {
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return mResponse.body().byteStream();
    }

    @Override
    public Map<String, List<String>> getRequestHeaderFields() {
        ensureRequest();
        return mRequest.headers().toMultimap();
    }

    private void ensureRequest() {
        if (mRequest == null) {
            mRequest = requestBuilder.build();
        }
    }

    @Override
    public Map<String, List<String>> getResponseHeaderFields() {
        return mResponse.headers().toMultimap();
    }

    @Override
    public String getResponseHeaderField(String name) {
        return mResponse.header(name);
    }

    @Override
    public void execute() throws IOException {
        ensureRequest();
        Call call = sOkHttpClient.newCall(mRequest);
        mResponse = call.execute();
    }

    @Override
    public int getResponseCode() throws IOException {
        return mResponse.code();
    }

    @Override
    public void ending() {
        Log.e("", "ending");
        mRequest = null;
        mResponse = null;
    }
}
