package com.angcyo.uiview.kotlin

import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
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

public fun Int.remove(value: Int): Int = this and value.inv()
public fun Int.add(value: Int): Int = this or value

/**文本的高度*/
public fun Paint.textHeight(): Float = descent() - ascent()

/**文本的宽度*/
public fun Paint.textWidth(text: String): Float = this.measureText(text)

public inline fun <T : View> UIIViewImpl.vh(id: Int): Lazy<T> {
    return lazy {
        v<T>(id)
    }
}

public fun Float.abs() = Math.abs(this)
public fun Int.abs() = Math.abs(this)

public fun Float.max0() = Math.max(0f, this)
public fun Int.max0() = Math.max(0, this)

/**这个值允许的最小值*/
public fun Float.minValue(value: Float) = Math.max(value, this)

public fun Float.minValue(value: Int) = Math.max(value.toFloat(), this)

public fun Int.minValue(value: Int) = Math.max(value, this)

/**这个值允许的最大值*/
public fun Float.maxValue(value: Float) = Math.min(value, this)

public fun Int.maxValue(value: Int) = Math.min(value, this)

public fun String.int() = if ((TextUtils.isEmpty(this) || "null".equals(this, true))) 0 else this.toInt()

/**矩形缩放*/
public fun Rect.scale(scaleX: Float, scaleY: Float) {
    var dw = 0
    var dh = 0
    if (scaleX != 1.0f) {
        /*宽度变化量*/
        val offsetW = (width() * scaleX + 0.5f).toInt() - width()
        dw = offsetW / 2
    }
    if (scaleY != 1.0f) {
        /*高度变化量*/
        val offsetH = (height() * scaleY + 0.5f).toInt() - height()
        dh = offsetH / 2
    }
    inset(-dw, -dh)
}

/**矩形旋转*/
public fun Rect.rotateTo(inRect: Rect, degrees: Float) {
    var dw = 0
    var dh = 0

    /*斜边长度*/
    val c = c()

    /*斜边与邻边的幅度*/
    val aR = Math.asin(this.height().toDouble() / c /*弧度*/)

    /*角度转弧度*/
    val d = Math.toRadians(degrees.toDouble()) /*弧度*/

    val nW1 = Math.abs(c / 2 * Math.cos(aR - d))
    val nW2 = Math.abs(c / 2 * Math.cos(Math.PI - aR - d))

    val nH1 = Math.abs(c / 2 * Math.sin(aR - d))
    val nH2 = Math.abs(c / 2 * Math.sin(Math.PI - aR - d))

    /*新的宽度*/
    val nW = 2 * Math.max(nW1, nW2)
    /*新的宽度*/
    val nH = 2 * Math.max(nH1, nH2)

    dw = nW.toInt() - this.width()
    dh = nH.toInt() - this.height()

    inRect.set(this)
    inRect.inset(-dw / 2, -dh / 2)
}

/**矩形斜边长度*/
public fun Rect.c(): Double {
    /*斜边长度*/
    val c = Math.sqrt(Math.pow(this.width().toDouble(), 2.toDouble()) + Math.pow(this.height().toDouble(), 2.toDouble()))
    return c
}

public fun Rect.scaleTo(inRect: Rect /*用来接收最后结果的矩形*/, scaleX: Float, scaleY: Float) {
    var dw = 0
    var dh = 0
    if (scaleX != 1.0f) {
        /*宽度变化量*/
        val offsetW = (width() * scaleX + 0.5f).toInt() - width()
        dw = offsetW / 2
    }
    if (scaleY != 1.0f) {
        /*高度变化量*/
        val offsetH = (height() * scaleY + 0.5f).toInt() - height()
        dh = offsetH / 2
    }
    inRect.set(left, top, right, bottom)
    inRect.inset(-dw, -dh)
}

public inline fun <T> T.nowTime() = System.currentTimeMillis()
