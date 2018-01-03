package com.angcyo.uiview.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.R

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/01/03 15:05
 * 修改人员：Robi
 * 修改时间：2018/01/03 15:05
 * 修改备注：
 * Version: 1.0.0
 */
abstract class BaseFramesImageView<T>(context: Context, attributeSet: AttributeSet? = null) : GlideImageView(context, attributeSet) {

    /**需要切换的图片列表*/
    private val dataList = mutableListOf<T>()

    /**切换间隔时间*/
    var switchInterval = 160

    private var dataIndex = 0
        set(value) {
            field = if (value >= dataList.size) {
                0
            } else {
                value
            }
        }

    init {
        animType = AnimType.CROSS_FADE
        defaultPlaceholderDrawableRes = -1

        val array = context.obtainStyledAttributes(R.styleable.BaseFramesImageView)
        switchInterval = array.getInteger(R.styleable.BaseFramesImageView_r_frame_switch_interval, switchInterval)
        array.recycle()
    }

    fun resetDataList(photos: List<T>) {
        dataList.clear()
        dataList.addAll(photos)
        dataIndex = 0
        checkStart()
    }

    private fun checkStart() {
        if (visibility == View.VISIBLE &&
                measuredHeight != 0 &&
                measuredWidth != 0 &&
                dataList.isNotEmpty()) {
            onDataIndex(dataList[dataIndex], dataIndex)
            postDelayed(switchRunnable, switchInterval.toLong())
        } else {
            removeCallbacks(switchRunnable)
        }
    }

    private val switchRunnable = Runnable {
        dataIndex++
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

    abstract fun onDataIndex(data: T?, index: Int)
}