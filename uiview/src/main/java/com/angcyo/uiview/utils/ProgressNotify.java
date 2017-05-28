package com.angcyo.uiview.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.github.utilcode.utils.IntentUtils;

import java.io.File;
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

    private ProgressNotify() {
        mContext = RApplication.getApp();

        this.NOTIFICATION_ID = new Random(System.currentTimeMillis()).nextInt(10_000);

        // 获取系统服务来初始化对象
        mNotificationManager = (NotificationManager) mContext
                .getSystemService(Activity.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
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

    public int show(String title, @DrawableRes int logo, int progress) {
        if (clickActivity != null) {
            Intent intent = new Intent(mContext, clickActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mBuilder.setContentIntent(PendingIntent.getActivity(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT));// 该通知要启动的Intent
        }

        RemoteViews remoteViews;

        if (progress >= 100) {
            remoteViews = mFinishRemoteViews;

            File targetFile = new File(targetFilePath);
            if (targetFile.exists()) {

                mBuilder.setContentIntent(PendingIntent.getActivity(mContext, requestCode,
                        IntentUtils.getInstallAppIntent(targetFile), PendingIntent.FLAG_UPDATE_CURRENT));// 该通知要启动的Intent
            }
        } else {
            mProgressRemoteViews.setProgressBar(R.id.progressBar, 100, progress, false);

            remoteViews = mProgressRemoteViews;
        }

        remoteViews.setImageViewResource(R.id.image_view, logo);
        remoteViews.setTextViewText(R.id.text_view, title);

        mBuilder.setSmallIcon(logo);// 设置顶部状态栏的小图标, 必须设置.
        mBuilder.setContent(remoteViews);
        mBuilder.setWhen(System.currentTimeMillis());
        mNotificationManager.notify(this.getClass().getSimpleName(), NOTIFICATION_ID, mBuilder.build());

        return NOTIFICATION_ID;
    }

    static class Holder {
        static ProgressNotify holder = new ProgressNotify();
    }

}
