package com.angcyo.uiview.game.layer

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.support.annotation.CallSuper
import android.view.MotionEvent
import com.angcyo.uiview.game.GameRenderView
import com.angcyo.uiview.kotlin.nowTime
import com.angcyo.uiview.utils.ScreenUtil
import java.util.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：游戏渲染的层单位, 一层一层渲染
 * 创建人员：Robi
 * 创建时间：2017/12/15 10:45
 * 修改人员：Robi
 * 修改时间：2017/12/15 10:45
 * 修改备注：
 * Version: 1.0.0
 */
open class BaseLayer {

    protected val random: Random by lazy {
        Random(System.nanoTime())
    }

    /**层, 在游戏View中的坐标区域*/
    var layerRect = Rect()

    var density = ScreenUtil.density()

    /**最后一次有效个绘制时间,用来控制绘制速度*/
    var lastValidDrawTime = 0L
    var lastValidDrawTimeThread = 0L

    /**绘制帧之间的间隔小时, 默认是60帧的速率绘制, 也就是16毫秒 (控制的是所有在上面绘制元素的速率)*/
    var drawIntervalTime = 0L
    var drawIntervalThreadTime = 0L

    /**记录开始渲染的调用时间*/
    protected var onRenderStartTime = 0L

    /**是否需要处理Touch事件, 最上层的Layout会优先回调此方法*/
    open fun onTouchEvent(event: MotionEvent, point: PointF): Boolean {
        return false
    }

    /**主方法1, 在GameRenderView里面回调 此方法会以60帧的速率调用, 用来更新界面*/
    open fun draw(canvas: Canvas, gameStartTime: Long /*最开始渲染的时间*/, lastRenderTime: Long /*上一次渲染的时间*/, nowRenderTime: Long /*现在渲染的时间*/) {
        if (nowRenderTime - lastValidDrawTime > drawIntervalTime) {
            onDraw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
            lastValidDrawTime = nowRenderTime
        }
    }

    /**Layer控速后的回调 可以设置的速率回调*/
    open fun onDraw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {

    }

    @CallSuper
    open fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //L.i("BaseLayer: onSizeChanged -> w:$w h:$h")
        layerRect.set(0, 0, w, h)
    }

    /**结束渲染的回调*/
    @CallSuper
    open fun onRenderEnd(gameRenderView: GameRenderView) {
        isRenderStart = false
    }

    /**是否开始了, 渲染.这个时候 layerRect 是父View的大小 */
    var isRenderStart = false

    lateinit var gameRenderView: GameRenderView

    /** 开始渲染的回调*/
    @CallSuper
    open fun onRenderStart(gameRenderView: GameRenderView) {
        //L.i("call: onRenderStart -> w:${gameRenderView.measuredWidth} h:${gameRenderView.measuredHeight}")
        layerRect.set(0, 0, gameRenderView.measuredWidth, gameRenderView.measuredHeight)
        this.gameRenderView = gameRenderView
        isRenderStart = true
        onRenderStartTime = nowTime()
    }

    /**主方法2, 在GameRenderView里面的子线程回调, 子线程执行, 用来更新界面参数*/
    open fun drawThread(gameStartTime: Long /*最开始渲染的时间*/, lastRenderTimeThread: Long /*上一次渲染的时间*/, nowRenderTime: Long /*现在渲染的时间*/) {
        if (nowRenderTime - lastValidDrawTimeThread > drawIntervalThreadTime) {
            onDrawThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
            lastValidDrawTimeThread = nowRenderTime
        }
    }

    /**子线程控速回调, 子线程执行*/
    open fun onDrawThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {

    }
}
