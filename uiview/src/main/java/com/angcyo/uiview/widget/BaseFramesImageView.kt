package com.angcyo.uiview.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.R
import com.bumptech.glide.request.RequestOptions

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

    /**循环播放*/
    var loopSwitch = true

    /**循环了第几圈*/
    protected var frameCount = 0

    /**循环结束回调*/
    var onFrameEndCallback: ((Int) -> Unit)? = null

    init {
        animType = AnimType.NONE
        defaultPlaceholderDrawableRes = -1

        skipMemoryCache = false
        noPlaceholderDrawable = true
        override = false
        mShowGifTip = false

        val array = context.obtainStyledAttributes(R.styleable.BaseFramesImageView)
        switchInterval = array.getInteger(R.styleable.BaseFramesImageView_r_frame_switch_interval, switchInterval)
        array.recycle()
    }

    open fun resetDataList(photos: List<T>) {
        dataList.clear()
        dataList.addAll(photos)
        dataIndex = 0
        frameCount = 0
        checkStart()
    }

    private fun checkStart() {
        if (visibility == View.VISIBLE &&
                measuredHeight != 0 &&
                measuredWidth != 0 &&
                dataList.isNotEmpty()) {

            if (dataIndex >= dataList.size) {
                frameCount++
                onFrameEnd(frameCount)
                //结束了
                if (loopSwitch) {
                    dataIndex = 0

                    onDataIndex(dataList[dataIndex], dataIndex)
                    postDelayed(switchRunnable, switchInterval.toLong())
                } else {
                    //需要停留在最后一帧?
                }
            } else {
                onDataIndex(dataList[dataIndex], dataIndex)
                postDelayed(switchRunnable, switchInterval.toLong())
            }
        } else {
            removeCallbacks(switchRunnable)
        }
    }

    /**循环结束*/
    open fun onFrameEnd(frameCount: Int /*第几圈*/) {
        onFrameEndCallback?.invoke(frameCount)
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

    override fun defaultConfig(isGif: Boolean): RequestOptions {
        return super.defaultConfig(isGif).placeholder(drawable).error(drawable)
    }

    abstract fun onDataIndex(data: T?, index: Int)
}