package com.angcyo.uiview;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.angcyo.github.utilcode.utils.AppUtils;
import com.angcyo.github.utilcode.utils.Utils;
import com.angcyo.library.utils.L;
import com.angcyo.uiview.manager.RNotifier;
import com.angcyo.uiview.net.Rx;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.utils.Debug;
import com.angcyo.uiview.utils.ScreenUtil;
import com.angcyo.uiview.utils.T_;
import com.angcyo.uiview.view.UIIViewImpl;
import com.bumptech.glide.Glide;
import com.orhanobut.hawk.Hawk;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import rx.functions.Func1;

/**
 * Created by robi on 2016-06-21 11:24.
 * Copyright (c) 2016, angcyo@126.com All Rights Reserved.
 * *                                                   #
 * #                                                   #
 * #                       _oo0oo_                     #
 * #                      o8888888o                    #
 * #                      88" . "88                    #
 * #                      (| -_- |)                    #
 * #                      0\  =  /0                    #
 * #                    ___/`---'\___                  #
 * #                  .' \\|     |# '.                 #
 * #                 / \\|||  :  |||# \                #
 * #                / _||||| -:- |||||- \              #
 * #               |   | \\\  -  #/ |   |              #
 * #               | \_|  ''\---/''  |_/ |             #
 * #               \  .-\__  '-'  ___/-. /             #
 * #             ___'. .'  /--.--\  `. .'___           #
 * #          ."" '<  `.___\_<|>_/___.' >' "".         #
 * #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 * #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 * #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 * #                       `=---='                     #
 * #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 * #                                                   #
 * #               佛祖保佑         永无BUG              #
 * #                                                   #
 */

public class RApplication extends Application {

    public static boolean isLowDevice = true;
    public static boolean isHighDevice = false;

    public static int memoryClass = 0;
    public static int largeMemoryClass = 0;

    public static RApplication app;

    /**
     * 获取设备唯一标识码, 需要权限 android.permission.READ_PHONE_STATE
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getIMEI() {
        String imei = "Unknown";
        try {
            TelephonyManager telephonyManager = ((TelephonyManager) getApp()
                    .getSystemService(Context.TELEPHONY_SERVICE));

            if (telephonyManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = telephonyManager.getImei();
                } else {
                    imei = telephonyManager.getDeviceId();
                }
            }
            //L.w("call: getIMEI([])-> " + imei);
        } catch (Exception e) {
            L.e("IMEI获取失败, 请检查权限:" + e.getMessage());
            //e.printStackTrace();
            //L.e("call: getIMEI([])-> " + imei + " " + e.getMessage());
        }
        return imei;
    }

    public static RApplication getApp() {
//        if (app == null) {
//            throw new IllegalArgumentException("application 初始化了吗?");
//        }
        return app;
    }

    public static void clearGlide() {
        Glide.get(getApp()).clearMemory();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        if (Util.isInitOnce(this)) {

            Debug.logTimeStart("RApplication 正在初始化:isInitOnce()");

            onBaseInit();

            Debug.logTimeEnd("RApplication 初始化结束:isInitOnce()");
        }
    }

    protected void onBaseInit() {
        onInit();

        Rx.base(new Func1<String, String>() {
            @Override
            public String call(String s) {
                Debug.logTimeStart("RApplication 异步初始化:onAsyncInit()");
                onAsyncInit();
                Debug.logTimeEnd("RApplication 异步初始化结束:onAsyncInit()");
                return null;
            }
        });
    }

    /**
     * 异步初始化
     */
    protected void onAsyncInit() {
        RNotifier.instance().init(this);

        /*Realm数据库初始化*/
        //RRealm.init(this, "r_jcenter.realm", true);

        /*Facebook图片加载库, 必须*/
        //Fresco.initialize(this);

        if (BuildConfig.DEBUG) {
            try {
                ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                memoryClass = manager.getMemoryClass();
                largeMemoryClass = manager.getLargeMemoryClass();
            } catch (Exception e) {

            }
            //96 256
            L.e("RApplication -> memoryClass:" + memoryClass + "MB largeMemoryClass:" + largeMemoryClass + "MB");

            L.e("onAsyncInit([])-> 签名MD5:" + AppUtils.getAppSignatureMD5(this));
            L.e("onAsyncInit([])-> 签名SHA1:" + AppUtils.getAppSignatureSHA1(this));
            L.e("onAsyncInit([])-> IMEI:" + getIMEI());
        }
    }

    /**
     * 同步初始化
     */
    protected void onInit() {
        initDefaultNotificationChannel();

        SkinHelper.init(this);
        Utils.init(this);
        ScreenUtil.init(this);

        /*sp持久化库*/
        Hawk.init(this)
                .build();

        /*崩溃异常处理*/
        RCrashHandler.init(this);

        isLowDevice = UIIViewImpl.isLowDevice();
        isHighDevice = UIIViewImpl.isHighDevice();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        app = this;
        /*65535限制*/
        MultiDex.install(this);
    }

    @Override
    public void onLowMemory() {
        if (BuildConfig.DEBUG) {
            T_.show("请注意, 内存过低!");
        }
        Glide.get(this).clearMemory();
        //Fresco.shutDown();
//        if (Fresco.hasBeenInitialized()) {
//            Fresco.getImagePipeline().clearMemoryCaches();
//        }
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
//        if (BuildConfig.DEBUG) {
//            T_.show("请求释放内存..." + level);
//        }
        Glide.get(this).trimMemory(level);
        Glide.get(this).clearMemory();
//        if (Fresco.hasBeenInitialized()) {
//            Fresco.getImagePipeline().clearMemoryCaches();
//        }
        super.onTrimMemory(level);
    }

    /*用包名创建一个默认的通知通道*/
    protected void initDefaultNotificationChannel() {
        //NotificationManagerCompat compat = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                String packageName = this.getPackageName();
                NotificationChannel notificationChannel = new NotificationChannel(packageName,
                        packageName,
                        NotificationManager.IMPORTANCE_NONE);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                manager.createNotificationChannel(notificationChannel);
            }
        }
    }

    public static class Util {

        /**
         * 确保只初始化一次
         */
        public static boolean isInitOnce(Application application) {

            int pid = android.os.Process.myPid();
            String processAppName = getAppName(application, pid);
            // 如果APP启用了远程的service，此application:onCreate会被调用2次
            // 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
            // 默认的APP会在以包名为默认的process name下运行，如果查到的process name不是APP的process name就立即返回

            if (processAppName == null || !processAppName.equalsIgnoreCase(application.getPackageName())) {
                // 则此application::onCreate 是被service 调用的，直接返回
                return false;
            }

            return true;
        }

        public static boolean isMainProcess(Context context) {
            if (context == null) {
                return false;
            }

            String packageName = context.getApplicationContext().getPackageName();
            String processName = getProcessName(context);
            return packageName.equals(processName);
        }

        public static String getProcessName(Context context) {
            String processName = getProcessFromFile();
            if (processName == null) {
                // 如果装了xposed一类的框架，上面可能会拿不到，回到遍历迭代的方式
                processName = getProcessNameByAM(context);
            }
            return processName;
        }

        private static String getProcessFromFile() {
            BufferedReader reader = null;
            try {
                int pid = android.os.Process.myPid();
                String file = "/proc/" + pid + "/cmdline";
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "iso-8859-1"));
                int c;
                StringBuilder processName = new StringBuilder();
                while ((c = reader.read()) > 0) {
                    processName.append((char) c);
                }
                return processName.toString();
            } catch (Exception e) {
                return null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private static String getProcessNameByAM(Context context) {
            String processName = null;

            ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
            if (am == null) {
                return null;
            }

            while (true) {
                List<ActivityManager.RunningAppProcessInfo> plist = am.getRunningAppProcesses();
                if (plist != null) {
                    for (ActivityManager.RunningAppProcessInfo info : plist) {
                        if (info.pid == android.os.Process.myPid()) {
                            processName = info.processName;

                            break;
                        }
                    }
                }

                if (!TextUtils.isEmpty(processName)) {
                    return processName;
                }

                try {
                    Thread.sleep(100L); // take a rest and again
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public static boolean isMainProcessLive(Context context) {
            if (context == null) {
                return false;
            }

            final String processName = context.getPackageName();
            ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
            if (am != null) {
                List<ActivityManager.RunningAppProcessInfo> plist = am.getRunningAppProcesses();
                if (plist != null) {
                    for (ActivityManager.RunningAppProcessInfo info : plist) {
                        if (info.processName.equals(processName)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        /**
         * 获取进程号对应的进程名
         *
         * @param pid 进程号  android.os.Process.myPid()
         * @return 进程名
         */
        private static String getProcessName(int pid) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
                String processName = reader.readLine();
                if (!TextUtils.isEmpty(processName)) {
                    processName = processName.trim();
                }
                return processName;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
            return null;
        }

        private static String getAppName(Application application, int pID) {
            String processName = null;
            ActivityManager am = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
            List l = am.getRunningAppProcesses();
            Iterator i = l.iterator();
            PackageManager pm = application.getPackageManager();
            while (i.hasNext()) {
                ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
                try {
                    if (info.pid == pID) {
                        processName = info.processName;
                        return processName;
                    }
                } catch (Exception e) {
                    // Log.d("Process", "Error>> :"+ e.toString());
                }
            }
            return processName;
        }
    }
}
