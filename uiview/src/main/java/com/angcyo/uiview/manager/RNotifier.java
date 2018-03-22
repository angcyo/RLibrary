/************************************************************
 *  * Hyphenate CONFIDENTIAL 
 * __________________ 
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved. 
 *
 * NOTICE: All information contained herein is, and remains 
 * the property of Hyphenate Inc.
 * Dissemination of this information or reproduction of this material 
 * is strictly forbidden unless prior written permission is obtained
 * from Hyphenate Inc.
 */
package com.angcyo.uiview.manager;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import com.angcyo.library.utils.L;

import java.util.HashSet;
import java.util.Locale;

/**
 * new message notifier class
 * <p>
 * this class is subject to be inherited and implement the relative APIs
 */
public class RNotifier {
    private final static String TAG = "RNotifier";
    protected static int notifyID = 0525; // start notification id
    protected static int foregroundNotifyID = 0555;
    protected NotificationManager notificationManager = null;
    protected HashSet<String> fromUsers = new HashSet<String>();
    protected int notificationNum = 0;
    protected Context appContext;
    protected String packageName;
    protected String[] msgs;
    protected long lastNotifiyTime;
    protected AudioManager audioManager;
    protected Vibrator vibrator;
    Ringtone ringtone = null;

    private RNotifier() {
    }

    public static RNotifier instance() {
        return Holder.instance;
    }

    /**
     * this function can be override
     *
     * @param context
     * @return
     */
    public RNotifier init(Context context) {
        appContext = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        packageName = appContext.getApplicationInfo().packageName;
        if (Locale.getDefault().getLanguage().equals("zh")) {
        } else {
        }

        //音频管理
        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        //震动管理
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);

        return this;
    }

    /**
     * this function can be override
     */
    public void reset() {
        resetNotificationCount();
        cancelNotificaton();
    }

    void resetNotificationCount() {
        notificationNum = 0;
        fromUsers.clear();
    }

    void cancelNotificaton() {
        if (notificationManager != null)
            notificationManager.cancel(notifyID);
    }


    /**
     * send it to notification bar
     * This can be override by subclass to provide customer implementation
     */
    protected void sendNotification(String content, boolean isForeground, boolean numIncrease) {
//        String username = message.getFrom();
//        try {
//            String notifyText = username + " ";
//            switch (message.getType()) {
//                case TXT:
//                    notifyText += msgs[0];
//                    break;
//                case IMAGE:
//                    notifyText += msgs[1];
//                    break;
//                case VOICE:
//
//                    notifyText += msgs[2];
//                    break;
//                case LOCATION:
//                    notifyText += msgs[3];
//                    break;
//                case VIDEO:
//                    notifyText += msgs[4];
//                    break;
//                case FILE:
//                    notifyText += msgs[5];
//                    break;
//            }
//
//            PackageManager packageManager = appContext.getPackageManager();
//            String appname = (String) packageManager.getApplicationLabel(appContext.getApplicationInfo());
//
//            // notification title
//            String contentTitle = appname;
//            if (notificationInfoProvider != null) {
//                String customNotifyText = notificationInfoProvider.getDisplayedText(message);
//                String customCotentTitle = notificationInfoProvider.getTitle(message);
//                if (customNotifyText != null) {
//                    notifyText = customNotifyText;
//                }
//
//                if (customCotentTitle != null) {
//                    contentTitle = customCotentTitle;
//                }
//            }
//
//            // create and send notificaiton
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext)
//                    .setSmallIcon(appContext.getApplicationInfo().icon)
//                    .setWhen(System.currentTimeMillis())
//                    .setAutoCancel(true);
//
//            Intent msgIntent = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
//            if (notificationInfoProvider != null) {
//                msgIntent = notificationInfoProvider.getLaunchIntent(message);
//            }
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(appContext, notifyID, msgIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            if (numIncrease) {
//                // prepare latest event info section
//                if (!isForeground) {
//                    notificationNum++;
//                    fromUsers.add(message.getFrom());
//                }
//            }
//
//            int fromUsersNum = fromUsers.size();
//            String summaryBody = msgs[6].replaceFirst("%1", Integer.toString(fromUsersNum)).replaceFirst("%2", Integer.toString(notificationNum));
//
//            if (notificationInfoProvider != null) {
//                // lastest text
//                String customSummaryBody = notificationInfoProvider.getLatestText(message, fromUsersNum, notificationNum);
//                if (customSummaryBody != null) {
//                    summaryBody = customSummaryBody;
//                }
//
//                // small icon
//                int smallIcon = notificationInfoProvider.getSmallIcon(message);
//                if (smallIcon != 0) {
//                    mBuilder.setSmallIcon(smallIcon);
//                }
//            }
//
//            mBuilder.setContentTitle(contentTitle);
//            mBuilder.setTicker(notifyText);
//            mBuilder.setContentText(summaryBody);
//            mBuilder.setContentIntent(pendingIntent);
//            // mBuilder.setNumber(notificationNum);
//            Notification notification = mBuilder.build();
//
//            if (isForeground) {
//                notificationManager.notify(foregroundNotifyID, notification);
//                notificationManager.cancel(foregroundNotifyID);
//            } else {
//                notificationManager.notify(notifyID, notification);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * vibrate and  play tone
     */
    public void vibrateAndPlayTone() {
        if (System.currentTimeMillis() - lastNotifiyTime < 1000) {
            // received new messages within 2 seconds, skip play ringtone
            return;
        }

        try {
            lastNotifiyTime = System.currentTimeMillis();

            // check if in silent mode
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                L.e(TAG, "in slient mode now");
                return;
            }

            //开始震动
            long[] pattern = new long[]{0, 180, 80, 120};
            vibrator.vibrate(pattern, -1);

            //播放铃声
            if (ringtone == null) {
                Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                ringtone = RingtoneManager.getRingtone(appContext, notificationUri);
                if (ringtone == null) {
                    L.d(TAG, "cant find ringtone at:" + notificationUri.getPath());
                    return;
                }
            }

            if (!ringtone.isPlaying()) {
                String vendor = Build.MANUFACTURER;

                ringtone.play();
                // for samsung S3, we meet a bug that the phone will
                // continue ringtone without stop
                // so add below special handler to stop it after 3s if
                // needed
                if (vendor != null && vendor.toLowerCase().contains("samsung")) {
                    Thread ctlThread = new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(3000);
                                if (ringtone.isPlaying()) {
                                    ringtone.stop();
                                }
                            } catch (Exception e) {
                            }
                        }
                    };
                    ctlThread.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Holder {
        static RNotifier instance = new RNotifier();
    }

}
