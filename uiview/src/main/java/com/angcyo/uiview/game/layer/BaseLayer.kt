package com.angcyo.uiview.game.layer

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.view.MotionEvent
import com.angcyo.uiview.game.GameRenderView

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

    /**层, 在游戏View中的坐标区域*/
    var layerRect = Rect()

    /**是否需要处理Touch事件, 最上层的Layout会优先回调此方法*/
    open fun onTouchEvent(event: MotionEvent, point: PointF): Boolean {
        return false
    }

    /**此方法会以60帧的速率调用*/
    open fun draw(canvas: Canvas, gameStartTime: Long /*最开始渲染的时间*/, lastRenderTime: Long /*上一次渲染的时间*/, nowRenderTime: Long /*现在渲染的时间*/) {

    }

    open fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        layerRect.set(0, 0, w, h)
    }

    /**结束渲染的回调*/
    open fun onRenderEnd(gameRenderView: GameRenderView) {

    }

    /**开始渲染的回调*/
    open fun onRenderStart(gameRenderView: GameRenderView) {

    }
}