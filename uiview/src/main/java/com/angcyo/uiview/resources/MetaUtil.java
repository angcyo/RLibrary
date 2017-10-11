package com.angcyo.uiview.resources;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/10/11 10:53
 * 修改人员：Robi
 * 修改时间：2017/10/11 10:53
 * 修改备注：
 * Version: 1.0.0
 */
public class MetaUtil {
    public static String getAppMetaData(Context context, String key) {
        String value = "";
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = applicationInfo.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getActivityMetaData(Context context, String activityClass, String key) {
        String value = "";
        try {
            ComponentName componentName = new ComponentName(context.getPackageName(), activityClass);
            ActivityInfo activityInfo = context.getPackageManager()
                    .getActivityInfo(componentName, PackageManager.GET_META_DATA);
            value = activityInfo.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getServiceMetaData(Context context, String serviceClass, String key) {
        String value = "";
        try {
            ComponentName componentName = new ComponentName(context.getPackageName(), serviceClass);
            ServiceInfo serviceInfo = context.getPackageManager()
                    .getServiceInfo(componentName, PackageManager.GET_META_DATA);
            value = serviceInfo.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getReceiverMetaData(Context context, String receiverClass, String key) {
        String value = "";
        try {
            ComponentName componentName = new ComponentName(context.getPackageName(), receiverClass);
            ActivityInfo receiverInfo = context.getPackageManager()
                    .getReceiverInfo(componentName, PackageManager.GET_META_DATA);
            value = receiverInfo.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }
}
