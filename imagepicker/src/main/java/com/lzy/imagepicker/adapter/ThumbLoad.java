package com.lzy.imagepicker.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.lzy.imagepicker.Utils;
import com.lzy.imagepicker.bean.ImageItem;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：视频缩略图加载, 缓存
 * 创建人员：Robi
 * 创建时间：2017/06/15 16:59
 * 修改人员：Robi
 * 修改时间：2017/06/15 16:59
 * 修改备注：
 * Version: 1.0.0
 */
public class ThumbLoad {
    /**
     * 保存缓存的缩略图路径
     */
    static WeakHashMap<String, String> thumbMap;
    /**
     * 缩略图是否正在加载
     */
    static WeakHashMap<String, Boolean> thumbMapLoad;

    public static void createThumbFile(final WeakReference<Activity> activityWeakReference,
                                       final WeakReference<RecyclerView.Adapter> adapterWeakReference,
                                       final ImageItem item, final int position) {
        if (thumbMap == null) {
            thumbMap = new WeakHashMap<>();
        }
        if (thumbMapLoad == null) {
            thumbMapLoad = new WeakHashMap<>();
        }

        File thumbFile = new File(item.videoThumbPath);
        String temp = thumbMap.get(item.path);
        if (!TextUtils.isEmpty(temp) && new File(temp).exists()) {
            item.videoThumbPath = temp;
            return;
        }

        if (!thumbFile.exists()) {
            Boolean aBoolean = thumbMapLoad.get(item.path);
            if (aBoolean != null && aBoolean) {
                //已经在加载, 退出不处理
                return;
            }

            thumbMapLoad.put(item.path, true);

            ThreadExecutor.instance().onThread(new Runnable() {
                @Override
                public void run() {
                    Activity activity = activityWeakReference.get();
                    if (activity == null) {
                        thumbMapLoad.put(item.path, false);
                        return;
                    }

                    item.videoThumbPath = activity.getCacheDir().getAbsolutePath() + File.separator + UUID.randomUUID().toString();
                    boolean thumbnail = Utils.extractThumbnail(item.path, item.videoThumbPath);
                    if (!thumbnail) {
                        thumbMapLoad.put(item.path, false);
                        return;
                    }
                    thumbMap.put(item.path, item.videoThumbPath);

                    ThreadExecutor.instance().onMain(new Runnable() {
                        @Override
                        public void run() {
                            thumbMapLoad.put(item.path, false);
                            RecyclerView.Adapter adapter = adapterWeakReference.get();
                            if (adapter != null) {
                                adapter.notifyItemChanged(position);
                            }
                        }
                    });
                }
            });
        }
    }
}
