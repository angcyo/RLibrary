package com.angcyo.uiview.kotlin

import android.graphics.Paint
import android.view.View
import com.angcyo.uiview.view.UIIViewImpl

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/07 16:41
 * 修改人员：Robi
 * 修改时间：2017/07/07 16:41
 * 修改备注：
 * Version: 1.0.0
 */

/**整型数中, 是否包含另一个整数*/
public fun Int.have(value: Int): Boolean = value != 0 && this and value == value

/**文本的高度*/
public fun Paint.textHeight(): Float = descent() - ascent()

/**文本的宽度*/
public fun Paint.textWidth(text: String): Float = this.measureText(text)

public inline fun <T : View> UIIViewImpl.vh(id: Int): Lazy<T> {
    return lazy {
        v<T>(id)
    }
}

public fun Float.max0() = Math.max(0f, this)
public fun Int.max0() = Math.max(0, this)

/**这个值允许的最小值*/
public fun Float.minValue(value: Float) = Math.max(value, this)

public fun Float.minValue(value: Int) = Math.max(value.toFloat(), this)

public fun Int.minValue(value: Int) = Math.max(value, this)

/**这个值允许的最大值*/
public fun Float.maxValue(value: Float) = Math.min(value, this)

public fun Int.maxValue(value: Int) = Math.min(value, this)