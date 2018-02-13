package com.angcyo.uiview.game.spirit

import android.graphics.Point
import android.graphics.drawable.Drawable
import com.angcyo.uiview.utils.ScreenUtil

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：随机位置的精灵
 * 创建人员：Robi
 * 创建时间：2017/12/16 12:28
 * 修改人员：Robi
 * 修改时间：2017/12/16 12:28
 * 修改备注：
 * Version: 1.0.0
 */
open class RandomPointBean(drawableArray: Array<Drawable>, val startPoint: Point? = null) : FrameBean(drawableArray, Point(0, 0)) {


    init {
        loopDrawFrame = true
        if (startPoint != null) {
            drawPoint = startPoint
        } else {
            drawPoint = Point(random.nextInt(ScreenUtil.screenWidth), random.nextInt(ScreenUtil.screenHeight))
        }
    }

//    override var drawPoint: Point = Point()
//        get() = if (startPoint == null) {
//            Point(random.nextInt(parentRect.width()), random.nextInt(parentRect.height()))
//        } else {
//            field
//        }
//        set(value) {
//            field = value
//        }

    override fun onLoopFrame() {
        super.onLoopFrame()
        drawPoint = getRandomPoint()
    }

    private fun getRandomPoint() = Point(random.nextInt(parentRect.width()), random.nextInt(parentRect.height()))
}