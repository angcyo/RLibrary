package com.angcyo.uiview.widget

import android.content.Context
import android.util.AttributeSet
import com.angcyo.uiview.RApplication
import com.bumptech.glide.Glide
import java.io.File

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：可以加载本地图片, 网络图片, 自动播放帧
 * 创建人员：Robi
 * 创建时间：2018/03/26 11:54
 * 修改人员：Robi
 * 修改时间：2018/03/26 11:54
 * 修改备注：
 * Version: 1.0.0
 */
open class RFramesImageView(context: Context, attributeSet: AttributeSet? = null)
    : BaseFramesImageView<String>(context, attributeSet) {

    companion object {
        /**
         * 预加载
         */
        fun preload(images: List<String>) {
            for (p in images) {
                if (File(p).exists()) {
                    Glide.with(RApplication.getApp()).load(File(p)).preload()
                } else {
                    Glide.with(RApplication.getApp()).load(p).preload()
                }
            }
        }
    }

    init {
        loopSwitch = true
        switchInterval = 60
    }

    override fun resetDataList(photos: List<String>) {
        //preload(photos)
        super.resetDataList(photos)
    }

    override fun onDataIndex(data: String?, index: Int) {
        data?.let {
            url = it
        }
    }
}