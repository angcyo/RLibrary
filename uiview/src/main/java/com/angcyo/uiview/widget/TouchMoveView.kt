package com.angcyo.uiview.widget

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.MotionEventCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.animation.LinearInterpolator
import com.angcyo.uiview.R
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模仿QQ底部按钮, touch的时候, 图标会跟着手势移动
 * 创建人员：Robi
 * 创建时间：2017/06/05 14:33
 * 修改人员：Robi
 * 修改时间：2017/06/05 14:33
 * 修改备注：
 * Version: 1.0.0
 */
class TouchMoveView : View {

    val mPaint: TextPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = mShowTextSize * scaledDensity }
    }

    val scaledDensity: Float by lazy { resources.displayMetrics.scaledDensity }
    val density: Float by lazy { resources.displayMetrics.density }

    /**需要绘制显示的文本*/
    var mShowText: String? = null
        get() {
            return if (mSelected) textSelected else textNormal
        }

    var textNormal: String? = null
    var textSelected: String? = null
    var mShowTextSize = 12f
        set(value) {
            field = value
            mPaint.textSize = value
        }
    var mTextColorNormal by ColorSetDelegate()
    var mTextColorSelected by ColorSetDelegate(true)

    val textDrawColor: Int
        get() {
            return if (mSelected) mTextColorSelected else mTextColorNormal
        }

    /**图片文本之间的间隙*/
    var mTextSpace = 4f
        get() {
            if (mShowText.isNullOrEmpty()) {
                return 0f
            } else {
                return field
            }
        }
    var textWidth = 0
        get() {
            return mPaint.measureText(mShowText).toInt()
        }
    var textHeight = 0f
        get() {
            if (mShowText.isNullOrEmpty()) {
                return 0f
            } else {
                return mPaint.fontMetrics.descent - mPaint.fontMetrics.ascent
            }
        }
    val imageWidth by DrawableWidthDelegate()
    val imageHeight by DrawableHeightDelegate()

    /**需要绘制的Drawable*/
    var mDrawableNormal: Drawable? by DrawableDelegate()

    var mDrawableSelected: Drawable? by DrawableDelegate()

    var mSubDrawableNormal: Drawable? by DrawableDelegate()

    var mSubDrawableSelected: Drawable? by DrawableDelegate()

    var drawDrawable: Drawable? = null
        get() {
            return if (mSelected) mDrawableSelected else mDrawableNormal
        }
    var subDrawDrawable: Drawable? = null
        get() {
            return if (mSelected) mSubDrawableSelected else mSubDrawableNormal
        }

    /**最大允许图片移动的四向的距离*/
    var mMaxMoveOffset = 4 * density
    var mSubMaxMoveOffset = 6 * density

    /**是否是选中状态*/
    var mSelected = false
        set(value) {
            field = value
            if (value) {
                mPaint.color = mTextColorSelected
            } else {
                mPaint.color = mTextColorNormal
            }
            postInvalidate()
        }

    /**用来控制图片偏移的距离*/
    var mDrawOffsetX = 0f
    var mDrawOffsetY = 0f

    var mSubDrawOffsetX = 0f
    var mSubDrawOffsetY = 0f

    /**在非选择状态下, sub默认的偏移距离,相对于mSubMaxMoveOffset的比例, 0..1f*/
    var mStartSubDrawOffsetRatioX = 0f
    var mStartSubDrawOffsetRatioY = 0f

    /**未读消息数量, -1表示不显示, 0表示显示一个小红点, 1..99显示数字+小红点, 99+显示特殊*/
    var noReadNum = -1
        set(value) {
            field = value
            postInvalidate()
        }

    /**小红点*/
    val redDotDrawable: Drawable by lazy {
        val drawable = ContextCompat.getDrawable(context, R.drawable.skin_tips_dot_png)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable
    }
    /**.9格式的小红点*/
    val newMessageDrawable: Drawable by lazy {
        val drawable = ContextCompat.getDrawable(context, R.drawable.skin_tips_newmessage_9_png)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable
    }
    /**99+*/
    val ninetyNineDrawable: Drawable by lazy {
        val drawable = ContextCompat.getDrawable(context, R.drawable.skin_tips_newmessage_ninetynine_png)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable
    }

    /**贝塞尔曲线的path*/
    val besselPath = Path()

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        initView(context, attr)
    }

    fun initView(context: Context, attr: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.TouchMoveView)
        mShowText = typedArray.getString(R.styleable.TouchMoveView_r_show_text)
        mShowTextSize = typedArray.getDimension(R.styleable.TouchMoveView_r_show_text_size, mShowTextSize * scaledDensity)

        mTextColorNormal = typedArray.getColor(R.styleable.TouchMoveView_r_text_color_normal, mTextColorNormal)
        mTextColorSelected = typedArray.getColor(R.styleable.TouchMoveView_r_text_color_selected, mTextColorSelected)

        mDrawableNormal = typedArray.getDrawable(R.styleable.TouchMoveView_r_image_normal)
        mDrawableSelected = typedArray.getDrawable(R.styleable.TouchMoveView_r_image_selected)

        mSubDrawableNormal = typedArray.getDrawable(R.styleable.TouchMoveView_r_sub_image_normal)
        mSubDrawableSelected = typedArray.getDrawable(R.styleable.TouchMoveView_r_sub_image_selected)

        mTextSpace = typedArray.getDimension(R.styleable.TouchMoveView_r_text_space, mTextSpace)
        mMaxMoveOffset = typedArray.getDimension(R.styleable.TouchMoveView_r_max_draw_offset, mMaxMoveOffset)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var newWidthSpec = widthMeasureSpec
        var newHeightSpec = heightMeasureSpec

        if (widthMode == MeasureSpec.AT_MOST || widthMode == View.MeasureSpec.UNSPECIFIED) {
            widthSize = Math.max(imageWidth, textWidth)
            newWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        }

        if (heightMode == MeasureSpec.AT_MOST || heightMode == View.MeasureSpec.UNSPECIFIED) {
            heightSize = (imageHeight + textHeight).toInt()
            newHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
        }
        super.onMeasure(newWidthSpec, newHeightSpec)
    }

    var downX = 0f
    var downY = 0f

    /**图片和文本和空隙的总高度*/
    var subHeight = 0f
        get() {
            return imageHeight + mTextSpace + textHeight
        }

    /**图片中心的坐标*/
    var imageCenterX: Float = 0.0f
        get() {
            return (measuredWidth / 2).toFloat()
        }

    var imageCenterY: Float = 0.0f
        get() {
            if (drawDrawable == null) {
                return (measuredHeight / 2).toFloat()
            } else {
                return (measuredHeight - subHeight) / 2 + imageHeight / 2
            }
        }

    var lastDegrees: Double? = null
    var animScale = 1f
        set(value) {
            field = value
            //L.e("scale:$field")
            postInvalidate()
        }

    val scaleAnimation: ValueAnimator by lazy {
        ObjectAnimator.ofObject(TypeEvaluator<Float> { fraction, startValue, endValue -> startValue + fraction * (endValue - startValue) }, 0.6f, 1.0f)
                .apply {
                    addUpdateListener { animation -> animScale = animation.animatedValue as Float }
                    duration = 200
                    interpolator = LinearInterpolator()
                }
    }

    var isTouchDown: Boolean = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val eventX = event.x
        val eventY = event.y

        when (MotionEventCompat.getActionMasked(event)) {
            ACTION_DOWN -> {
                //scaleAnimation.cancel()
                isTouchDown = true
                if (!mSelected) {
                    scaleAnimation.start()
                }
            }
            ACTION_MOVE -> {
                downX = eventX
                downY = eventY
                isTouchDown = true

                val dx = downX - imageCenterX
                val dy = downY - imageCenterY

                var atan = Math.atan((dy / dx).toDouble())
                val degrees = Math.toDegrees(atan)
                if (lastDegrees != null && Math.abs(lastDegrees!! - degrees) < 5) {

                } else {
                    lastDegrees = degrees

                    if (dx < 0) {
                        atan -= Math.PI
                    }

                    //mDrawOffsetX = Math.max(Math.min(moveX - downX, mMaxMoveOffset), -mMaxMoveOffset)
                    //mDrawOffsetY = Math.max(Math.min(moveY - downY, mMaxMoveOffset), -mMaxMoveOffset)

                    mDrawOffsetX = (mMaxMoveOffset * Math.cos(atan)).toFloat()
                    mDrawOffsetY = (mMaxMoveOffset * Math.sin(atan)).toFloat()

                    mSubDrawOffsetX = (mSubMaxMoveOffset * Math.cos(atan)).toFloat()
                    mSubDrawOffsetY = (mSubMaxMoveOffset * Math.sin(atan)).toFloat()

                    //L.e("距离X:$dx  距离Y:$dy  弧度:$atan  角度:$degrees cos:${Math.cos(atan)} x:$mDrawOffsetX  y:$mDrawOffsetY max:$mMaxMoveOffset")

                    postInvalidate()
                }
            }
            ACTION_UP -> {
                onTouchUp()
                if (eventX > 0 && eventX < measuredWidth &&
                        eventY > 0 && eventY < measuredHeight) {
                    if (parent is TouchMoveGroupLayout) {
                        (parent as TouchMoveGroupLayout).updateSelector(this)
                    }
                }
//                for (i in 0..720) {
//                    val radians = Math.toRadians(i.toDouble())
//                    L.e("角度:$i 弧度:$radians cos:${Math.cos(radians)} sin:${Math.sin(radians)}")
//                }

//                for (i in -180..180) {
//                    L.e("弧度:$i cos:${Math.cos()} sin:${Math.sin()}")
//                }
            }
            ACTION_CANCEL -> {
                onTouchUp()
            }
        }

        return true
    }

    private fun onTouchUp() {
        downX = 0f
        downY = 0f
        mDrawOffsetX = 0f
        mDrawOffsetY = 0f

        mSubDrawOffsetX = 0f
        mSubDrawOffsetY = 0f

        lastDegrees = null

        isTouchDown = false
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        mPaint.textSize = mShowTextSize
        //绘制图片
        if (drawDrawable != null) {
            canvas.translate((measuredWidth / 2).toFloat(), measuredHeight / 2 - subHeight / 2 + imageHeight / 2)
            canvas.scale(animScale, animScale)

            canvas.save()
            canvas.translate(-imageWidth / 2 + mDrawOffsetX,
                    -imageHeight / 2 + mDrawOffsetY)
            drawDrawable!!.draw(canvas)
            canvas.restore()

            if (subDrawDrawable != null) {
                canvas.save()
                if (!isTouchDown && !mSelected) {
                    canvas.translate(-imageWidth / 2 + mStartSubDrawOffsetRatioX * mSubMaxMoveOffset,
                            -imageHeight / 2 + mStartSubDrawOffsetRatioY * mSubMaxMoveOffset)
                } else {
                    canvas.translate(-imageWidth / 2 + mSubDrawOffsetX,
                            -imageHeight / 2 + mSubDrawOffsetY)
                }
                subDrawDrawable!!.draw(canvas)
                canvas.restore()
            }
        }
        canvas.restore()

        //绘制文本
        if (!mShowText.isNullOrEmpty()) {
            mPaint.color = textDrawColor
            canvas.drawText(mShowText,
                    (measuredWidth / 2 - textWidth / 2).toFloat(),
                    measuredHeight / 2 + subHeight / 2 - mPaint.descent(),
                    mPaint)
        }

        //绘制未读消息
        canvas.save()
        canvas.translate((measuredWidth / 2).toFloat(), paddingTop + 4 * density)//移动到中间位置
        when {
            noReadNum == 0 -> {
                redDotDrawable.draw(canvas)
            }
            noReadNum in 1..99 -> {
                mPaint.textSize = 9 * scaledDensity
                val string = noReadNum.toString()
                val paddingTop = 2 * density
                val paddingLeft = 2 * paddingTop
                newMessageDrawable.setBounds(0, 0,
                        (mPaint.measureText(string, 0, string.length) + 2 * paddingLeft).toInt(),
                        (mPaint.descent() - mPaint.ascent() + 2 * paddingTop).toInt())
                newMessageDrawable.draw(canvas)
                mPaint.color = Color.WHITE
                canvas.drawText(string, paddingLeft, paddingTop - mPaint.ascent(), mPaint)
            }
            (noReadNum > 99) -> {
                ninetyNineDrawable.draw(canvas)
            }
        }
        canvas.restore()

//        //绘制贝塞尔取消
//        canvas.save()
//        val besselCircleRadius = 8 * density
//        besselPath.reset()
//        besselPath.moveTo((measuredWidth / 2).toFloat(), measuredHeight.toFloat())
//        besselPath.quadTo((measuredWidth / 2).toFloat() + besselCircleRadius, (measuredHeight / 2).toFloat(), (measuredWidth / 2).toFloat(), 0f)
//        besselPath.lineTo(measuredWidth / 2 + besselCircleRadius * 2, 0f)
//        besselPath.quadTo((measuredWidth / 2).toFloat() + besselCircleRadius / 2, (measuredHeight / 2).toFloat(), (measuredWidth / 2).toFloat() + besselCircleRadius * 2, measuredHeight.toFloat())
//        mPaint.style = Paint.Style.STROKE
//        mPaint.strokeWidth = 2 * density
//        mPaint.color = Color.RED
//        canvas.drawPath(besselPath, mPaint)
//        canvas.restore()
    }
}

private class DrawableDelegate : ReadWriteProperty<TouchMoveView, Drawable?> {
    private var value: Drawable? = null

    override fun getValue(thisRef: TouchMoveView, property: KProperty<*>): Drawable? {
        return value
    }

    override fun setValue(thisRef: TouchMoveView, property: KProperty<*>, value: Drawable?) {
        this.value = value
        value?.setBounds(0, 0, value.intrinsicWidth, value.intrinsicHeight)
    }
}

private class DrawableWidthDelegate : ReadOnlyProperty<TouchMoveView, Int> {

    override fun getValue(thisRef: TouchMoveView, property: KProperty<*>): Int {
        if (thisRef.mSelected) {
            if (thisRef.mDrawableSelected == null) {
                return 0
            } else {
                return thisRef.mDrawableSelected!!.intrinsicWidth
            }
        } else {
            if (thisRef.mDrawableNormal == null) {
                return 0
            } else {
                return thisRef.mDrawableNormal!!.intrinsicWidth
            }
        }
    }
}

private class DrawableHeightDelegate : ReadOnlyProperty<TouchMoveView, Int> {

    override fun getValue(thisRef: TouchMoveView, property: KProperty<*>): Int {
        if (thisRef.mSelected) {
            if (thisRef.mDrawableSelected == null) {
                return 0
            } else {
                return thisRef.mDrawableSelected!!.intrinsicHeight
            }
        } else {
            if (thisRef.mDrawableNormal == null) {
                return 0
            } else {
                return thisRef.mDrawableNormal!!.intrinsicHeight
            }
        }
    }
}

private class ColorSetDelegate(var isSelectorColor: Boolean = false) : ReadWriteProperty<TouchMoveView, Int> {
    private var value: Int? = null

    init {
        if (isSelectorColor) {
            value = Color.parseColor("#3DB8FF")
        } else {
            value = Color.GRAY
        }
    }

    override fun getValue(thisRef: TouchMoveView, property: KProperty<*>): Int {
        return value!!
    }

    override fun setValue(thisRef: TouchMoveView, property: KProperty<*>, value: Int) {
        if (isSelectorColor == thisRef.mSelected) {
            thisRef.mPaint.color = value
        }
        this.value = value
    }
}
