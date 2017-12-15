package com.angcyo.uiview.game.layer

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.angcyo.uiview.kotlin.getBoundsWith

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来播放帧动画的Layer
 * 创建人员：Robi
 * 创建时间：2017/12/15 14:46
 * 修改人员：Robi
 * 修改时间：2017/12/15 14:46
 * 修改备注：
 * Version: 1.0.0
 */
open class BaseFrameLayer : BaseLayer() {

    private val frameList = mutableListOf<FrameBean>()

    init {
        drawIntervalTime = 100
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        val deleteList = mutableListOf<FrameBean>()
        for (frame in frameList) {
            frame.parentRect.set(layerRect)
            frame.draw(canvas) {
                deleteList.add(frame)
            }
        }
        frameList.removeAll(deleteList)
    }

    override fun onDraw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.onDraw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        for (frame in frameList) {
            frame.onDraw(canvas)
        }
    }

    fun addFrameBean(frameBean: FrameBean) {
        frameList.add(frameBean)
    }
}

/**数据bean*/
open class FrameBean(val drawableArray: Array<Drawable> /*需要播放的帧动画*/, val centerPoint: Point /*需要在什么位置播放(中心点)*/) {

    /**Layer的显示区域范围*/
    var parentRect = Rect()

    /**是否循环播放*/
    var loop = true

    /*总共多少帧*/
    private val frameSize = drawableArray.size
    /*当前播放到多少帧*/
    private var frameIndex = 0

    open fun draw(canvas: Canvas, onDrawEnd: () -> Unit) {
        if (frameIndex >= frameSize) {
            //播放结束
            if (loop) {
                frameIndex = 0
            } else {
                onDrawEnd.invoke()
            }
        }
        if (frameIndex < frameSize) {
            drawableArray[frameIndex].let {
                it.bounds = it.getBoundsWith(centerPoint, parentRect)
                it.draw(canvas)
            }
        }
    }

    open fun onDraw(canvas: Canvas) {
        frameIndex++
    }
}