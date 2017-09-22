package com.liulishuo;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.v4.util.ArrayMap;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadList;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.liulishuo.filedownloader.connection.OkHttp3Connection;
import com.liulishuo.filedownloader.services.DownloadMgrInitialParams;
import com.liulishuo.filedownloader.util.FileDownloadLog;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    public static void unInit() {
        FileDownloader.getImpl().unBindServiceIfIdle();
    }

    public static void init(Application context, boolean debug) {
        /**
         * just for cache Application's Context, and ':filedownloader' progress will NOT be launched
         * by below code, so please do not worry about performance.
         * @see FileDownloader#init(Context)
         */
//        FileDownloader.init(context,
//                new DownloadMgrInitialParams.InitCustomMaker()
//                        .connectionCreator(new FileDownloadUrlConnection
//                                .Creator(new FileDownloadUrlConnection.Configuration()
//                                .setUseOkHttp(true)// use ok http
//                                .connectTimeout(15_000) // set connection timeout.
//                                .readTimeout(15_000) // set read timeout.
//                                .proxy(Proxy.NO_PROXY) // set proxy
//                        )));
//星期五 2017-9-22
//        FileDownloader.init(context, new DownloadMgrInitialParams.InitCustomMaker()
//                .connectionCreator(new OkHttp3Connection.Creator()));

        FileDownloader.setupOnApplicationOnCreate(context).connectionCreator(new OkHttp3Connection.Creator());

        FileDownloadLog.NEED_LOG = debug;

        checkServiceConnect();
        //FileDownloader.getImpl().setMaxNetworkThreadCount(3);
    }

    private static void checkServiceConnect() {
        if (!FileDownloader.getImpl().isServiceConnected()) {
            FileDownloader.getImpl().bindService();
        }
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
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#INVALID_STATUS    0
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#pending           1
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#connected         2
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#progress          3
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#blockComplete     4
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#retry             5
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#started           6
     * <p/>
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#error             -1
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#paused            -2
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#completed         -3
     * @see com.liulishuo.filedownloader.model.FileDownloadStatus#warn              -4
     */
    public static int getStatus(final String url, final String path) {
        return FileDownloader.getImpl().getStatus(url, path);
    }

    public static int getStatusIgnoreCompleted(final int id) {
        return FileDownloader.getImpl().getStatusIgnoreCompleted(id);
    }

    public static int getStatusIgnoreCompleted(final String url, final String path) {
        return FileDownloader.getImpl().getStatusIgnoreCompleted(generateId(url, path));
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
        boolean isForceReDownload;


        //        static ArrayMap<Integer, String> taskMap = new ArrayMap();
        /**
         * 保存url 和 创建的任务id
         */
        static ConcurrentHashMap<String, Integer> taskMap = new ConcurrentHashMap<>();

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

        public Builder setForceReDownload(boolean forceReDownload) {
            isForceReDownload = forceReDownload;
            return this;
        }

        /**
         * @return 返回任务id, 可以用来取消下载
         */
        public int download(FDownListener listener) {
            int id = -1;
            Integer integer = taskMap.get(url);
            if (integer != null && integer > 0) {
                id = integer;
                BaseDownloadTask.IRunningTask runningTask = FileDownloadList.getImpl().get(integer);
                if (runningTask != null) {
                    runningTask.getOrigin().setListener(listener);
                } else {
                    id = -1;
                }
            }

            if (id == -1) {
                id = FileDownloader.getImpl().create(url)
                        .setPath(fullPath, false)
                        .setCallbackProgressTimes(mCallbackProgressTimes)/**每隔多少毫秒回调一次进度*/
                        .setMinIntervalUpdateSpeed(mCallbackProgressMinIntervalMillis)/**每隔多少毫秒回调一次速度*/
                        .setTag(tag)/**附加对象*/
                        .setForceReDownload(isForceReDownload)/**如果文件存在是否重新下载*/
                        .addFinishListener(new BaseDownloadTask.FinishListener() {
                            @Override
                            public void over(BaseDownloadTask task) {
                                taskMap.remove(task.getUrl());
                            }
                        })
                        .setListener(listener)
                        .start();
                taskMap.put(url, id);
            }
            return id;
        }
    }
}
