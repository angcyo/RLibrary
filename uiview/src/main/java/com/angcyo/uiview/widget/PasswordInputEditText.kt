package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.AppCompatEditText
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ContextMenu
import android.view.inputmethod.EditorInfo
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/04 16:52
 * 修改人员：Robi
 * 修改时间：2017/07/04 16:52
 * 修改备注：
 * Version: 1.0.0
 */
class PasswordInputEditText(context: Context, attributeSet: AttributeSet? = null)
    : AppCompatEditText(context, attributeSet) {

    /**需要输入密码的数量*/
    var passwordCount = 4

    /**密码与密码之间的间隙*/
    var passwordSpace: Float = 10 * density

    /**密码提示框的大小, 当空隙为0时, 自动层叠边框*/
    var passwordSize: Float = 30 * density

    var strokeWidth = 2 * density

    var passwordHighlightColor = Color.RED
    var passwordBgColor = Color.TRANSPARENT
    var passwordColor = Color.GRAY
    var passwordBorderColor = Color.GRAY

    private val paint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.strokeWidth = strokeWidth
        p
    }

    private val rect: Rect by lazy {
        Rect()
    }

    private val highlightRect: Rect by lazy {
        Rect()
    }

    init {
        val array = context.obtainStyledAttributes(attributeSet, R.styleable.PasswordInputEditText)
        passwordCount = array.getInt(R.styleable.PasswordInputEditText_r_password_count, passwordCount)
        passwordSpace = array.getDimensionPixelOffset(R.styleable.PasswordInputEditText_r_password_space, passwordSpace.toInt()).toFloat()
        passwordSize = array.getDimensionPixelOffset(R.styleable.PasswordInputEditText_r_password_size, passwordSize.toInt()).toFloat()
        strokeWidth = array.getDimensionPixelOffset(R.styleable.PasswordInputEditText_r_border_width, strokeWidth.toInt()).toFloat()

        passwordHighlightColor = array.getColor(R.styleable.PasswordInputEditText_r_password_highlight_color, passwordHighlightColor)
        passwordBgColor = array.getColor(R.styleable.PasswordInputEditText_r_password_bg_color, passwordBgColor)
        passwordColor = array.getColor(R.styleable.PasswordInputEditText_r_password_color, passwordColor)
        passwordBorderColor = array.getColor(R.styleable.PasswordInputEditText_r_password_border_color, passwordBorderColor)

        array.recycle()

        setBackgroundColor(Color.TRANSPARENT)
        setTextColor(Color.TRANSPARENT)
        isCursorVisible = false
        setTextSize(TypedValue.COMPLEX_UNIT_PX, 0f)
        setTextIsSelectable(false)
        inputType = EditorInfo.TYPE_CLASS_NUMBER
        filters = arrayOf(InputFilter.LengthFilter(passwordCount))
        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        keyListener = DigitsKeyListener.getInstance("1234567890")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY) {
            if (passwordSpace == 0f) {
                widthSize = (passwordCount * passwordSize + strokeWidth +
                        paddingLeft + paddingRight).toInt()
            } else {
                widthSize = (passwordCount * (passwordSize + strokeWidth) +
                        Math.max(passwordCount - 1, 0) * passwordSpace +
                        paddingLeft + paddingRight).toInt()
            }
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = (passwordSize + strokeWidth + paddingTop + paddingBottom).toInt()
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun createContextMenu(menu: ContextMenu?) {
        //super.createContextMenu(menu)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        if (text?.length == passwordCount && passwordCount != 0) {
            //T_.show("本次密码:$text")
        }
    }

    override fun isFocused(): Boolean {
        if (isInEditMode) {
            return true
        }
        return super.isFocused()
    }

    override fun onDraw(canvas: Canvas) {
        //canvas.drawColor(Color.parseColor("#40000000"))
        paint.strokeWidth = strokeWidth
        highlightRect.setEmpty()

        var left: Float
        for (i in 0..passwordCount - 1) {
            if (passwordSpace == 0f) {
                left = paddingLeft + passwordSize * i + strokeWidth / 2
            } else {
                left = paddingLeft + strokeWidth / 2 + (passwordSize + strokeWidth) * i + Math.max(i, 0) * passwordSpace
            }

            rect.set(left.toInt(), (paddingTop + strokeWidth / 2).toInt(),
                    (left + passwordSize).toInt(), (paddingTop + strokeWidth / 2 + passwordSize).toInt())

            //填充密码框
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.color = passwordBgColor
            canvas.drawRect(rect, paint)

            //绘制外框
            paint.style = Paint.Style.STROKE
            if (isFocused && text.length == i) {
                //高亮颜色
                paint.color = passwordHighlightColor

                highlightRect.set(rect)
            } else {
                paint.color = passwordBorderColor
            }
            canvas.drawRect(rect, paint)

            //绘制内框
            if (text.length > i) {
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.color = passwordColor
                canvas.drawCircle(rect.centerX().toFloat(), rect.centerY().toFloat(), passwordSize * 0.6f / 2, paint)
            }
        }

        //高亮处理
        if (passwordSpace == 0f) {
            paint.color = passwordHighlightColor
            canvas.drawRect(highlightRect, paint)
        }
    }

    /**是否输入有误*/
    fun isInputError(): Boolean {
        if (text.isNullOrEmpty()) {
            return true
        }
        if (text.length != passwordCount) {
            return true
        }

        return false
    }

    fun string(): String {
        val rawText = text.toString().trim()
        return rawText
    }
}