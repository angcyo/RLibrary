package com.angcyo.uiview.helper

import android.graphics.PointF

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：三阶贝塞尔曲线
 * 创建人员：Robi
 * 创建时间：2017/12/14 08:51
 * 修改人员：Robi
 * 修改时间：2017/12/14 08:51
 * 修改备注：
 * Version: 1.0.0
 */
class BezierPointHelper(private val startPoint: PointF /*曲线起点*/, private val endPoint: PointF /*曲线终点*/,
                        private val controlPoint1: PointF, private val controlPoint2: PointF /*曲线控制点*/) {

    fun evaluate(time: Float /*取值范围 [0, 1]*/): PointF {

        val t = Math.max(Math.min(time, 1f), 0f)

        val timeLeft = 1.0f - t
        val point = PointF()//结果

        /*当计算x时, 与y的值没有关系. 所以x 和 y, 都是贝塞尔曲线上的点*/
        point.x = (timeLeft * timeLeft * timeLeft * startPoint.x
                + 3f * timeLeft * timeLeft * t * controlPoint1.x
                + 3f * timeLeft * t * t * controlPoint2.x
                + t * t * t * endPoint.x)

        point.y = (timeLeft * timeLeft * timeLeft * startPoint.y
                + 3f * timeLeft * timeLeft * t * controlPoint1.y
                + 3f * timeLeft * t * t * controlPoint2.y
                + t * t * t * endPoint.y)

        return point
    }
}

class BezierHelper(private val startPoint: Float /*曲线起点的坐标, 决定了曲线的起点坐标*/,
                   private val endPoint: Float /*曲线终点的坐标,  决定了曲线的终点坐标*/,
                   private val controlPoint1: Float /*控制曲线的幅度, 决定曲线开始偏移的方向和幅度 (幅度的大小小于 控制点-起点的差值)*/,
                   private val controlPoint2: Float /*曲线控制点, 决定曲线接下来的偏移方向和幅度*/) {

    fun evaluate(time: Float /*取值范围 [0, 1]*/): Float {

        val t = Math.max(Math.min(time, 1f), 0f)

        val timeLeft = 1.0f - t
        val point: Float//结果

        /*当计算x时, 与y的值没有关系. 所以x 和 y, 都是贝塞尔曲线上的点*/
        point = (timeLeft * timeLeft * timeLeft * startPoint
                + 3f * timeLeft * timeLeft * t * controlPoint1
                + 3f * timeLeft * t * t * controlPoint2
                + t * t * t * endPoint)

        return point
    }
}