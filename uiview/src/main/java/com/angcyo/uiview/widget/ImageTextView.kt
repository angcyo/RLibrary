package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.scaledDensity

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：图片和文本混排的View
 * 创建人员：Robi
 * 创建时间：2017/06/26 17:27
 * 修改人员：Robi
 * 修改时间：2017/06/26 17:27
 * 修改备注：
 * Version: 1.0.0
 */
class ImageTextView(context: Context, attributeSet: AttributeSet? = null) : ViewGroup(context, attributeSet) {

    /**需要绘制显示的文本*/
    var showText: String? = null
        set(value) {
            field = value
            postInvalidate()
        }
    var showTextSize: Int = 14
    var textOffset: Int = 0
        get() {
            if (showText.isNullOrEmpty()) {
                return 0
            }
            return field
        }
    var textShowColor: Int = Color.parseColor("#333333")

    var imageSize: Int = 0

    val textPaint: Paint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG)
    }

    val imageView: CircleImageView by lazy {
        val view = CircleImageView(context)
        view.showType = CircleImageView.CIRCLE
        view.scaleType = ImageView.ScaleType.CENTER_CROP
        view
    }

    init {

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ImageTextView)
        showText = typedArray.getString(R.styleable.ImageTextView_r_show_text)
        showTextSize = typedArray.getDimensionPixelOffset(R.styleable.ImageTextView_r_show_text_size, (showTextSize * scaledDensity).toInt())
        textOffset = typedArray.getDimensionPixelOffset(R.styleable.ImageTextView_r_text_offset, 0)
        imageSize = typedArray.getDimensionPixelOffset(R.styleable.ImageTextView_r_image_size, (30 * density).toInt())
        textShowColor = typedArray.getColor(R.styleable.ImageTextView_r_show_text_color, textShowColor)
        typedArray.recycle()

        setWillNotDraw(false)

        val drawable = ContextCompat.getDrawable(context, R.drawable.base_image_placeholder_shape)
        imageView.setImageDrawable(drawable)
        addView(imageView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        val imageMeasureSpec = MeasureSpec.makeMeasureSpec(imageSize, MeasureSpec.EXACTLY)
        imageView.measure(imageMeasureSpec, imageMeasureSpec)

        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = (imageView.measuredWidth + textOffset + textWidth).toInt()
        }
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = Math.max(imageView.measuredHeight, textHeight.toInt())
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        imageView.layout(paddingLeft, paddingTop, paddingLeft + imageView.measuredWidth + paddingRight, measuredHeight - paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!showText.isNullOrEmpty()) {
            //绘制需要显示的文本文本
            val rawHeight = measuredHeight - paddingTop - paddingBottom
            textPaint.textSize = showTextSize.toFloat()
            textPaint.color = textShowColor
            canvas.drawText(showText, (paddingLeft + imageWidth + textOffset).toFloat(),
                    paddingTop + rawHeight / 2 + textHeight / 2 - textPaint.descent(), textPaint)
        }
    }

    val textHeight: Float
        get() {
            textPaint.textSize = showTextSize.toFloat()
            return textPaint.descent() - textPaint.ascent()
        }
    val textWidth: Float
        get() {
            if (showText.isNullOrEmpty()) {
                return 0f
            }
            textPaint.textSize = showTextSize.toFloat()
            return textPaint.measureText(showText)
        }

    val imageWidth: Int
        get() {
            return imageView.measuredWidth
        }
}