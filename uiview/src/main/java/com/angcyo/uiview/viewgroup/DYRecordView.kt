package com.angcyo.uiview.viewgroup

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import com.angcyo.library.utils.L
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.getDrawCenterTextCx
import com.angcyo.uiview.kotlin.maxValue
import com.angcyo.uiview.kotlin.textHeight
import com.angcyo.uiview.resources.RAnimListener
import com.angcyo.uiview.skin.SkinHelper

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模仿抖音视频录制按钮
 * 创建人员：Robi
 * 创建时间：2017/09/19 17:12
 * 修改人员：Robi
 * 修改时间：2017/09/19 17:12
 * 修改备注：
 * Version: 1.0.0
 */
class DYRecordView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {
    companion object {
        /**内圈呼吸的半径范围*/
        val INNER_CIRCLE_MIN_R = 0.8f
        val INNER_CIRCLE_MAX_R = 0.95f
    }

    var showText = "按住拍"
    var showTextSize = 20 * density
    var showTextColor = Color.WHITE

    /**圈圈的颜色*/
    var circleColor = Color.RED
    /**默认时, 圈的半径*/
    var circleDefaultRadius = 40 * density
    /**开始录时, 圈允许放大到的倍数*/
    var circleMaxScale = 1.5f

    /**推荐使用 Match_parent*/
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**默认情况下, 距离底部的距离*/
    var defaultCircleOffsetBottom = 30 * density

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DYRecordView)
        val string = typedArray.getString(R.styleable.DYRecordView_r_show_text)
        if (string != null) {
            showText = string
        }
        showTextSize = typedArray.getDimensionPixelOffset(R.styleable.DYRecordView_r_show_text_size, showTextSize.toInt()).toFloat()
        showTextColor = typedArray.getColor(R.styleable.DYRecordView_r_show_text_color, showTextColor)
        if (isInEditMode) {
            circleColor = Color.RED
        } else {
            circleColor = SkinHelper.getSkin().themeSubColor
        }
        circleColor = typedArray.getColor(R.styleable.DYRecordView_r_circle_color, circleColor)
        circleDefaultRadius = typedArray.getDimensionPixelOffset(R.styleable.DYRecordView_r_circle_default_radius, circleDefaultRadius.toInt()).toFloat()
        circleMaxScale = typedArray.getFloat(R.styleable.DYRecordView_r_circle_max_scale, circleMaxScale)
        defaultCircleOffsetBottom = typedArray.getDimensionPixelOffset(R.styleable.DYRecordView_r_default_circle_offset_bottom, defaultCircleOffsetBottom.toInt()).toFloat()

        typedArray.recycle()
    }


    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    private val textPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = showTextSize
            color = showTextColor
        }
    }

    //圈绘制时, 的x坐标
    private val cx: Float
        get() = ((measuredWidth - paddingLeft - paddingRight) / 2 + paddingLeft).toFloat()

    //y坐标
    private val cy: Float
        get() = measuredHeight - paddingBottom - defaultCircleOffsetBottom - circleDefaultRadius

    private var drawCX: Float = 0f
    private var drawCY: Float = 0f

    /*保存动画需要结束时的各项值*/
    private var drawEndCX: Float = 0f
    private var drawEndCY: Float = 0f
    private var circleDrawEndRadius: Float = 0f
    private var circleInnerDrawEndScale = 0f
    private var showTextSizeDrawEnd = 0f

    //绘制时, 圆圈的半径
    private var circleDrawRadius: Float = circleDefaultRadius

    private val circleRect by lazy {
        RectF()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        drawCX = cx
        drawCY = cy
        circleDrawRadius = circleDefaultRadius
        showTextSizeDraw = showTextSize

        circleRect.set(cx - circleDefaultRadius, cy - circleDefaultRadius, cx + circleDefaultRadius, cy + circleDefaultRadius)

        circleBitmap?.recycle()
        if (measuredWidth != 0 && measuredHeight != 0) {
            circleBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            circleCanvas = Canvas(circleBitmap)
        }
    }

    private var circleCanvas: Canvas? = null
    private var circleBitmap: Bitmap? = null
    private var circleInnerDrawScale = 0f
    private var showTextSizeDraw = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //绘制圆
        circleCanvas?.let {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            it.drawPaint(paint)

            paint.xfermode = null
            paint.color = circleColor
            circleCanvas?.drawCircle(drawCX, drawCY, circleDrawRadius, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
            paint.color = Color.TRANSPARENT
            circleCanvas?.drawCircle(drawCX, drawCY, circleDrawRadius * circleInnerDrawScale, paint)
            paint.xfermode = null

            canvas.drawBitmap(circleBitmap, 0f, 0f, null)
        }

        canvas.save()
        //绘制文本
        textPaint.textSize = showTextSizeDraw
        canvas.drawText(showText, getDrawCenterTextCx(textPaint, showText), cy + textHeight(textPaint) / 2 - textPaint.descent(), textPaint)
        canvas.restore()

        recordingListener()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return super.onTouchEvent(event)
        } else {
            when (event.actionMasked) {
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    endRecord()
                }
            }
            return gestureCompat.onTouchEvent(event)
        }
    }

    /**调用此方法, 结束录制*/
    fun endRecord(notify: Boolean = true) {
        drawEndCX = drawCX
        drawEndCY = drawCY
        circleDrawEndRadius = circleDrawRadius
        circleInnerDrawEndScale = circleInnerDrawScale
        showTextSizeDrawEnd = showTextSizeDraw

        touchInAnimator?.cancel()
        breathAnimator?.cancel()
        reset()

        if (notify) {
            endRecordListener()
        }
    }

    /*手势松开, 恢复动画*/
    private var resetAnimator: ValueAnimator? = null

    private fun reset() {
        if (resetAnimator == null) {
            resetAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
                interpolator = AccelerateInterpolator()
                duration = 200
                addUpdateListener { animation ->
                    val animatedValue: Float = animation.animatedValue as Float
                    drawCX = cx + (drawEndCX - cx) * animatedValue
                    drawCY = cy + (drawEndCY - cy) * animatedValue
                    circleDrawRadius = circleDefaultRadius + (circleDrawEndRadius - circleDefaultRadius) * animatedValue
                    circleInnerDrawScale = (animatedValue).maxValue(circleInnerDrawEndScale)
                    showTextSizeDraw = showTextSizeDrawEnd + (showTextSize - showTextSizeDrawEnd) * (1 - animatedValue)

                    postInvalidate()
                }
                addListener(object : RAnimListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        resetAnimator = null
                    }
                })
                start()
            }
        }
    }

    /*控制手势事件*/
    private val gestureCompat: GestureDetectorCompat by lazy {
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(event: MotionEvent) {
                L.e("call: onLongPress -> ")
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                drawCX -= distanceX
                drawCY -= distanceY
                postInvalidate()
                return true
            }

            override fun onDown(event: MotionEvent): Boolean {
                val contains = circleRect.contains(event.x, event.y)
                if (contains) {
                    onTouchInCircle()
                }
                return contains
            }
        }).apply {
            setIsLongpressEnabled(false)
        }
    }

    /*手势在圆圈内触发*/
    private var touchInAnimator: ValueAnimator? = null

    private fun onTouchInCircle() {
        if (touchInAnimator == null) {
            touchInAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                interpolator = AccelerateInterpolator()
                duration = 200
                addUpdateListener { animation ->
                    val animatedValue: Float = animation.animatedValue as Float
                    circleDrawRadius = circleDefaultRadius * (1 + (circleMaxScale - 1) * animatedValue)
                    circleInnerDrawScale = (animatedValue).maxValue(INNER_CIRCLE_MIN_R)
                    showTextSizeDraw = showTextSize * (1 - animatedValue)

                    postInvalidate()
                }
                addListener(object : RAnimListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        touchInAnimator = null
                        startBreath()
                    }
                })
                start()
            }
        }
    }

    /*呼吸动画*/
    private var breathAnimator: ValueAnimator? = null

    private fun startBreath() {
        if (breathAnimator == null) {
            breathAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                interpolator = LinearInterpolator()
                duration = 700
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener { animation ->
                    val animatedValue: Float = animation.animatedValue as Float
                    circleInnerDrawScale = INNER_CIRCLE_MIN_R + (INNER_CIRCLE_MAX_R - INNER_CIRCLE_MIN_R) * animatedValue
                    postInvalidate()
                }
                addListener(object : RAnimListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        breathAnimator = null
                    }
                })
                start()
            }
        }
        startRecordListener()
    }

    private var isRecording = false
    private var startRecordTime = 0L
    //开始录制
    private fun startRecordListener() {
        if (isRecording) {
            return
        }
        isRecording = true
        startRecordTime = System.currentTimeMillis()
        onRecordListener?.onRecordStart()
    }

    //结束录制
    private fun endRecordListener() {
        if (isRecording) {
            isRecording = false
            val progress = getRecordProgress()
            startRecordTime = 0L
            onRecordListener?.onRecordEnd(progress[0], progress[1])
        }
    }

    //录制中
    private var recordProgress = -1

    private fun recordingListener() {
        if (isRecording) {
            val progress = getRecordProgress()
            if (recordProgress != progress[1]) {
                recordProgress = progress[1]
                onRecordListener?.onRecording(progress[0], progress[1])
            }
        }
    }

    private fun getRecordProgress(): IntArray {
        val currentTimeMillis = System.currentTimeMillis()
        return intArrayOf(((currentTimeMillis - startRecordTime) / 1000).toInt(), ((currentTimeMillis - startRecordTime).toInt()))
    }

    var onRecordListener: OnRecordListener? = null

    public abstract class OnRecordListener {
        open fun onRecordStart() {

        }

        open fun onRecordEnd(second: Int, millisecond: Int) {

        }

        open fun onRecording(second: Int /*录制了多少秒*/, millisecond: Int /*毫秒单位*/) {

        }
    }
}