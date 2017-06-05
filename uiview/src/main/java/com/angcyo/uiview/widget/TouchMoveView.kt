package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.v4.view.MotionEventCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import com.angcyo.uiview.R

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
    var mShowTextSize = 12f
    var mTextColorNormal = Color.GRAY
        set(value) {
            field = value
            if (!mSelected) {
                mPaint.color = value
            }
        }
    var mTextColorSelected = Color.BLACK
        set(value) {
            field = value
            if (mSelected) {
                mPaint.color = value
            }
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
    var imageWidth = 0
        get() {
            if (mSelected) {
                if (mDrawableSelected == null) {
                    return 0
                } else {
                    return mDrawableSelected!!.intrinsicWidth
                }
            } else {
                if (mDrawableNormal == null) {
                    return 0
                } else {
                    return mDrawableNormal!!.intrinsicWidth
                }
            }
        }
    var imageHeight = 0
        get() {
            if (mSelected) {
                if (mDrawableSelected == null) {
                    return 0
                } else {
                    return mDrawableSelected!!.intrinsicHeight
                }
            } else {
                if (mDrawableNormal == null) {
                    return 0
                } else {
                    return mDrawableNormal!!.intrinsicHeight
                }
            }
        }

    /**需要绘制的Drawable*/
    var mDrawableNormal: Drawable? = null
        set(value) {
            value?.setBounds(0, 0, value.intrinsicWidth, value.intrinsicHeight)
            field = value
        }
    var mDrawableSelected: Drawable? = null
        set(value) {
            value?.setBounds(0, 0, value.intrinsicWidth, value.intrinsicHeight)
            field = value
        }

    var drawDrawable: Drawable? = null
        get() {
            return if (mSelected) mDrawableSelected else mDrawableNormal
        }

    /**最大允许图片移动的四向的距离*/
    var mMaxMoveOffset = 4 * density

    /**是否是选中状态*/
    var mSelected = false

    /**用来控制图片偏移的距离*/
    var mDrawOffsetX = 0f
    var mDrawOffsetY = 0f

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        initView(context, attr)
    }

    fun initView(context: Context, attr: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.TouchMoveView)
        mShowText = typedArray.getString(R.styleable.TouchMoveView_r_show_text)
        mShowTextSize = typedArray.getDimension(R.styleable.TouchMoveView_r_show_text_size, mShowTextSize)

        mTextColorNormal = typedArray.getColor(R.styleable.TouchMoveView_r_text_color_normal, mTextColorNormal)
        mTextColorSelected = typedArray.getColor(R.styleable.TouchMoveView_r_text_color_selected, mTextColorSelected)

        mDrawableNormal = typedArray.getDrawable(R.styleable.TouchMoveView_r_image_normal)
        mDrawableSelected = typedArray.getDrawable(R.styleable.TouchMoveView_r_image_selected)

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

        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = Math.max(imageWidth, textWidth)
            newWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        }

        if (heightMode == MeasureSpec.AT_MOST) {
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
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (MotionEventCompat.getActionMasked(event)) {
            ACTION_DOWN, ACTION_MOVE -> {
                downX = event.x
                downY = event.y

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

                    //L.e("距离X:$dx  距离Y:$dy  弧度:$atan  角度:$degrees cos:${Math.cos(atan)} x:$mDrawOffsetX  y:$mDrawOffsetY max:$mMaxMoveOffset")

                    postInvalidate()
                }
            }
            ACTION_CANCEL, ACTION_UP -> {
                downX = 0f
                downY = 0f
                mDrawOffsetX = 0f
                mDrawOffsetY = 0f

                lastDegrees = null

                postInvalidate()

//                for (i in 0..720) {
//                    val radians = Math.toRadians(i.toDouble())
//                    L.e("角度:$i 弧度:$radians cos:${Math.cos(radians)} sin:${Math.sin(radians)}")
//                }

//                for (i in -180..180) {
//                    L.e("弧度:$i cos:${Math.cos()} sin:${Math.sin()}")
//                }
            }
        }

        return false
    }

    override fun onDraw(canvas: Canvas) {
        //绘制图片
        if (drawDrawable != null) {
            canvas.save()
            canvas.translate(measuredWidth / 2 - imageWidth / 2 + mDrawOffsetX,
                    measuredHeight / 2 - subHeight / 2 + mDrawOffsetY)
            drawDrawable!!.draw(canvas)
            canvas.restore()
        }

        //绘制文本
        if (!mShowText.isNullOrEmpty()) {
            canvas.drawText(mShowText,
                    (measuredWidth / 2 - textWidth / 2).toFloat(),
                    measuredHeight / 2 + subHeight / 2 - mPaint.descent(),
                    mPaint)
        }
    }
}