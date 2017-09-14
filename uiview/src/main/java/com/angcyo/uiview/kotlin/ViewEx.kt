package com.angcyo.uiview.kotlin

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.angcyo.uiview.view.RClickListener

/**
 * Kotlin View的扩展
 * Created by angcyo on 2017-06-03.
 */

@Suppress("UNCHECKED_CAST")
public fun <V : View> View.v(id: Int): V? {
    val view: View? = findViewById(id)
    return view as V?
}

public val View.scaledDensity: Float
    get() = resources.displayMetrics.scaledDensity
public val View.density: Float
    get() = resources.displayMetrics.density

/**返回居中绘制文本的y坐标*/
public fun View.getDrawCenterTextCy(paint: Paint): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    return paddingTop + rawHeight / 2 + (paint.descent() - paint.ascent()) / 2 - paint.descent()
}

public fun View.getDrawCenterTextCx(paint: Paint, text: String): Float {
    val rawWidth = measuredWidth - paddingLeft - paddingRight
    return paddingLeft + rawWidth / 2 - paint.measureText(text) / 2
}

public fun View.getDrawCenterCy(): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    return (paddingTop + rawHeight / 2).toFloat()
}

public fun TextView.getDrawCenterTextCy(): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    return paddingTop + rawHeight / 2 + (paint.descent() - paint.ascent()) / 2 - paint.descent()
}

/**文本的高度*/
public fun <T> T.textHeight(paint: Paint): Float = paint.descent() - paint.ascent()

public fun TextView.textHeight(): Float = paint.descent() - paint.ascent()

/**文本宽度*/
public fun View.textWidth(paint: Paint, text: String): Float = paint.measureText(text)

public fun TextView.textWidth(text: String): Float = paint.measureText(text)

public fun View.getColor(id: Int): Int = ContextCompat.getColor(context, id)

public fun View.getDrawable(id: Int): Drawable = ContextCompat.getDrawable(context, id)

public fun View.getDimensionPixelOffset(id: Int): Int = resources.getDimensionPixelOffset(id)

/**Match_Parent*/
public fun View.exactlyMeasure(size: Int): Int = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)

/**Wrap_Content*/
public fun View.atmostMeasure(size: Int): Int = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST)

public fun View.setOnRClickListener(listener: View.OnClickListener?) {
    if (listener == null) {
        this.isClickable = false
        this.setOnClickListener(null)
    } else {
        if (listener is RClickListener) {
            this.setOnClickListener(listener)
        } else {
            this.setOnClickListener(object : RClickListener(300) {
                override fun onRClick(view: View?) {
                    listener.onClick(view)
                }
            })
        }
    }

}