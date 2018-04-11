package com.liulishuo;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.angcyo.library.utils.L;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;

import java.util.ArrayList;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/03/27 11:09
 * 修改人员：Robi
 * 修改时间：2018/03/27 11:09
 * 修改备注：
 * Version: 1.0.0
 */
class FListener extends FileDownloadSampleListener {

    /**
     * 监听哪个下载地址
     */
    private String listenerUrl = null;
    private ArrayList<FListener> mListeners = new ArrayList<>();

    //    @Override
//    public int hashCode() {
//        return listenerUrl.hashCode();
//    }
//
    @Override
    public boolean equals(Object obj) {
        if (TextUtils.isEmpty(listenerUrl)) {
            return super.equals(obj);
        }

        if (TextUtils.isEmpty(((FListener) obj).listenerUrl)) {
            return super.equals(obj);
        }

        return TextUtils.equals(listenerUrl, ((FListener) obj).listenerUrl);
    }

    public String getListenerUrl() {
        return listenerUrl;
    }

    public FListener setListenerUrl(String listenerUrl) {
        this.listenerUrl = listenerUrl;
        return this;
    }

    public FListener addListener(FListener listener) {
        if (mListeners.contains(listener)) {

        } else {
            mListeners.add(listener);
        }
        L.d("call: addListener 监听事件数量-> " + mListeners.size());
        return this;
    }

    public FListener removeListener(FListener listener) {
        mListeners.remove(listener);
        return this;
    }

    public FListener clearListener() {
        mListeners.clear();
        return this;
    }

    /**
     * 1:
     */
    @Override
    protected void pending(@NonNull BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.pending(task, soFarBytes, totalBytes);
        L.d("call: pending([task, soFarBytes, totalBytes])-> soFarBytes:" + soFarBytes + " totalBytes:" + totalBytes);
    }

    /**
     * 2:
     */
    @Override
    protected void started(@NonNull BaseDownloadTask task) {
        super.started(task);
        onStarted(task);
    }

    /**
     * 3:
     */
    @Override
    protected void connected(@NonNull BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
    }

    /**
     * 4:
     */
    @Override
    protected void progress(@NonNull BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.progress(task, soFarBytes, totalBytes);
        float progress = 0;
        if (totalBytes != -1) {
            final float percent = soFarBytes / (float) totalBytes;
            progress = percent * 100;
        }
        onProgress(task, soFarBytes, totalBytes, progress, ((int) progress) + "%");
        onProgress(task, soFarBytes, totalBytes, progress);
        onProgress(task, soFarBytes, totalBytes, ((int) progress) + "%");
    }

    /**
     * 5:
     */
    @Override
    protected void completed(@NonNull BaseDownloadTask task) {
        super.completed(task);
        onProgress(task, task.getSmallFileSoFarBytes(), task.getSmallFileTotalBytes(), 100, "100%");
        onProgress(task, task.getSmallFileSoFarBytes(), task.getSmallFileTotalBytes(), 100);
        onProgress(task, task.getSmallFileSoFarBytes(), task.getSmallFileTotalBytes(), "100%");

        onCompleted(task);
    }

    @Override
    protected void blockComplete(@NonNull BaseDownloadTask task) {
        super.blockComplete(task);
    }

    @Override
    protected void paused(@NonNull BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.paused(task, soFarBytes, totalBytes);
    }

    @Override
    protected void error(@NonNull BaseDownloadTask task, Throwable e) {
        super.error(task, e);
        onError(task, e);
    }

    @Override
    protected void warn(@NonNull BaseDownloadTask task) {
        super.warn(task);
    }

    @Override
    protected boolean isInvalid() {
        return super.isInvalid();
    }

    @Override
    protected void retry(@NonNull BaseDownloadTask task, Throwable ex, int retryingTimes, int soFarBytes) {
        super.retry(task, ex, retryingTimes, soFarBytes);
    }

    /**
     * 下载进度
     */
    @Deprecated
    public void onProgress(@NonNull BaseDownloadTask task, int soFarBytes, int totalBytes, float progress /*80 比例*/) {
//        L.d("下载进度:" + task.getUrl() + " -> total:" + totalBytes + " :" + progress);
    }

    @Deprecated
    public void onProgress(@NonNull BaseDownloadTask task, int soFarBytes, int totalBytes, String scale /*80% 比例*/) {
        //  L.d("下载进度:" + task.getUrl() + " -> total:" + totalBytes + " :" + scale);
    }

    public void onProgress(@NonNull BaseDownloadTask task, int soFarBytes /*已下载量*/, int totalBytes /*总下载量*/, float progress, String scale) {
        L.d("下载进度:" + task.getUrl() + " -> total:" + totalBytes + " :" + progress + " :" + scale);

        for (int i = 0; i < mListeners.size(); i++) {
            FListener fListener = mListeners.get(i);
            fListener.onProgress(task, soFarBytes, totalBytes, progress, scale);
        }
    }

    /**
     * 开始下载
     */
    public void onStarted(@NonNull BaseDownloadTask task) {
        L.i("开始下载:" + task.getUrl() + " ->" + task.getPath());

        for (int i = 0; i < mListeners.size(); i++) {
            FListener fListener = mListeners.get(i);
            fListener.onStarted(task);
        }
    }

    /**
     * 下载完成
     */
    public void onCompleted(@NonNull BaseDownloadTask task) {
        L.w("下载完成:" + task.getUrl() + " ->" + task.getPath());

        for (int i = 0; i < mListeners.size(); i++) {
            FListener fListener = mListeners.get(i);
            fListener.onCompleted(task);
        }
    }

    /**
     * 下载失败
     */
    public void onError(@NonNull BaseDownloadTask task, Throwable e) {
        L.e("下载失败:" + task.getUrl());
        e.printStackTrace();

        for (int i = 0; i < mListeners.size(); i++) {
            FListener fListener = mListeners.get(i);
            fListener.onError(task, e);
        }
    }
}