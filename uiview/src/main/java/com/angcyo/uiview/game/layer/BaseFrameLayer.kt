package com.angcyo.uiview.game.layer

import android.graphics.Canvas
import com.angcyo.uiview.game.spirit.BaseLayerBean
import java.util.concurrent.CopyOnWriteArrayList

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

    protected val frameList = CopyOnWriteArrayList<BaseLayerBean>()
    protected val lock = Object()

    /**暂停绘制*/
    @Volatile
    var pauseDrawFrame = false

    @Volatile
    var pauseDrawFrameThread = false

    init {
        //drawIntervalTime = 100
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        if (!pauseDrawFrame) {
            super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
            val deleteList = mutableListOf<BaseLayerBean>()
            synchronized(lock) {
                for (frame in frameList) {
                    frame.parentRect.set(layerRect)
                    frame.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime) {
                        deleteList.add(frame)
                    }
                }
                frameList.removeAll(deleteList)
            }
        }
    }

    override fun onDraw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.onDraw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        synchronized(lock) {
            for (frame in frameList) {
                frame.onDraw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
            }
        }
    }

    open fun addFrameBean(frameBean: BaseLayerBean) {
        synchronized(lock) {
            frameList.add(frameBean)
        }
    }

    override fun drawThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {
        if (!pauseDrawFrameThread) {
            super.drawThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
            synchronized(lock) {
                for (frame in frameList) {
                    frame.drawThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
                }
            }
        }
    }

    override fun onDrawThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {
        super.onDrawThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
        synchronized(lock) {
            for (frame in frameList) {
                frame.onDrawThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
            }
        }
    }
}




