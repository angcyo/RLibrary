package com.angcyo.picker.media

import android.app.Activity
import android.text.TextUtils
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.uiview.net.RFunc
import com.angcyo.uiview.net.RSubscriber
import com.angcyo.uiview.net.Rx
import com.angcyo.uiview.recycler.adapter.RBaseAdapter
import com.angcyo.uiview.utils.media.BitmapDecoder
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

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
object ThumbLoad {
    /**
     * 保存缓存的缩略图路径
     */
    internal var thumbMap: WeakHashMap<String, String>? = null
    /**
     * 缩略图是否正在加载
     */
    internal var thumbMapLoad: WeakHashMap<String, Boolean>? = null

    fun createThumbFile(activityWeakReference: WeakReference<Activity>,
                        adapterWeakReference: WeakReference<RBaseAdapter<MediaItem>>,
                        item: MediaItem) {

        if (thumbMap == null) {
            thumbMap = WeakHashMap()
        }
        if (thumbMapLoad == null) {
            thumbMapLoad = WeakHashMap()
        }

        val thumbFile = File(item.videoThumbPath)
        val temp = thumbMap!![item.path]
        if (!TextUtils.isEmpty(temp) && File(temp).exists()) {
            return
        }

        if (!thumbFile.exists()) {
            val aBoolean = thumbMapLoad!![item.path]
            if (aBoolean != null && aBoolean) {
                //已经在加载, 退出不处理
                return
            }

            thumbMapLoad!![item.path] = true

            Rx.base(object : RFunc<Boolean>() {
                override fun onFuncCall(): Boolean {
                    val activity = activityWeakReference.get()
                    if (activity == null) {
                        thumbMapLoad!![item.path] = false
                        return false
                    }

                    val thumbnail = BitmapDecoder.extractThumbnail(item.path, item.videoThumbPath)
                    if (thumbnail) {
                        thumbMap!![item.path] = item.videoThumbPath
                        return true
                    }
                    return false
                }

            }, object : RSubscriber<Boolean>() {
                override fun onSucceed(bean: Boolean) {
                    super.onSucceed(bean)
                    thumbMapLoad!![item.path] = false
                    if (bean) {
                        val adapter = adapterWeakReference.get()
                        adapter?.notifyItemChanged(item)
                    }
                }
            })
        }
    }
}
