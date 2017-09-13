package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.textHeight
import com.angcyo.uiview.kotlin.textWidth
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：显示图片, 右上角可以显示文本提示的 tip View
 * 创建人员：Robi
 * 创建时间：2017/06/05 14:33
 * 修改人员：Robi
 * 修改时间：2017/06/05 14:33
 * 修改备注：
 * Version: 1.0.0
 */
class ImageTipView : View {

    companion object {
        val WRAP_CONTENT = 1
        val MATCH_PARENT = 2
    }

    /**图标测量模式*/
    private var drawableHeightMeasureSpec = WRAP_CONTENT
    private var drawableWidthMeasureSpec = WRAP_CONTENT

    val mPaint: TextPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = showTextSize * density }
    }

    /**需要绘制显示的文本*/
    var showText: String? = null
        get() {
            return if (mSelected) textSelected else textNormal
        }

    /**未读消息数量, -1表示不显示, 0表示显示一个小红点, 1..99显示数字+小红点, 99+显示特殊*/
    var tipText: String? = null
        set(value) {
            field = value
            postInvalidate()
        }
    var tipTextSize = 9f
    var tipTextColor = Color.WHITE
    /**提示文本是否有背景, .9红色背景*/
    var tipTextBg = true

    var textNormal: String? = null
    var textSelected: String? = null
    var showTextSize = 12f


    /**文本提示模式, 图标和提示文本分开, 允许提示文本远离图标绘制, 图标不变形*/
    var isTipModel = false

    var mTextColorNormal by ColorSetDelegate2()
    var mTextColorSelected by ColorSetDelegate2(true)

    val textDrawColor: Int
        get() {
            return if (mSelected) mTextColorSelected else mTextColorNormal
        }

    /**图片文本之间的间隙*/
    var mTextSpace = 4f
        get() {
            if (showText.isNullOrEmpty()) {
                return 0f
            } else {
                return field
            }
        }
    var tipTextLeftOffset = 2f
    var tipTextTopOffset = 2f

    var textWidth = 0
        get() {
            if (showText.isNullOrEmpty()) {
                return 0
            }
            return mPaint.measureText(showText).toInt()
        }
    var textHeight = 0f
        get() {
            if (showText.isNullOrEmpty()) {
                return 0f
            } else {
                return mPaint.fontMetrics.descent - mPaint.fontMetrics.ascent
            }
        }
    val imageWidth by DrawableWidthDelegate2()
    val imageHeight by DrawableHeightDelegate2()

    /**需要绘制的Drawable*/
    var mDrawableNormal: Drawable? by DrawableDelegate2()

    var mDrawableSelected: Drawable? by DrawableDelegate2()

    var drawDrawable: Drawable? = null
        get() {
            return if (mSelected) mDrawableSelected else mDrawableNormal
        }

    /**是否是选中状态*/
    var mSelected = false
        set(value) {
            field = value
            postInvalidate()
        }

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

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        initView(context, attr)
    }

    fun initView(context: Context, attr: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.ImageTipView)
        textSelected = typedArray.getString(R.styleable.ImageTipView_r_text_selected)
        textNormal = typedArray.getString(R.styleable.ImageTipView_r_text_normal)

        showTextSize = typedArray.getDimension(R.styleable.ImageTipView_r_show_text_size, showTextSize * density)
        isTipModel = typedArray.getBoolean(R.styleable.ImageTipView_r_is_tip_mode, isTipModel)

        mTextColorNormal = typedArray.getColor(R.styleable.ImageTipView_r_text_color_normal, mTextColorNormal)
        mTextColorSelected = typedArray.getColor(R.styleable.ImageTipView_r_text_color_selected, mTextColorSelected)

        mDrawableNormal = typedArray.getDrawable(R.styleable.ImageTipView_r_image_normal)
        mDrawableSelected = typedArray.getDrawable(R.styleable.ImageTipView_r_image_selected)

        mTextSpace = typedArray.getDimension(R.styleable.ImageTipView_r_text_space, mTextSpace * density)
        tipTextLeftOffset = typedArray.getDimension(R.styleable.ImageTipView_r_tip_text_left_offset, tipTextLeftOffset * density)
        tipTextTopOffset = typedArray.getDimension(R.styleable.ImageTipView_r_tip_text_top_offset, tipTextTopOffset * density)

        tipText = typedArray.getString(R.styleable.ImageTipView_r_tip_text)
        tipTextSize = typedArray.getDimension(R.styleable.ImageTipView_r_tip_text_size, tipTextSize * density)
        tipTextColor = typedArray.getColor(R.styleable.ImageTipView_r_tip_text_color, tipTextColor)
        tipTextBg = typedArray.getBoolean(R.styleable.ImageTipView_r_tip_text_bg, tipTextBg)

        noReadNum = typedArray.getInteger(R.styleable.ImageTipView_r_no_read_num, noReadNum)

        drawableHeightMeasureSpec = typedArray.getInteger(R.styleable.ImageTipView_r_drawable_height_measure_mode, drawableHeightMeasureSpec)
        drawableWidthMeasureSpec = typedArray.getInteger(R.styleable.ImageTipView_r_drawable_width_measure_mode, drawableWidthMeasureSpec)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var newWidthSpec = widthMeasureSpec
        var newHeightSpec = heightMeasureSpec

        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            var imageWidth = 0
            drawDrawable?.let {
                imageWidth = it.intrinsicWidth
            }
            widthSize = Math.max(imageWidth, textWidth)
            newWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        }

        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            var imageHeight = 0
            drawDrawable?.let {
                imageHeight = it.intrinsicHeight
            }

            heightSize = (imageHeight + textHeight + paddingBottom + paddingTop).toInt()
            newHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
        }

        super.onMeasure(newWidthSpec, newHeightSpec)

        ensureDrawable()
    }

    private fun ensureDrawable() {
        var imageWidth = 0
        drawDrawable?.let {
            imageWidth = it.intrinsicWidth
        }

        var imageHeight = 0
        drawDrawable?.let {
            imageHeight = it.intrinsicHeight
        }

        if (drawableHeightMeasureSpec == MATCH_PARENT) {
            measureDrawableH(mDrawableNormal, measuredHeight - paddingBottom - paddingTop)
            measureDrawableH(mDrawableSelected, measuredHeight - paddingBottom - paddingTop)
        } else {
            measureDrawableH(mDrawableNormal, imageHeight)
            measureDrawableH(mDrawableSelected, imageHeight)
        }

        if (drawableWidthMeasureSpec == MATCH_PARENT) {
            measureDrawableW(mDrawableNormal, measuredWidth - paddingLeft - paddingRight)
            measureDrawableW(mDrawableSelected, measuredWidth - paddingLeft - paddingRight)
        } else {
            measureDrawableW(mDrawableNormal, imageWidth)
            measureDrawableW(mDrawableSelected, imageWidth)
        }
    }

    private fun measureDrawableW(drawable: Drawable?, width: Int) {
        drawable?.let {
            it.setBounds(0, 0, width, it.bounds.height())
        }
    }

    private fun measureDrawableH(drawable: Drawable?, height: Int) {
        drawable?.let {
            it.setBounds(0, 0, it.bounds.width(), height)
        }
    }

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

    private val rawViewWidth: Int
        get() = measuredWidth - paddingLeft - paddingRight

    private val rawViewHeight: Int
        get() = measuredHeight - paddingTop - paddingBottom

    override fun onDraw(canvas: Canvas) {
        if (isInEditMode) {
            canvas.drawColor(Color.YELLOW)
        }

        var drawableCx = (rawViewWidth / 2).toFloat() + paddingLeft

        //绘制图片
        if (drawDrawable != null) {
            canvas.save()
            if (isTipModel) {
                val drawLeft = if (showText.isNullOrEmpty() || drawDrawable!!.bounds.width() > textWidth) 0 else (textWidth - drawDrawable!!.bounds.width()) / 2
                canvas.translate(paddingLeft.toFloat() + drawLeft, paddingTop.toFloat())

                drawableCx = paddingLeft.toFloat() + drawLeft + drawDrawable!!.bounds.width() / 2
            } else {
                canvas.translate((rawViewWidth / 2).toFloat() + paddingLeft, rawViewHeight / 2 - subHeight / 2 + imageHeight / 2 + paddingTop)
                canvas.translate((-imageWidth / 2).toFloat(), (-imageHeight / 2).toFloat())
            }
            drawDrawable!!.draw(canvas)
            canvas.restore()
        }

        //绘制文本
        if (!showText.isNullOrEmpty()) {
            mPaint.color = textDrawColor
            mPaint.textSize = showTextSize
            if (isTipModel) {
                canvas.drawText(showText, paddingLeft.toFloat(),
                        measuredHeight / 2 + subHeight / 2 - mPaint.descent(),
                        mPaint)
            } else {
                canvas.drawText(showText,
                        (measuredWidth / 2 - textWidth / 2).toFloat(),
                        measuredHeight / 2 + subHeight / 2 - mPaint.descent(),
                        mPaint)
            }
        }

        //绘制未读消息
        canvas.save()
        if (isTipModel) {
            canvas.translate(drawableCx + tipTextLeftOffset, tipTextTopOffset)//移动到中间位置
        } else {
            canvas.translate((rawViewWidth / 2).toFloat() + paddingLeft, tipTextTopOffset)//移动到中间位置
        }
        when {
            noReadNum == 0 -> {
                redDotDrawable.draw(canvas)
            }
            noReadNum in 1..99 -> {
                mPaint.textSize = 9 * density
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
        tipText?.let {
            if ("0".equals(it, true)) {
                //不绘制0
            } else {
                mPaint.textSize = tipTextSize
                mPaint.color = tipTextColor

                val paddingTop = 2 * density
                val paddingLeft = 2 * paddingTop
                if (tipTextBg) {
                    newMessageDrawable.setBounds(0, 0,
                            (mPaint.measureText(it, 0, it.length) + 2 * paddingLeft).toInt(),
                            (mPaint.descent() - mPaint.ascent() + 2 * paddingTop).toInt())
                    newMessageDrawable.draw(canvas)
                }

                //需要绘制文本的宽度
                var needTextWidth = mPaint.textWidth(it)
                //剩余空间的宽度
                var spaceWidth = measuredWidth - (drawableCx + tipTextLeftOffset) - paddingLeft
                while (needTextWidth > spaceWidth) {
                    //如果空间不足, 减少字体大小
                    mPaint.textSize -= 2 * density
                    if (mPaint.textSize < 4 * density) {
                        break
                    }
                    needTextWidth = mPaint.textWidth(it)
                }
                canvas.drawText(it, paddingLeft, paddingTop + mPaint.textHeight(), mPaint)
            }
        }
        canvas.restore()
    }

    fun setDrawableSelectedRes(res: Int) {
        if (res == -1) {
            mDrawableSelected = null
        } else {
            mDrawableSelected = ContextCompat.getDrawable(context, res)
        }
        ensureDrawable()
        postInvalidate()
    }

    fun setDrawableNormalRes(res: Int) {
        if (res == -1) {
            mDrawableNormal = null
        } else {
            mDrawableNormal = ContextCompat.getDrawable(context, res)
        }
        ensureDrawable()
        postInvalidate()
    }
}

private class DrawableDelegate2 : ReadWriteProperty<ImageTipView, Drawable?> {
    private var value: Drawable? = null

    override fun getValue(thisRef: ImageTipView, property: KProperty<*>): Drawable? {
        return value
    }

    override fun setValue(thisRef: ImageTipView, property: KProperty<*>, value: Drawable?) {
        this.value = value
        value?.setBounds(0, 0, value.intrinsicWidth, value.intrinsicHeight)
    }
}

private class DrawableWidthDelegate2 : ReadOnlyProperty<ImageTipView, Int> {

    override fun getValue(thisRef: ImageTipView, property: KProperty<*>): Int {
        if (thisRef.mSelected) {
            if (thisRef.mDrawableSelected == null) {
                return 0
            } else {
                return thisRef.mDrawableSelected!!.bounds.width()
            }
        } else {
            if (thisRef.mDrawableNormal == null) {
                return 0
            } else {
                return thisRef.mDrawableNormal!!.bounds.width()
            }
        }
    }
}

private class DrawableHeightDelegate2 : ReadOnlyProperty<ImageTipView, Int> {

    override fun getValue(thisRef: ImageTipView, property: KProperty<*>): Int {
        if (thisRef.mSelected) {
            if (thisRef.mDrawableSelected == null) {
                return 0
            } else {
                return thisRef.mDrawableSelected!!.bounds.height()
            }
        } else {
            if (thisRef.mDrawableNormal == null) {
                return 0
            } else {
                return thisRef.mDrawableNormal!!.bounds.height()
            }
        }
    }
}

private class ColorSetDelegate2(var isSelectorColor: Boolean = false) : ReadWriteProperty<ImageTipView, Int> {
    private var value: Int? = null

    init {
        if (isSelectorColor) {
            value = Color.parseColor("#3DB8FF")
        } else {
            value = Color.GRAY
        }
    }

    override fun getValue(thisRef: ImageTipView, property: KProperty<*>): Int {
        return value!!
    }

    override fun setValue(thisRef: ImageTipView, property: KProperty<*>, value: Int) {
        if (isSelectorColor == thisRef.mSelected) {
            thisRef.mPaint.color = value
        }
        this.value = value
    }
}