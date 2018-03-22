package com.angcyo.uiview.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;

import com.angcyo.uiview.RApplication;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：本地广播管理类
 * 创建人员：Robi
 * 创建时间：2018/03/22 12:00
 * 修改人员：Robi
 * 修改时间：2018/03/22 12:00
 * 修改备注：
 * Version: 1.0.0
 */
public class RLocalBroadcastManager {

    BroadcastReceiver broadcastReceiver;

    private RLocalBroadcastManager() {
    }

    public static LocalBroadcastManager get() {
        return LocalBroadcastManager.getInstance(RApplication.getApp());
    }

    /**
     * 发送广播
     */
    public static RLocalBroadcastManager sendBroadcast(String action) {
        get().sendBroadcast(new Intent(action));
        return instance();
    }

    public static RLocalBroadcastManager instance() {
        return Holder.instance;
    }

    /**
     * 注册广播
     */
    public RLocalBroadcastManager registerBroadcast(final OnBroadcastReceiver receiver, String... actions) {
        IntentFilter intentFilter = new IntentFilter();
        for (String a : actions) {
            intentFilter.addAction(a);
        }

        if (broadcastReceiver != null) {
            unregisterBroadcast();
        }

        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                receiver.onReceive(context, intent, action);
            }
        };

        get().registerReceiver(broadcastReceiver, intentFilter);
        return instance();
    }
    
    public RLocalBroadcastManager registerBroadcast(final ArrayMap<String, Runnable> actions) {
        IntentFilter intentFilter = new IntentFilter();
        for (String a : actions.keySet()) {
            intentFilter.addAction(a);
        }

        if (broadcastReceiver != null) {
            unregisterBroadcast();
        }

        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                actions.get(action).run();
            }
        };

        get().registerReceiver(broadcastReceiver, intentFilter);
        return instance();
    }

    /**
     * 反注册
     */
    public void unregisterBroadcast() {
        get().unregisterReceiver(broadcastReceiver);
    }

    public interface OnBroadcastReceiver {
        void onReceive(Context context, Intent intent, String action);
    }

    private static class Holder {
        static RLocalBroadcastManager instance = new RLocalBroadcastManager();
    }
}
