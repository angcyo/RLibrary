package com.liulishuo;

import android.support.annotation.NonNull;

import com.liulishuo.filedownloader.BaseDownloadTask;

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
public class FDownListener extends FListener {

    //只回调以下接口

    @Override
    public void onStarted(@NonNull BaseDownloadTask task) {
        //super.onStarted(task);
    }

    @Override
    public void onCompleted(@NonNull BaseDownloadTask task) {
        //super.onCompleted(task);
    }

    @Override
    public void onProgress(@NonNull BaseDownloadTask task, int soFarBytes, int totalBytes, float progress, String scale) {
        //super.onProgress(task, soFarBytes, totalBytes, progress, scale);
    }

    @Override
    public void onError(@NonNull BaseDownloadTask task, Throwable e) {
        //super.onError(task, e);
    }
}
