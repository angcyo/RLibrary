package com.angcyo.uiview.viewgroup

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.*
import android.view.MotionEvent.*
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.OverScroller
import com.angcyo.library.utils.L
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.scaledDensity
import com.angcyo.uiview.skin.SkinHelper

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：实际上是一个用来录制视频的View, 但是提供了 展开显示滤镜, 收缩隐藏滤镜布局的动画
 * 创建人员：Robi
 * 创建时间：2017/06/09 11:06
 * 修改人员：Robi
 * 修改时间：2017/06/09 11:06
 * 修改备注：
 * Version: 1.0.0
 */
class ExpandRecordLayout(context: Context, attributeSet: AttributeSet? = null) :
        LinearLayout(context, attributeSet) {

    /**当前状态*/
    var state = STATE_CLOSE

    /**控制滚动展开,隐藏*/
    val scroller: OverScroller by lazy { OverScroller(context) }

    init {
        orientation = VERTICAL
        gravity = Gravity.BOTTOM
        setWillNotDraw(false)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //expandLayout(false)
        /**滚动至默认状态*/
        post {
            if (state == STATE_EXPAND) {
                scrollTo(0, 0)
            } else if (state == STATE_CLOSE) {
                scrollTo(0, -childHeight)
            }
        }
    }

    /**所有child View的高度*/
    var childHeight: Int = 0

    val paint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.strokeCap = Paint.Cap.BUTT
        p.strokeJoin = Paint.Join.ROUND
        p.textSize = 12 * scaledDensity
        p
    }

    val circleMaxOffset = 30 * density
    val circleMinOffset: Float
        get() {
            return circleMaxOffset * 0.6f
        }
    val circleOffset: Float
        get() {
            return circleMinOffset + (circleMaxOffset - circleMinOffset) * (Math.abs(scrollY.toFloat()) / childHeight)
        }


    val circleMaxRadius = 30 * density
    val circleMinRadius: Float
        get() {
            return circleMaxRadius * 0.5f
        }
    val circleDrawRadius: Float
        get() {
            return circleMinRadius + (circleMaxRadius - circleMinRadius) * (Math.abs(scrollY.toFloat()) / childHeight)
        }

    val outCircleMaxRadius = circleMaxRadius + 10 * density
    val outCircleMinRadius: Float
        get() {
            return outCircleMaxRadius * 0.5f
        }
    val outCircleDrawRadius: Float
        get() {
            return outCircleMinRadius + (outCircleMaxRadius - outCircleMinRadius) * (Math.abs(scrollY.toFloat()) / childHeight)
        }

    /**控制缩放动画的变量*/
    var circleScale: Float = 1f
    var outCircleScale: Float = 1f

    /**当前圆的位置坐标*/
    val circleRect: Rect by lazy {
        Rect()
    }
    val outCircleRect: RectF by lazy {
        RectF()
    }

    var outCircleColor: Int = Color.parseColor("#F0F0F0")
    var circleColor: Int = if (isInEditMode) Color.RED else SkinHelper.getSkin().themeSubColor
    var progressColor: Int = if (isInEditMode) Color.RED else SkinHelper.getSkin().themeSubColor

    /**长按检查*/
    val longPressRunnable = Runnable {
        if (state == STATE_CLOSE) {
            L.e("call: longPressRunnable 长按... -> ")
            isLongPress = true
            startProgress()
        }
    }

    /**控制手势事件*/
    val gestureCompat: GestureDetectorCompat by lazy {
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(event: MotionEvent) {
                L.e("call: onLongPress -> ")
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                if (isLongPress) {
                    return false
                }
                L.e("call: onSingleTapUp -> ")
                //expandLayout(state == STATE_CLOSE)

                if (state == STATE_EXPAND) {
                    expandLayout(false)
                } else {
                    //在未展开的状态下, 点击可以实现拍照(微信就是这样的), 长按就是录制
                }
                return super.onSingleTapUp(e)
            }

            override fun onDown(event: MotionEvent): Boolean {
                val eventX = event.x
                val eventY = event.y + scrollY

                var intercept = false
                if (outCircleRect.contains(eventX, eventY)) {
                    intercept = true
                    if (state == STATE_CLOSE) {
                        postDelayed(longPressRunnable, 200)
                    }
                } else if (state == STATE_EXPAND) {
                    intercept = true
                }
                L.e("call: onDown -> $intercept")
                return intercept
            }
        })
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            val currY = scroller.currY
            scrollTo(0, currY)
            postInvalidate()
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        //if (childCount > 2) throw IllegalArgumentException("child max have 2.")
    }

    /**推荐使用Match_Parent*/
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var height: Int = 0
        (0..childCount - 1).map {
            height += getChildAt(it).measuredHeight
        }
        childHeight = height
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (MotionEventCompat.getActionMasked(event)) {
            ACTION_CANCEL, ACTION_UP -> {
                removeCallbacks(longPressRunnable)
                if (isLongPress) {
                    isLongPress = false
                    stopProgress()
                }
            }
            ACTION_MOVE -> {
                val eventX = event.x
                val eventY = event.y + scrollY

                if (!outCircleRect.contains(eventX, eventY)) {
                    removeCallbacks(longPressRunnable)
                }
            }
        }
        return gestureCompat.onTouchEvent(event)
    }

    /**最大允许录制30秒*/
    var maxTime: Int = 30

    val progressAnimator: ValueAnimator by lazy {
        val anim = ObjectAnimator.ofFloat(0f, 360f)
        anim.duration = (maxTime * 1000).toLong()
        anim.interpolator = LinearInterpolator()
        anim
    }

    /**变小动画*/
    val lessenAnimator: ValueAnimator by lazy {
        val anim = ObjectAnimator.ofFloat(1f, MIN_SCALE)
        anim.duration = ANIM_TIME
        anim.interpolator = LinearInterpolator()
        anim
    }

    val lessenReAnimator: ValueAnimator by lazy {
        val anim = ObjectAnimator.ofFloat(MIN_SCALE, 1f)
        anim.duration = ANIM_TIME
        anim.interpolator = LinearInterpolator()
        anim
    }

    /**变大动画*/
    val largenAnimator: ValueAnimator by lazy {
        val anim = ObjectAnimator.ofFloat(1f, MAX_SCALE)
        anim.duration = ANIM_TIME
        anim.interpolator = LinearInterpolator()
        anim
    }

    val largenReAnimator: ValueAnimator by lazy {
        val anim = ObjectAnimator.ofFloat(MAX_SCALE, 1f)
        anim.duration = ANIM_TIME
        anim.interpolator = LinearInterpolator()
        anim
    }

    /**当前录制的进度*/
    var progress: Float = 0f
        set(value) {
            field = value
            if (isLongPress) {
                postInvalidate()
            }
        }

    /**进度条的宽度*/
    var progressWidth = 4 * density

    private var isLongPress = false

    /**开始绘制进度*/
    private fun startProgress() {
        if (!progressAnimator.isStarted) {
            progress = 0f
            progressAnimator.addUpdateListener {
                progress = it.animatedValue as Float
                //L.e("call: startProgress -> ${Math.ceil(it.currentPlayTime / 1000.0)}s")
            }
            progressAnimator.start()
        }

        largenAnimator.addUpdateListener {
            outCircleScale = it.animatedValue as Float
        }
        largenAnimator.start()
        lessenAnimator.addUpdateListener {
            circleScale = it.animatedValue as Float
        }
        lessenAnimator.start()
    }

    private fun stopProgress() {
        L.e("call: stopProgress -> ${Math.ceil(progressAnimator.currentPlayTime / 1000.0)}s")
        if (progressAnimator.isStarted || progressAnimator.isRunning) {
            progressAnimator.cancel()
        }

        largenReAnimator.addUpdateListener {
            outCircleScale = it.animatedValue as Float
        }
        largenReAnimator.start()
        lessenReAnimator.addUpdateListener {
            circleScale = it.animatedValue as Float
            postInvalidate()
        }
        lessenReAnimator.start()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        // canvas.drawColor(Color.RED)
        val cx = measuredWidth / 2
        val cr = circleDrawRadius
        val cy = measuredHeight - childHeight - cr - circleOffset

        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 0f

        //绘制外圈
        outCircleRect.set(cx - outCircleDrawRadius * outCircleScale,
                cy - outCircleDrawRadius * outCircleScale,
                cx + outCircleDrawRadius * outCircleScale,
                cy + outCircleDrawRadius * outCircleScale)
        paint.color = outCircleColor
        canvas.drawCircle(cx.toFloat(), cy, outCircleDrawRadius * outCircleScale, paint)

        //绘制内圈
        circleRect.set((cx - cr).toInt(), (cy - cr).toInt(), (cx + cr).toInt(), (cy + cr).toInt())
        paint.color = circleColor
        canvas.drawCircle(cx.toFloat(), cy, cr * circleScale, paint)

        //绘制进度
        if (isLongPress) {
            paint.style = Paint.Style.STROKE
            paint.color = circleColor

            paint.strokeWidth = 10 * density
            outCircleRect.inset(progressWidth / 2, progressWidth / 2)

            paint.strokeWidth = 0f
            paint.color = progressColor

            //绘制进度文本
            val time = "${Math.ceil(progressAnimator.currentPlayTime / 1000.0)} s"
            canvas.drawText(time, cx - paint.measureText(time) / 2, outCircleRect.top - 20 * density, paint)

            //进度的宽度
            paint.strokeWidth = progressWidth
            canvas.drawArc(outCircleRect, -90f, progress, false, paint)
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)

        if (y == 0) {
            state = STATE_EXPAND
        } else if (y == -childHeight) {
            state = STATE_CLOSE
        } else {
            state = STATE_SCROLL_ING
        }
    }

    private fun startScroll(to: Int) {
        val scrollY = scrollY
        scroller.startScroll(0, scrollY, 0, to - scrollY, 500)
        postInvalidate()
    }


    fun expandLayout(expand: Boolean) {
        if (state == STATE_SCROLL_ING) {
            return
        }
        if (expand) {
            startScroll(0)
        } else {
            startScroll(-childHeight)
        }
    }

    companion object {
        //未展开
        val STATE_CLOSE = 0
        //正在滚动
        val STATE_SCROLL_ING = 1
        //滚动结束
        val STATE_SCROLL_IDLE = 2
        //展开
        val STATE_EXPAND = 3

        const val ANIM_TIME = 200L
        const val MAX_SCALE = 1.2f
        const val MIN_SCALE = 0.8f
    }
}