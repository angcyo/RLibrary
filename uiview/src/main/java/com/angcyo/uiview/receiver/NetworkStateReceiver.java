package com.angcyo.uiview.receiver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import com.angcyo.github.utilcode.utils.NetworkUtils;
import com.angcyo.library.utils.L;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.net.Rx;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：网络状态监听广播
 * 创建人员：Robi
 * 创建时间：2017/03/10 10:58
 * 修改人员：Robi
 * 修改时间：2017/03/10 10:58
 * 修改备注：
 * Version: 1.0.0
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public static NetworkWrapper sNetworkWrapper = new NetworkWrapper();
    static Set<NetworkStateListener> sListeners = new HashSet<>();
    static Object lock = new Object();
    private static NetworkUtils.NetworkType netType = NetworkUtils.NetworkType.NETWORK_NO;

    public static void addNetworkStateListener(NetworkStateListener listener) {
        synchronized (lock) {
            sListeners.add(listener);
        }
    }

    public static synchronized void removeNetworkStateListener(NetworkStateListener listener) {
        synchronized (lock) {
            sListeners.remove(listener);
        }
    }

    public static void init(Application application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    L.e("NetworkStateReceiver: onAvailable([network])-> ");
                    sNetworkWrapper.network = network;
                }

                @Override
                public void onLosing(Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    L.e("NetworkStateReceiver: onLosing([network, maxMsToLive])-> ");
                    //sNetworkWrapper.network = null;
                    //sNetworkWrapper.lostNetwork = network;
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    L.e("NetworkStateReceiver: onLost([network])-> ");
                    NetworkUtils.NetworkType oldType;
                    if (sNetworkWrapper.network == null) {
                        oldType = NetworkUtils.NetworkType.NETWORK_NO;
                    } else {
                        oldType = NetworkUtils.NetworkType.NETWORK_UNKNOWN;
                    }

                    sNetworkWrapper.network = null;
                    sNetworkWrapper.lostNetwork = network;
                    synchronized (lock) {
                        for (NetworkStateListener listener : sListeners) {
                            listener.onNetworkChange(oldType, NetworkUtils.NetworkType.NETWORK_NO);
                        }
                    }
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    L.e("NetworkStateReceiver: onUnavailable([])-> ");
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    L.e("NetworkStateReceiver: onCapabilitiesChanged([network, networkCapabilities])-> " + networkCapabilities.describeContents());

                    sNetworkWrapper.networkCapabilities = networkCapabilities;
                    sNetworkWrapper.network = network;
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                    L.e("NetworkStateReceiver: onLinkPropertiesChanged([network, linkProperties])-> ");
                    sNetworkWrapper.network = network;
                    sNetworkWrapper.linkProperties = linkProperties;

                    final NetworkUtils.NetworkType oldType;
                    if (sNetworkWrapper.lostNetwork == null) {
                        oldType = NetworkUtils.NetworkType.NETWORK_NO;
                    } else {
                        oldType = NetworkUtils.NetworkType.NETWORK_UNKNOWN;
                    }

                    sNetworkWrapper.lostNetwork = null;
                    synchronized (lock) {
                        Rx.main(new Runnable() {
                            @Override
                            public void run() {
                                for (NetworkStateListener listener : sListeners) {
                                    listener.onNetworkChange(oldType, NetworkUtils.NetworkType.NETWORK_UNKNOWN);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 获取网络类型
     */
    public static NetworkUtils.NetworkType getNetType() {
        //if (netType == NetworkUtils.NetworkType.NETWORK_NO) {
        netType = NetworkUtils.getNetworkType(RApplication.getApp());
        //}
        return netType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        NetworkUtils.NetworkType oldType = netType;

        netType = NetworkUtils.getNetworkType(activeNetworkInfo);

        if (oldType != netType) {
            synchronized (lock) {
                for (NetworkStateListener listener : sListeners) {
                    listener.onNetworkChange(oldType, netType);
                }
            }
        }
        if (netType == NetworkUtils.NetworkType.NETWORK_WIFI) {
            //RxBus.get().post("update", "Wifi");
        }
        L.w("网络变化至:" + netType + " " + (netType.value() > 0 ? "有网" : "没网"));
    }

    public interface NetworkStateListener {
        void onNetworkChange(NetworkUtils.NetworkType from, NetworkUtils.NetworkType to);
    }

    public static class NetworkWrapper {
        public LinkProperties linkProperties;
        public Network network, lostNetwork;
        public NetworkCapabilities networkCapabilities;
    }
}
