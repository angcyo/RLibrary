package com.angcyo.uiview;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.angcyo.github.utilcode.utils.Utils;
import com.angcyo.library.utils.L;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.utils.Debug;
import com.angcyo.uiview.utils.T_;
import com.angcyo.uiview.view.UIIViewImpl;
import com.bumptech.glide.Glide;
import com.orhanobut.hawk.Hawk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
    public static String getIMEI() {
        String imei = "Unknown";
        try {
            imei = ((TelephonyManager) getApp()
                    .getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceId();
            L.e("call: getIMEI([])-> " + imei);
        } catch (Exception e) {
            e.printStackTrace();
            L.e("call: getIMEI([])-> " + imei + " " + e.getMessage());
        }
        return imei;
    }

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

        if (isInitOnce(this)) {

            Debug.logTimeStart("RApplication 正在初始化:isInitOnce()");

            onBaseInit();

            Debug.logTimeEnd("RApplication 初始化结束:isInitOnce()");
        }
    }

    protected void onBaseInit() {
        isLowDevice = UIIViewImpl.isLowDevice();
        isHighDevice = UIIViewImpl.isHighDevice();

        try {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            memoryClass = manager.getMemoryClass();
            largeMemoryClass = manager.getLargeMemoryClass();
        } catch (Exception e) {

        }
        //96 256
        L.e("RApplication -> memoryClass:" + memoryClass + "MB largeMemoryClass:" + largeMemoryClass + "MB");

        Utils.init(this);

             /*sp持久化库*/
        Hawk.init(this)
                .build();

            /*崩溃异常处理*/
        RCrashHandler.init(this);

        SkinHelper.init(this);

            /*Realm数据库初始化*/
        //RRealm.init(this, "r_jcenter.realm", true);

            /*Facebook图片加载库, 必须*/
        //Fresco.initialize(this);

        onInit();

        new Thread() {
            @Override
            public void run() {
                Debug.logTimeStart("RApplication 异步初始化:onAsyncInit()");
                onAsyncInit();
                Debug.logTimeEnd("RApplication 异步初始化结束:onAsyncInit()");
            }
        }.start();
    }

    /**
     * 异步初始化
     */
    protected void onAsyncInit() {

    }

    /**
     * 同步初始化
     */
    protected void onInit() {

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
}
