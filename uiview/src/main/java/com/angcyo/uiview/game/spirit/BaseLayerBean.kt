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

    /**
     *  每一帧绘制间隔时间
     *  注意: 此间隔 是在 BaseLayer drawIntervalTime = 0 作用下的
     *  */
    var frameDrawIntervalTime = 0L

    protected var lastFrameOnDrawTime = 0L
    protected var lastFrameOnDrawTimeThread = 0L
    protected var lastFrameDrawTime = 0L
    protected var lastFrameDrawTimeThread = 0L

    /**Layer的显示区域范围*/
    var parentRect = Rect()

    protected val random: Random by lazy {
        Random(System.nanoTime())
    }

    /**此回调一定是60帧速率*/
    open fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long, onDrawEnd: (() -> Unit)? = null) {
        if (nowRenderTime - lastFrameDrawTime > frameDrawIntervalTime) {
            lastFrameDrawTime = nowRenderTime
            onFrameDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        }
    }

    /**此回调会收到BaseLayer drawIntervalTime 的影响*/
    open fun onDraw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        if (nowRenderTime - lastFrameOnDrawTime > frameDrawIntervalTime) {
            lastFrameOnDrawTime = nowRenderTime
            onFrameOnDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        }
    }

    open fun onFrameOnDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {

    }

    open fun onFrameDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {

    }

    /**子线程执行*/
    open fun drawThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {
        if (nowRenderTime - lastFrameDrawTimeThread > frameDrawIntervalTime) {
            lastFrameDrawTimeThread = nowRenderTime
            onFrameDrawIntervalThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
        }
    }

    /**子线程执行*/
    open fun onDrawThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {
        if (nowRenderTime - lastFrameOnDrawTimeThread > frameDrawIntervalTime) {
            lastFrameOnDrawTimeThread = nowRenderTime
            onFrameOnDrawIntervalThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
        }
    }

    /**子线程执行*/
    open fun onFrameOnDrawIntervalThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {

    }

    /**子线程执行*/
    open fun onFrameDrawIntervalThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {

    }
}
