package com.angcyo.uiview.game.spirit

import android.graphics.Canvas
import android.graphics.drawable.Drawable

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：全屏背景
 * 创建人员：Robi
 * 创建时间：2017/12/16 12:19
 * 修改人员：Robi
 * 修改时间：2017/12/16 12:19
 * 修改备注：
 * Version: 1.0.0
 */
open class FrameBgBean(val bgDrawable: Drawable) : BaseLayerBean() {
    override fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long, onDrawEnd: (() -> Unit)?) {
        super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime, onDrawEnd)
        bgDrawable.let {
            it.bounds = parentRect
            it.draw(canvas)
        }
    }
}