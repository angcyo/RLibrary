package com.angcyo.uiview.kotlin

import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView

/**
 * Kotlin View的扩展
 * Created by angcyo on 2017-06-03.
 */

@Suppress("UNCHECKED_CAST")
public fun <V : View> View.v(id: Int): V {
    val view: View = findViewById(id)
    return view as V
}

public val View.scaledDensity: Float get() {
    return resources.displayMetrics.scaledDensity
}
public val View.density: Float get() {
    return resources.displayMetrics.density
}

/**返回居中绘制文本的y坐标*/
public fun View.getDrawCenterTextCy(paint: Paint): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    return paddingTop + rawHeight / 2 - paint.descent() / 2 - paint.ascent() / 2
}

public fun View.getDrawCenterCy(): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    return (paddingTop + rawHeight / 2).toFloat()
}

public fun TextView.getDrawCenterTextCy(): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    return paddingTop + rawHeight / 2 - paint.descent() / 2 - paint.ascent() / 2
}

/**文本的高度*/
public fun View.textHeight(paint: Paint): Float {
    return paint.descent() - paint.ascent()
}

public fun TextView.textHeight(): Float {
    return paint.descent() - paint.ascent()
}

public fun View.getColor(id: Int): Int {
    return ContextCompat.getColor(context, id)
}

/**Match_Parent*/
public fun View.exactlyMeasure(size: Int): Int {
    return View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
}