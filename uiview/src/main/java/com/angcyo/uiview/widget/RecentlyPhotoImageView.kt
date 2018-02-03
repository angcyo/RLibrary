package com.angcyo.uiview.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：新照片提醒
 * 创建人员：Robi
 * 创建时间：2017/12/26 16:09
 * 修改人员：Robi
 * 修改时间：2017/12/26 16:09
 * 修改备注：
 * Version: 1.0.0
 */
class RecentlyPhotoImageView(context: Context, attributeSet: AttributeSet? = null) : GlideImageView(context, attributeSet) {

    /**需要切换的图片列表*/
    private val photoList = mutableListOf<String>()

    /**切换间隔时间*/
    var switchInterval = 1300L

    private var photoIndex = 0
        set(value) {
            field = if (value >= photoList.size) {
                0
            } else {
                value
            }
        }

    init {
        animType = AnimType.CROSS_FADE
    }

    fun addPhotoList(photos: List<String>) {
        removeCallbacks(switchRunnable)
        photoList.clear()
        photoList.addAll(photos)
        photoIndex = 0
        checkStart()
    }

    private fun checkStart() {
        if (visibility == View.VISIBLE &&
                measuredHeight != 0 &&
                measuredWidth != 0 &&
                photoList.isNotEmpty()) {
            url = photoList[photoIndex]
            removeCallbacks(switchRunnable)
            postDelayed(switchRunnable, switchInterval)
        } else {
            removeCallbacks(switchRunnable)
        }
    }

    private val switchRunnable = Runnable {
        photoIndex++
        checkStart()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        checkStart()
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        checkStart()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        checkStart()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        checkStart()
    }
}
