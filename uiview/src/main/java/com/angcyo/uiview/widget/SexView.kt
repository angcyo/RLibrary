package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.getDrawCenterTextCy
import com.angcyo.uiview.kotlin.scaledDensity
import com.angcyo.uiview.kotlin.textHeight

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：男女性别展示, 还支持年龄展示
 * 创建人员：Robi
 * 创建时间：2017/08/03 14:54
 * 修改人员：Robi
 * 修改时间：2017/08/03 14:54
 * 修改备注：
 * Version: 1.0.0
 */
open class SexView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {
    companion object {
        /*保密*/
        const val NONE = 0
        /*男*/
        const val MALE = 1
        /*女*/
        const val FEMALE = 2
    }

    private val maleDrawable: Drawable by lazy {
        ContextCompat.getDrawable(context, R.drawable.base_male_18)
    }
    private val femaleDrawable: Drawable by lazy {
        ContextCompat.getDrawable(context, R.drawable.base_female_18)
    }

    /*背景颜色*/
    var maleBgColor: Int = Color.parseColor("#7BAFF6")
    var femaleBgColor: Int = Color.parseColor("#FF7F88")

    /**性别*/
    var sex: Int = FEMALE
        set(value) {
            field = value
            postInvalidate()
        }

    /**年龄小于0时,不显示*/
    var age: Int = -1
        set(value) {
            field = value
            requestLayout()
        }

    private val paint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.WHITE
        p.textSize = 9f * scaledDensity
        p
    }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SexView)
        sex = typedArray.getInt(R.styleable.SexView_r_sex, sex)
        age = typedArray.getInt(R.styleable.SexView_r_age, age)
        typedArray.recycle()

        setPadding(2 * density.toInt(), 1 * density.toInt(), 2 * density.toInt(), 1 * density.toInt())
    }

    private fun getAgeWidth(): Int {
        if (age < 0) {
            return 0
        } else {
            return paint.measureText(age.toString()).toInt()
        }
    }

    private fun getAgeHeight(): Int {
        return textHeight(paint).toInt()
    }

    private fun getDrawableWidth(): Int {
        return maleDrawable.intrinsicWidth
    }

    private fun getDrawableHeight(): Int {
        return maleDrawable.intrinsicHeight
    }

    private fun getDrawDrawable(): Drawable {
        return when (sex) {
            MALE -> maleDrawable
            FEMALE -> femaleDrawable
            else -> femaleDrawable
        }
    }

    private fun getDrawBgColor(): Int {
        return when (sex) {
            MALE -> maleBgColor
            FEMALE -> femaleBgColor
            else -> femaleBgColor
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = paddingLeft + paddingRight + getDrawableWidth() + getAgeWidth() + ageOffset
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = paddingTop + paddingBottom + Math.max(getDrawableHeight(), getAgeHeight())
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    private val roundRect: RectF by lazy {
        val r = RectF()
        r
    }

    private val ageOffset: Int
        get() {
            if (getAgeWidth() == 0) {
                return 0
            } else {
                return 2 * density.toInt()
            }
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //绘制背景
        roundRect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = getDrawBgColor()
        canvas.drawRoundRect(roundRect, 1 * density, 1 * density, paint)

        //绘制性别图标
        val drawable = getDrawDrawable()
        val drawableRight = paddingLeft + drawable.intrinsicWidth
        drawable.setBounds(paddingLeft, measuredHeight / 2 - drawable.intrinsicHeight / 2,
                drawableRight, measuredHeight / 2 + drawable.intrinsicHeight / 2)
        drawable.draw(canvas)

        //绘制年龄
        if (age >= 0) {
            paint.style = Paint.Style.STROKE
            paint.color = Color.WHITE
            canvas.drawText(age.toString(), (drawableRight + ageOffset).toFloat(), getDrawCenterTextCy(paint), paint)
        }
    }
}