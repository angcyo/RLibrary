package com.angcyo.uiview.game.spirit

import android.graphics.Point
import android.graphics.drawable.Drawable

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：透明度变化的精灵 (MoveBean 也有透明度变化)
 * 创建人员：Robi
 * 创建时间：2018/01/17 12:02
 * 修改人员：Robi
 * 修改时间：2018/01/17 12:02
 * 修改备注：
 * Version: 1.0.0
 */
open class AlphaSpiritBean : FrameBean {

    /**透明变化列表*/
    private var alphaList: List<Int>

    private var alphaIndex = 0

    /**每次透明度变化的值, 大于0的值*/
    var alphaStep = 40

    /**是否循环变化*/
    var loopAlpha = false

    constructor(drawableArray: Array<Drawable>, centerPoint: Point, alphaList: List<Int> /*0-255*/) : super(drawableArray, centerPoint) {
        this.alphaList = alphaList
        loopDrawFrame = true

        /*透明度更新频率*/
        //frameDrawThreadIntervalTime = 60

        if (alphaList.isNotEmpty()) {
            drawableAlpha = alphaList[0] //开始绘制时的透明度, 取第一个
            if (haveNext()) {
                alphaIndex++ //透明度的下一个目标
            }
        }
    }

    override fun onFrameOnDrawIntervalThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {
        super.onFrameOnDrawIntervalThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
        if (alphaList.size > 1) {
            //更新透明度
            val curAlpha = drawableAlpha
            val nextAlpha = alphaList[alphaIndex]

            if (nextAlpha > curAlpha) {
                //值需要+
                drawableAlpha = Math.min(curAlpha + alphaStep, nextAlpha)
            } else if (nextAlpha < curAlpha) {
                //值需要-
                drawableAlpha = Math.max(curAlpha - alphaStep, nextAlpha)
            } else {
                //当到达目标时
                if (alphaIndex == alphaList.size - 1) {
                    //到达后一个透明度
                    if (loopAlpha) {
                        alphaIndex = 0
                    } else {
                        onLoopAlphaEnd?.invoke()
                    }
                } else {
                    alphaIndex++
                }
            }
        }
    }

    /*是否有下一个透明度的值需要变化*/
    private fun haveNext(): Boolean {
        return alphaIndex >= 0 && alphaIndex < alphaList.size - 1
    }

    private fun nextAlpha(): Int {
        if (haveNext()) {
            alphaIndex++
            return alphaList[alphaIndex]
        }
        //alphaIndex = 0
        return drawableAlpha
    }

    var onLoopAlphaEnd: (() -> Unit)? = null
}