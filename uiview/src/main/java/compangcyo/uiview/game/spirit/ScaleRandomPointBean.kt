package com.angcyo.uiview.game.spirit

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.Drawable
import com.angcyo.uiview.kotlin.maxValue

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/12/16 13:25
 * 修改人员：Robi
 * 修改时间：2017/12/16 13:25
 * 修改备注：
 * Version: 1.0.0
 */
class ScaleRandomPointBean(val scaleArray: Array<Float> /*缩放比例数组*/,
                           drawable: Drawable /*帧*/,
                           startPoint: Point? = null) : RandomPointBean(arrayOf(drawable), startPoint) {
    override val drawDrawable: Drawable
        get() = drawableArray[0]

    init {
        frameSize = scaleArray.size
        frameDrawIntervalTime = 100L /*30帧速率切换*/
        delayDrawTime = (random.nextInt(10) * 100).toLong()
    }

    override fun onFrameOnDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        scaleX = scaleArray[frameIndex.maxValue(frameSize - 1)]
        scaleY = scaleArray[frameIndex.maxValue(frameSize - 1)]
        super.onFrameOnDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
    }
}