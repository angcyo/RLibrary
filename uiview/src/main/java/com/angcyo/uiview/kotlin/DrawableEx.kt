package com.angcyo.uiview.kotlin

import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/12/15 15:01
 * 修改人员：Robi
 * 修改时间：2017/12/15 15:01
 * 修改备注：
 * Version: 1.0.0
 */

/**将Drawable放在inRect的指定点centerPoint的位置*/
public fun Drawable.getBoundsWith(centerPoint: Point, inRect: Rect) = Rect().apply {
    left = centerPoint.x - intrinsicWidth / 2
    top = centerPoint.y - intrinsicHeight / 2
    right = centerPoint.x + intrinsicWidth / 2
    bottom = centerPoint.y + intrinsicHeight / 2
}
