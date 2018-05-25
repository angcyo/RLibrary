package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模仿IOS的搜索输入框
 * 创建人员：Robi
 * 创建时间：2017/09/20 10:17
 * 修改人员：Robi
 * 修改时间：2017/09/20 10:17
 * 修改备注：
 * Version: 1.0.0
 */
class SearchEditText(context: Context, attributeSet: AttributeSet? = null) : ExEditText(context, attributeSet) {

    private var rawPaddingLeft = 0
    var searchDrawable: Drawable? = null

    var searchTipText = ""
    var searchTipTextSize = 14 * density
    var searchTipTextColor = getColor(R.color.base_text_color_dark2)
    /**文本偏移searchDrawable的距离*/
    var searchTipTextLeftOffset = 0f

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SearchEditText)
        val string = typedArray.getString(R.styleable.SearchEditText_r_search_tip_text)
        searchTipText = string ?: "搜索一下"
        typedArray.recycle()
    }

    override fun initView(context: Context, attrs: AttributeSet?) {
        super.initView(context, attrs)

        //默认激活的配置
        setSingleLine(true)
        maxLines = 1
        imeOptions = EditorInfo.IME_ACTION_SEARCH
        //end

        rawPaddingLeft = paddingLeft

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SearchEditText)
        val drawable = typedArray.getDrawable(R.styleable.SearchEditText_r_search_drawable)
        if (drawable == null) {
            if (isInEditMode) {
                searchDrawable = getDrawable(R.drawable.base_search)
            } else {
                searchDrawable = getDrawable(R.drawable.base_search)
            }
        } else {
            searchDrawable = drawable
        }

        searchDrawable?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
        }

        searchTipTextLeftOffset = 4 * density
        searchTipTextSize = typedArray.getDimensionPixelOffset(R.styleable.SearchEditText_r_search_tip_text_size, searchTipTextSize.toInt()).toFloat()
        searchTipTextLeftOffset = typedArray.getDimensionPixelOffset(R.styleable.SearchEditText_r_search_tip_text_left_offset, searchTipTextLeftOffset.toInt()).toFloat()
        searchTipTextColor = typedArray.getColor(R.styleable.SearchEditText_r_search_tip_text_color, searchTipTextColor)

        typedArray.recycle()

        setPadding((rawPaddingLeft + (searchDrawable?.intrinsicWidth
                ?: 0) + searchTipTextLeftOffset).toInt(),
                paddingTop, paddingRight, paddingBottom)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        clearFocus()
    }

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private val drawWidth: Float
        get() {
            paint.textSize = searchTipTextSize
            return (searchDrawable?.intrinsicWidth
                    ?: 0) + searchTipTextLeftOffset + textWidth(paint, searchTipText)
        }

    private val drawHeight: Float
        get() {
            paint.textSize = searchTipTextSize
            return textHeight(paint).minValue(searchDrawable?.intrinsicHeight ?: 0)
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //canvas.drawColor(Color.BLUE)

        var left = 0
        val top = paddingTop.toFloat() + (viewDrawHeight - drawHeight) / 2

        searchDrawable?.let {
            canvas.save()
            if (isFocused || !isEmpty) {
                canvas.translate(rawPaddingLeft.toFloat() + scrollX, top)
                it.draw(canvas)
            } else {
                canvas.translate(rawPaddingLeft + (viewDrawWith - drawWidth) / 2, top)
                it.draw(canvas)
            }
            left = it.intrinsicWidth
            canvas.restore()
        }

        if (!TextUtils.isEmpty(searchTipText) && isEmpty && !isFocused) {
            canvas.save()
            canvas.translate(rawPaddingLeft + (viewDrawWith - drawWidth) / 2, top)

            paint.color = searchTipTextColor
            paint.textSize = searchTipTextSize
            canvas.drawText(searchTipText, left + searchTipTextLeftOffset, -paint.ascent(), paint)
            canvas.restore()
        }
    }

    override fun checkNeedDrawRHintText(): Boolean {
        return super.checkNeedDrawRHintText() && isFocused
    }
}