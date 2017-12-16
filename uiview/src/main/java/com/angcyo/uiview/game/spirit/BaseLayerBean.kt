package com.angcyo.uiview.game.spirit

import android.graphics.Canvas
import android.graphics.Rect
import java.util.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：精灵绘制
 * 创建人员：Robi
 * 创建时间：2017/12/16 12:18
 * 修改人员：Robi
 * 修改时间：2017/12/16 12:18
 * 修改备注：
 * Version: 1.0.0
 */
open class BaseLayerBean {

    /**每一帧绘制间隔时间*/
    var frameDrawIntervalTime = 0L
    protected var lastFrameDrawTime = 0L

    /**Layer的显示区域范围*/
    var parentRect = Rect()

    protected val random: Random by lazy {
        Random(System.nanoTime())
    }

    open fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long, onDrawEnd: (() -> Unit)? = null) {
    }

    open fun onDraw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        if (nowRenderTime - lastFrameDrawTime > frameDrawIntervalTime) {
            lastFrameDrawTime = nowRenderTime
            onFrameDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        }
    }

    open fun onFrameDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {

    }
}