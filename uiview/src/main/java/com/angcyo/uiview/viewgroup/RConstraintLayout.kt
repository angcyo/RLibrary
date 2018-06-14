package com.angcyo.uiview.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.angcyo.uiview.R
import com.angcyo.uiview.draw.RDrawLine

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/04/27 18:09
 * 修改人员：Robi
 * 修改时间：2018/04/27 18:09
 * 修改备注：
 * Version: 1.0.0
 */
class RConstraintLayout : ConstraintLayout {

    private var drawLine: RDrawLine
    private var mRBackgroundDrawable: Drawable? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RConstraintLayout)
        mRBackgroundDrawable = typedArray.getDrawable(R.styleable.RConstraintLayout_r_background)
        drawLine = RDrawLine(this, attrs)

        typedArray.recycle()
    }

    init {
//        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RConstraintLayout)
//        mRBackgroundDrawable = typedArray.getDrawable(R.styleable.RConstraintLayout_r_background)
//        drawLine = RDrawLine(this, attributeSet)
//
//        typedArray.recycle()

        setWillNotDraw(false)

        //L.e("call: init -> ")
    }

    override fun draw(canvas: Canvas) {
        mRBackgroundDrawable?.let {
            it.bounds = canvas.clipBounds
            it.draw(canvas)
        }
        super.draw(canvas)
        drawLine.onDraw(canvas)
//        canvas.drawColor(Color.GREEN)
//        if (!isEnabled && showNoEnableMark) {
//            canvas.drawColor(ContextCompat.getColor(context, R.color.default_base_tran_dark2))
//        }
    }

    fun setRBackgroundDrawable(@ColorInt color: Int) {
        setRBackgroundDrawable(ColorDrawable(color))
    }

    fun setRBackgroundDrawable(drawable: Drawable) {
        mRBackgroundDrawable = drawable
        postInvalidate()
    }

    fun getRDrawLine(): RDrawLine {
        return drawLine
    }

}