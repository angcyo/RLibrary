package com.liulishuo;

import android.content.Context;
import android.os.Environment;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.liulishuo.filedownloader.services.DownloadMgrInitialParams;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：{@link com.liulishuo.filedownloader.FileDownloader} 简单封装
 * <p>
 * https://github.com/lingochamp/FileDownloader
 * <p>
 * 创建人员：Robi
 * 创建时间：2017/05/04 14:09
 * 修改人员：Robi
 * 修改时间：2017/05/04 14:09
 * 修改备注：
 * Version: 1.0.0
 */
public class FDown {

    private static final int mCallbackProgressTimes = 300;
    private static final int mCallbackProgressMinIntervalMillis = 300;

    private FDown() {

    }

    public static void init(Context context) {
        /**
         * just for cache Application's Context, and ':filedownloader' progress will NOT be launched
         * by below code, so please do not worry about performance.
         * @see FileDownloader#init(Context)
         */
        FileDownloader.init(context, new DownloadMgrInitialParams.InitCustomMaker()
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .setUseOkHttp(true)// use ok http
                        .connectTimeout(15_000) // set connection timeout.
                        .readTimeout(15_000) // set read timeout.
                        .proxy(Proxy.NO_PROXY) // set proxy
                )));

        //FileDownloader.getImpl().setMaxNetworkThreadCount(3);
    }

    public static Builder build(String url) {
        return new Builder(url);
    }

    /**
     * 暂停任务
     */
    public static void pause(final int id) {
        FileDownloader.getImpl().pause(id);
    }

    public static void pause(final String url, final String path) {
        FileDownloader.getImpl().pause(generateId(url, path));
    }

    /**
     * 使用md算法, 构建一个任务id
     */
    public static int generateId(final String url, final String path) {
        return FileDownloadUtils.generateId(url, path);
    }

    /**
     * 批量任务下载
     */
    public static void downloads(FDownListener listener, List<FTask> tasks) {
        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(listener);

        final List<BaseDownloadTask> baseDownloadTasks = new ArrayList<>();
        for (FTask task : tasks) {
            baseDownloadTasks.add(FileDownloader
                    .getImpl()
                    .create(task.url)
                    .setPath(task.path)
                    .setTag(task.url));
        }

        /**不回调进度*/
        queueSet.disableCallbackProgressTimes(); // do not want each task's download progress's callback,
        // we just consider which task will completed.

        // auto retry 1 time if download fail
        /**自动重试*/
        queueSet.setAutoRetryTimes(1);

        // start download in serial order
        /**串行下载*/
        //queueSet.downloadSequentially(baseDownloadTasks);
        // if your baseDownloadTasks are not a list, invoke such following will more readable:
//            queueSet.downloadSequentially(
//                    FileDownloader.getImpl().create(url).setPath(...),
//                    FileDownloader.getImpl().create(url).addHeader(...,...),
//                    FileDownloader.getImpl().create(url).setPath(...)
//            );
        // start parallel download
        /**并行下载*/
        queueSet.downloadTogether(baseDownloadTasks);
        // if your baseDownloadTasks are not a list, invoke such following will more readable:
//            queueSet.downloadTogether(
//                    FileDownloader.getImpl().create(url).setPath(...),
//                    FileDownloader.getImpl().create(url).setPath(...),
//                    FileDownloader.getImpl().create(url).setSyncCallback(true)
//            );
        queueSet.start();
    }

    public static class Builder {
        String url;
        String fullPath;
        Object tag;

        private Builder(String url) {
            this.url = url;
            tag = url;
            fullPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + UUID.randomUUID().toString();
        }

        public Builder setTag(Object tag) {
            this.tag = tag;
            return this;
        }

        public Builder setFullPath(String fullPath) {
            this.fullPath = fullPath;
            return this;
        }

        /**
         * @return 返回任务id, 可以用来取消下载
         */
        public int download(FDownListener listener) {
            return FileDownloader.getImpl().create(url)
                    .setPath(fullPath, false)
                    .setCallbackProgressTimes(mCallbackProgressTimes)/**每隔多少毫秒回调一次进度*/
                    .setMinIntervalUpdateSpeed(mCallbackProgressMinIntervalMillis)/**每隔多少毫秒回调一次速度*/
                    .setTag(tag)/**附加对象*/
                    .setListener(listener).start();

        }
    }
}
