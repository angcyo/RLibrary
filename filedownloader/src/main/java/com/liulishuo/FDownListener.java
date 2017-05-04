package com.liulishuo;

import com.angcyo.library.utils.L;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/05/04 14:23
 * 修改人员：Robi
 * 修改时间：2017/05/04 14:23
 * 修改备注：
 * Version: 1.0.0
 */
public class FDownListener extends FileDownloadSampleListener {

    /**
     * 1:
     */
    @Override
    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.pending(task, soFarBytes, totalBytes);
    }

    /**
     * 2:
     */
    @Override
    protected void started(BaseDownloadTask task) {
        super.started(task);
        onStarted(task);
    }

    /**
     * 3:
     */
    @Override
    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
    }

    /**
     * 4:
     */
    @Override
    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.progress(task, soFarBytes, totalBytes);
        String scale = "0%";
        if (totalBytes != -1) {
            final float percent = soFarBytes / (float) totalBytes;
            scale = percent * 100 + "%";
        }
        onProgress(task, soFarBytes, totalBytes, scale);
    }

    /**
     * 5:
     */
    @Override
    protected void completed(BaseDownloadTask task) {
        super.completed(task);
        onCompleted(task);
    }

    @Override
    protected void blockComplete(BaseDownloadTask task) {
        super.blockComplete(task);
    }

    @Override
    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.paused(task, soFarBytes, totalBytes);
    }

    @Override
    protected void error(BaseDownloadTask task, Throwable e) {
        super.error(task, e);
        onError(task, e);
    }

    @Override
    protected void warn(BaseDownloadTask task) {
        super.warn(task);
    }

    @Override
    protected boolean isInvalid() {
        return super.isInvalid();
    }

    @Override
    protected void retry(BaseDownloadTask task, Throwable ex, int retryingTimes, int soFarBytes) {
        super.retry(task, ex, retryingTimes, soFarBytes);
    }

    /**
     * 开始下载
     */
    protected void onStarted(BaseDownloadTask task) {
        L.i("开始下载:" + task.getUrl() + " ->" + task.getPath());
    }

    /**
     * 下载完成
     */
    protected void onCompleted(BaseDownloadTask task) {
        L.w("下载完成:" + task.getUrl() + " ->" + task.getPath());
    }

    /**
     * 下载进度
     */
    protected void onProgress(BaseDownloadTask task, int soFarBytes, int totalBytes, String scale /*80% 比例*/) {
        L.i("下载进度:" + task.getUrl() + " -> total:" + totalBytes + " :" + scale);
    }

    /**
     * 下载失败
     */
    protected void onError(BaseDownloadTask task, Throwable e) {
        L.e("下载失败:" + task.getUrl() + " ->" + e.getMessage());
    }
}
