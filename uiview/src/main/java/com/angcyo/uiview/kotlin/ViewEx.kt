package com.angcyo.uiview.kotlin


import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewCompat
import android.text.TextUtils
import android.util.TypedValue
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import com.angcyo.github.utilcode.utils.SingleTextWatcher
import com.angcyo.library.utils.Anim
import com.angcyo.uiview.RApplication
import com.angcyo.uiview.draw.RDrawNoRead
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.RRecyclerView
import com.angcyo.uiview.rsen.RGestureDetector
import com.angcyo.uiview.utils.ScreenUtil
import com.angcyo.uiview.utils.ScreenUtil.density
import com.angcyo.uiview.view.RClickListener
import com.angcyo.uiview.view.UIIViewImpl
import com.angcyo.uiview.widget.RExTextView
import com.angcyo.uiview.widget.RImageView
import com.angcyo.uiview.widget.RTextView
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

public fun <T> T.getDrawable(resId: Int): Drawable? {
    if (resId == -1) {
        return null
    }
    return ContextCompat.getDrawable(RApplication.getApp(), resId)
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
    return paddingTop + rawHeight / 2 + paint.textDrawCy()
}

public fun View.getDrawCenterTextCx(paint: Paint, text: String): Float {
    val rawWidth = measuredWidth - paddingLeft - paddingRight
    return paddingLeft + rawWidth / 2 - paint.textDrawCx(text)
}

public fun Paint.textDrawCx(text: String): Float {
    return measureText(text) / 2
}

/**文本绘制时候 的中点y坐标*/
public fun Paint.textDrawCy(): Float {
    return (descent() - ascent()) / 2 - descent()
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
public fun TextView.drawPadding(padding: Int) {
    compoundDrawablePadding = padding
}

public fun TextView.drawPadding(padding: Float) {
    drawPadding(padding.toInt())
}

/**设置文本大小 dp单位*/
public fun TextView.setTextSizeDp(sizeDp: Float) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeDp * ScreenUtil.density())
}

public fun TextView.setRightIco(id: Int) {
    RExTextView.setRightIco(this, id)
}

public fun TextView.setLeftIco(id: Int) {
    RExTextView.setLeftIco(this, id)
}

public fun View.getColor(id: Int): Int = ContextCompat.getColor(context, id)

public fun View.getDimensionPixelOffset(id: Int): Int = resources.getDimensionPixelOffset(id)

/**Match_Parent*/
public fun View.exactlyMeasure(size: Int): Int = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)

public fun View.exactlyMeasure(size: Float): Int = this.exactlyMeasure(size.toInt())

/**Wrap_Content*/
public fun View.atmostMeasure(size: Int): Int = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST)

public fun View.atmostMeasure(size: Float): Int = this.atmostMeasure(size.toInt())

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
    return calcLayoutWidthHeight(rLayoutWidth, rLayoutHeight, measuredWidth, measuredHeight, rLayoutWidthExclude, rLayoutHeightExclude)
}

public fun View.calcLayoutWidthHeight(rLayoutWidth: String?, rLayoutHeight: String?,
                                      parentWidth: Int, parentHeight: Int,
                                      rLayoutWidthExclude: Int = 0, rLayoutHeightExclude: Int = 0): IntArray {
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
        } else if (rLayoutWidth!!.contains("pw", true)) {
            val ratio = rLayoutWidth.replace("pw", "", true).toFloatOrNull()
            ratio?.let {
                size[0] = (ratio * (parentWidth - rLayoutWidthExclude)).toInt()
            }
        }
    }
    if (!TextUtils.isEmpty(rLayoutHeight)) {
        if (rLayoutHeight!!.contains("sh", true)) {
            val ratio = rLayoutHeight.replace("sh", "", true).toFloatOrNull()
            ratio?.let {
                size[1] = (ratio * (ScreenUtil.screenHeight - rLayoutHeightExclude)).toInt()
            }
        } else if (rLayoutHeight!!.contains("ph", true)) {
            val ratio = rLayoutHeight.replace("ph", "", true).toFloatOrNull()
            ratio?.let {
                size[1] = (ratio * (parentHeight - rLayoutHeightExclude)).toInt()
            }
        }
    }
    return size
}

/**手势是否结束*/
public fun View.isTouchFinish(event: MotionEvent) = event.isFinish()

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

public fun View.longClick(listener: (View) -> Unit) {
    setOnLongClickListener {
        listener.invoke(it)
        true
    }
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

/**只要文本改变就通知*/
public fun EditText.onTextChange(listener: (String) -> Unit) {
    this.addTextChangedListener(object : SingleTextWatcher() {
        var lastText: String? = null

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)
            val text = s?.toString() ?: ""
            if (TextUtils.equals(lastText, text)) {
            } else {
                listener.invoke(text)
                lastText = text
            }
        }
    })
}

/**相同文本不重复通知*/
public fun EditText.onTextChangeFilter(listener: (String) -> Unit) {
    this.addTextChangedListener(object : SingleTextWatcher() {
        var lastText: String? = null
        var text: String? = null
        var lastTime = -1L

        val notify = Runnable {
            val text = this.text ?: ""
            listener.invoke(text)
            lastText = text
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)
            text = s?.toString() ?: ""
            if (!lastText.isNullOrEmpty() && TextUtils.equals(lastText, text)) {
            } else {
                //val time = nowTime()
                removeCallbacks(notify)
//                if (lastTime == -1L || (time - lastTime) < 300L) {
//                    //延迟300毫秒通知一次
//                }
                //必定300毫秒通知一次
                postDelayed(notify, 300L)
            }
        }
    })
}

/**输入框, 按下删除键*/
public fun EditText.onBackPress(listener: (EditText) -> Unit) {
    setOnKeyListener { v, keyCode, keyEvent ->
        return@setOnKeyListener if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.action == KeyEvent.ACTION_UP) {
            listener.invoke(v as EditText)
            true
        } else {
            false
        }
    }
}

/**发送删除键*/
public fun EditText.sendDelKey() {
    this.del()
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

/**触发删除或回退键*/
public fun EditText.del() {
    this.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
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

/**自己监听控件的单击事件, 防止系统的不回调*/
public fun View.onSingleTapConfirmed(listener: () -> Boolean) {
    val gestureDetectorCompat = GestureDetectorCompat(context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    return listener.invoke()
                }
            })
    setOnTouchListener { _, event ->
        gestureDetectorCompat.onTouchEvent(event)
        false
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

/**显示未读小红点*/
public fun View.showNoRead(show: Boolean = true,
                           radius: Float = 3 * density(),
                           paddTop: Float = 2 * density(),
                           paddRight: Float = 2 * density()) {
    var drawNoRead: RDrawNoRead? = null
    if (this is RImageView) {
        drawNoRead = this.drawNoRead
    } else if (this is RTextView) {
        drawNoRead = this.drawNoRead
    }

    if (drawNoRead != null) {
        drawNoRead.setShowNoRead(show)
        drawNoRead.setNoReadRadius(radius)
        drawNoRead.setNoReadPaddingTop(paddTop)
        drawNoRead.setNoReadPaddingRight(paddRight)
    }
}

public fun CompoundButton.onChecked(listener: (Boolean) -> Unit) {
    this.setOnCheckedChangeListener { _, isChecked ->
        listener.invoke(isChecked)
    }
    listener.invoke(isChecked)
}

public fun TextView.addPaintFlags(flag: Int, add: Boolean = true, invalidate: Boolean = true) {
    if (add) {
        paint.flags = paint.flags or flag
    } else {
        paint.flags = paint.flags and flag.inv()
    }
    if (invalidate) {
        postInvalidate()
    }
}

public fun View.hideFromBottom(anim: Boolean = true) {
    if (this.translationY == 0f) {
        //是显示状态
        if (anim) {
            this.animate().setDuration(300)
                    .translationY((this.measuredHeight).toFloat())
                    .start()
        } else {
            ViewCompat.setTranslationY(this, (this.measuredHeight).toFloat())
        }
    }
}

public fun View.showFromBottom(anim: Boolean = true) {
    if (this.translationY == (this.measuredHeight).toFloat()) {
        //是隐藏状态
        if (anim) {
            this.animate().setDuration(300)
                    .translationY(0f)
                    .start()
        } else {
            ViewCompat.setTranslationY(this, 0f)
        }
    }
}

public fun View.hideFromTop(anim: Boolean = true) {
    if (this.translationY == 0f) {
        //是显示状态
        if (anim) {
            this.animate().setDuration(300)
                    .translationY((-this.measuredHeight).toFloat())
                    .start()
        } else {
            ViewCompat.setTranslationY(this, (-this.measuredHeight).toFloat())
        }
    }
}

public fun View.showFromTop(anim: Boolean = true) {
    if (this.translationY == (-this.measuredHeight).toFloat()) {
        //是隐藏状态
        if (anim) {
            this.animate().setDuration(300)
                    .translationY(0f)
                    .start()
        } else {
            ViewCompat.setTranslationY(this, 0f)
        }
    }
}

/**布局中心的坐标*/
public fun View.layoutCenterX(): Int {
    return left + measuredWidth / 2
}

public fun View.layoutCenterY(): Int {
    return top + measuredHeight / 2
}

public fun View.onInitView(init: (RBaseViewHolder) -> Unit) {
    init.invoke(RBaseViewHolder(this))
}