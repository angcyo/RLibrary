package com.angcyo.uiview.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.angcyo.github.utilcode.utils.IntentUtils;
import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 进度通知栏
 * Created by angcyo on 2017-05-28.
 */

public class ProgressNotify {

    private final int NOTIFICATION_ID;
    private final NotificationCompat.Builder mBuilder;
    private final RemoteViews mProgressRemoteViews;
    private final RemoteViews mFinishRemoteViews;
    NotificationManager mNotificationManager;
    private Context mContext;
    private Class<?> clickActivity;
    private int requestCode = 10011;

    private String targetFilePath = "";

    private String finishChannelId;
    private String progressChannelId;

    private ProgressNotify() {
        mContext = RApplication.getApp();

        this.NOTIFICATION_ID = new Random(System.currentTimeMillis()).nextInt(10_000);

        // 获取系统服务来初始化对象
        mNotificationManager = (NotificationManager) mContext
                .getSystemService(Activity.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            progressChannelId = String.valueOf(NOTIFICATION_ID);
            NotificationChannel channelProgress = new NotificationChannel(progressChannelId,
                    "ProgressNotify_channelProgress",
                    NotificationManager.IMPORTANCE_LOW);

            channelProgress.enableLights(false);
            channelProgress.enableVibration(false);
            channelProgress.setSound(null, null);

            finishChannelId = String.valueOf(NOTIFICATION_ID + 200);
            NotificationChannel channelFinish = new NotificationChannel(finishChannelId,
                    "ProgressNotify_channelFinish",
                    NotificationManager.IMPORTANCE_HIGH);

            channelFinish.enableLights(true);
            channelFinish.enableVibration(true);
            channelFinish.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);

            List<NotificationChannel> channelList = new ArrayList<>();
            channelList.add(channelProgress);
            channelList.add(channelFinish);
            mNotificationManager.createNotificationChannels(channelList);
        }

        mBuilder = new NotificationCompat.Builder(mContext, String.valueOf(NOTIFICATION_ID));
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        //设置自定义布局中按钮的跳转界面
        //如果是启动activity，那么就用PendingIntent.getActivity，如果是启动服务，那么是getService

        // 自定义布局
        mProgressRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.base_progress_notify_layout);
        mFinishRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.base_progress_finish_notify_layout);

        //实例化工具类，并且调用接口
    }

    public static ProgressNotify instance() {
        return Holder.holder;
    }

    public ProgressNotify setClickActivity(Class<?> clickActivity) {
        this.clickActivity = clickActivity;
        return this;
    }

    public ProgressNotify setTargetFilePath(String targetFilePath) {
        this.targetFilePath = targetFilePath;
        return this;
    }

    public int show(String title, @DrawableRes int logo, int progress) {
        if (clickActivity != null) {
            Intent intent = new Intent(mContext, clickActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            //intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            mBuilder.setContentIntent(PendingIntent.getActivity(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT));// 该通知要启动的Intent
        }

        RemoteViews remoteViews;

        if (progress >= 100) {
            mNotificationManager.cancel(NOTIFICATION_ID);
            return finish(title, logo, progress);
        } else {
            mBuilder.setChannelId(progressChannelId);

            mProgressRemoteViews.setProgressBar(R.id.progressBar, 100, progress, false);

            remoteViews = mProgressRemoteViews;

            mBuilder.setOngoing(true);
            //mBuilder.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE);
            //mBuilder.setDefaults(Notification.FLAG_SHOW_LIGHTS);
            mBuilder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);
        }

        remoteViews.setImageViewResource(R.id.image_view, logo);
        remoteViews.setTextViewText(R.id.text_view, title);

        mBuilder.setSmallIcon(logo);// 设置顶部状态栏的小图标, 必须设置.
        mBuilder.setCustomContentView(remoteViews);
        //mBuilder.setCustomBigContentView(remoteViews);
        mBuilder.setWhen(System.currentTimeMillis());
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        return NOTIFICATION_ID;
    }

    private int finish(String title, @DrawableRes int logo, int progress) {
        mBuilder.setChannelId(finishChannelId);

        if (clickActivity != null) {
            Intent intent = new Intent(mContext, clickActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            //intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            mBuilder.setContentIntent(PendingIntent.getActivity(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT));// 该通知要启动的Intent
        }

        RemoteViews remoteViews = mFinishRemoteViews;

        File targetFile = new File(targetFilePath);
        if (targetFile.exists()) {
            L.e("准备安装-> " + targetFile.getAbsolutePath());
            mBuilder.setContentIntent(PendingIntent.getActivity(mContext, requestCode,
                    IntentUtils.getInstallAppIntent(targetFile), PendingIntent.FLAG_CANCEL_CURRENT));// 该通知要启动的Intent
        } else {
            L.e("需要安装的文件不存在-> " + targetFile.getAbsolutePath());
        }

        mBuilder.setOngoing(false);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        remoteViews.setImageViewResource(R.id.image_view, logo);
        remoteViews.setTextViewText(R.id.text_view, title);

        mBuilder.setSmallIcon(logo);// 设置顶部状态栏的小图标, 必须设置.
        mBuilder.setCustomContentView(remoteViews);
        //mBuilder.setCustomBigContentView(remoteViews);
        mBuilder.setWhen(System.currentTimeMillis());
        mNotificationManager.notify(NOTIFICATION_ID + 200, mBuilder.build());

        return NOTIFICATION_ID + 200;
    }

    static class Holder {
        static ProgressNotify holder = new ProgressNotify();
    }

}
