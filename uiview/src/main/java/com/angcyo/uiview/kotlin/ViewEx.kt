package com.angcyo.uiview.kotlin


import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.TextView
import com.angcyo.github.utilcode.utils.SingleTextWatcher
import com.angcyo.library.utils.Anim
import com.angcyo.uiview.recycler.RRecyclerView
import com.angcyo.uiview.rsen.RGestureDetector
import com.angcyo.uiview.utils.ScreenUtil
import com.angcyo.uiview.utils.ScreenUtil.density
import com.angcyo.uiview.view.RClickListener
import com.angcyo.uiview.view.UIIViewImpl
import com.wangjie.shadowviewhelper.ShadowProperty
import com.wangjie.shadowviewhelper.ShadowViewDrawable
import java.util.*

/**
 * Kotlin View的扩展
 * Created by angcyo on 2017-06-03.
 */

@Suppress("UNCHECKED_CAST")
public fun <V : View> View.v(id: Int): V? {
    val view: View? = findViewById(id)
    return view as V?
}

public fun View.getDrawable(resId: Int): Drawable? {
    if (resId == -1) {
        return null
    }
    return ContextCompat.getDrawable(context, resId)
}

public val View.random: Random by lazy {
    Random(System.nanoTime())
}

public val UIIViewImpl.random: Random by lazy {
    Random(System.nanoTime())
}

public val View.scaledDensity: Float
    get() = resources.displayMetrics.scaledDensity

public val View.density: Float
    get() = resources.displayMetrics.density

public val View.viewDrawWith: Int
    get() = measuredWidth - paddingLeft - paddingRight

public val View.viewDrawHeight: Int
    get() = measuredHeight - paddingTop - paddingBottom

public val View.debugPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 1 * density
    }
}

public val View.viewSize: Int
    get() = viewDrawWith.maxValue(viewDrawHeight)

private val View.tempRect: Rect by lazy {
    Rect()
}

public fun View.getGlobalVisibleRect(): Rect {
    //top 永远都不会少于0  bottom 永远都不会大于屏幕高度, 可见的rect就是, 不可见的会被剃掉
    getGlobalVisibleRect(tempRect)
    return tempRect
}

/**返回居中绘制文本的y坐标*/
public fun View.getDrawCenterTextCy(paint: Paint): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    return paddingTop + rawHeight / 2 + (paint.descent() - paint.ascent()) / 2 - paint.descent()
}

public fun View.getDrawCenterTextCx(paint: Paint, text: String): Float {
    val rawWidth = measuredWidth - paddingLeft - paddingRight
    return paddingLeft + rawWidth / 2 - paint.measureText(text) / 2
}

public fun View.centerX(): Int {
    return (this.x + this.measuredWidth / 2).toInt()
}

public fun View.centerY(): Int {
    return (this.y + this.measuredHeight / 2).toInt()
}

public fun View.getDrawCenterCy(): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    return (paddingTop + rawHeight / 2).toFloat()
}

public fun View.getDrawCenterCx(): Float {
    val rawWidth = measuredWidth - paddingLeft - paddingRight
    return (paddingLeft + rawWidth / 2).toFloat()
}

/**最小圆的半径*/
public fun View.getDrawCenterR(): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    val rawWidth = measuredWidth - paddingLeft - paddingRight
    return (Math.min(rawWidth, rawHeight) / 2).toFloat()
}

public fun TextView.getDrawCenterTextCy(): Float {
    val rawHeight = measuredHeight - paddingTop - paddingBottom
    return paddingTop + rawHeight / 2 + (paint.descent() - paint.ascent()) / 2 - paint.descent()
}

/**文本的高度*/
public fun <T> T.textHeight(paint: Paint): Float = paint.descent() - paint.ascent()

public fun TextView.textHeight(): Float = paint.descent() - paint.ascent()

/**文本宽度*/
public fun View.textWidth(paint: Paint?, text: String?): Float = paint?.measureText(text ?: "")
        ?: 0F

public fun TextView.textWidth(text: String?): Float = paint.measureText(text ?: "")

public fun View.getColor(id: Int): Int = ContextCompat.getColor(context, id)

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

/**计算宽高比例*/
public fun View.calcWidthHeightRatio(widthHeightRatio: String?): IntArray? {
    if (!TextUtils.isEmpty(widthHeightRatio)) {
        /*固定比例如(1.5w 表示 高度是宽度的1.5倍, 0.5h 表示宽度是高度的0.5倍)*/
        if (widthHeightRatio!!.contains("wh", true)) {
            //特殊情况, size取宽高中最大的值
            val size = measuredWidth.minValue(measuredHeight)
            return intArrayOf(size, size)
        } else if (widthHeightRatio.contains("w", true)) {
            val ratio = widthHeightRatio.replace("w", "", true).toFloatOrNull()
            ratio?.let {
                return intArrayOf(measuredWidth, (it * measuredWidth).toInt())
            }
        } else if (widthHeightRatio.contains("h", true)) {
            val ratio = widthHeightRatio.replace("h", "", true).toFloatOrNull()
            ratio?.let {
                return intArrayOf((it * measuredHeight).toInt(), measuredHeight)
            }
        }
    }
    return null
}

/**用屏幕宽高, 计算View的宽高*/
public fun View.calcLayoutWidthHeight(rLayoutWidth: String?, rLayoutHeight: String?, rLayoutWidthExclude: Int = 0, rLayoutHeightExclude: Int = 0): IntArray {
    val size = intArrayOf(-1, -1)
    if (TextUtils.isEmpty(rLayoutWidth) && TextUtils.isEmpty(rLayoutHeight)) {
        return size
    }
    if (!TextUtils.isEmpty(rLayoutWidth)) {
        if (rLayoutWidth!!.contains("sw", true)) {
            val ratio = rLayoutWidth.replace("sw", "", true).toFloatOrNull()
            ratio?.let {
                size[0] = (ratio * (ScreenUtil.screenWidth - rLayoutWidthExclude)).toInt()
            }
        }
    }
    if (!TextUtils.isEmpty(rLayoutHeight)) {
        if (rLayoutHeight!!.contains("sh", true)) {
            val ratio = rLayoutHeight.replace("sh", "", true).toFloatOrNull()
            ratio?.let {
                size[1] = (ratio * (ScreenUtil.screenHeight - rLayoutHeightExclude)).toInt()
            }
        }
    }
    return size
}

/**手势是否结束*/
public fun View.isTouchFinish(event: MotionEvent) = event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL

public fun View.clickIt(listener: View.OnClickListener) {
    if (listener is RClickListener) {
        setOnClickListener(listener)
    } else {
        setOnClickListener(object : RClickListener() {
            override fun onRClick(view: View?) {
                listener.onClick(view)
            }
        })
    }
}

public fun View.clickIt(onClick: (View) -> Unit) {
    setOnClickListener(object : RClickListener() {
        override fun onRClick(view: View?) {
            onClick.invoke(this@clickIt)
        }
    })
}

/**焦点变化改变监听*/
public fun EditText.onFocusChange(listener: (Boolean) -> Unit) {
    this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus -> listener.invoke(hasFocus) }
    listener.invoke(this.isFocused)
}

/**空文本变化监听*/
public fun EditText.onEmptyText(listener: (Boolean) -> Unit) {
    this.addTextChangedListener(object : SingleTextWatcher() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)
            listener.invoke(TextUtils.isEmpty(s))
        }
    })
    listener.invoke(TextUtils.isEmpty(this.text))
}

public fun EditText.onTextChanage(listener: (String) -> Unit) {
    this.addTextChangedListener(object : SingleTextWatcher() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)
            listener.invoke(s?.toString() ?: "")
        }
    })
}

public fun RRecyclerView.onSizeChanged(listener: (w: Int, h: Int, oldw: Int, oldh: Int) -> Unit) {
    this.setOnSizeChangedListener { w, h, oldw, oldh ->
        listener.invoke(w, h, oldw, oldh)
    }
}

/**
 * 错误提示
 */
public fun View.error() {
    Anim.band(this)
}

public fun View.visible() {
    visibility = View.VISIBLE
}

public fun View.gone() {
    visibility = View.GONE
}

public fun View.invisible() {
    visibility = View.INVISIBLE
}

public fun TextView.isEmpty(): Boolean {
    return TextUtils.isEmpty(string())
}

public fun TextView.string(trim: Boolean = true): String {
    var rawText = if (TextUtils.isEmpty(text)) {
        ""
    } else {
        text.toString()
    }
    if (trim) {
        rawText = rawText.trim({ it <= ' ' })
    }
    return rawText
}

/**
 * 返回结果表示是否为空
 */
public fun EditText.checkEmpty(checkPhone: Boolean = false): Boolean {
    if (isEmpty()) {
        error()
        requestFocus()
        return true
    }
    if (checkPhone) {
//        if (isPhone()) {
//
//        } else {
//            error()
//            requestFocus()
//            return true
//        }
    }
    return false
}

public fun EditText.setInputText(text: String?) {
    this.setText(text)
    setSelection(text?.length ?: 0)
}


/**取消增益滑动效果*/
public fun View.setNoOverScroll() {
    overScrollMode = View.OVER_SCROLL_NEVER
}

/**设置阴影背景*/
public fun View.showShadowViewDrawable(shadowRadius: Int = 6) {
    val sp = ShadowProperty()
            .setShadowColor(0x77000000)
            .setShadowDy((1f * density()).toInt())//y轴偏移
            .setShadowRadius((shadowRadius * density()).toInt())//阴影半径
            .setShadowSide(ShadowProperty.ALL)
    val sd = ShadowViewDrawable(sp, Color.RED, 0f, 0f)
    ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null)
    ViewCompat.setBackground(this, sd)
}

/**双击控件回调*/
public fun View.onDoubleTap(listener: () -> Unit) {
    RGestureDetector.onDoubleTap(this) {
        listener.invoke()
    }
}

/**无限循环, 每秒60帧的速度*/
public val View.valueAnimator: ValueAnimator by lazy {
    ValueAnimator.ofInt(0, 100).apply {
        interpolator = LinearInterpolator()
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        duration = 1000
    }
}