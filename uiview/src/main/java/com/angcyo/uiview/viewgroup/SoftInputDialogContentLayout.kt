package com.angcyo.uiview.viewgroup

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.angcyo.uiview.kotlin.exactlyMeasure
import java.lang.IllegalStateException

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：在对话框中, 弹出软键盘, 用来控制内容布局的显示. 当键盘没有弹出, 半屏幕显示; 键盘弹出, 全屏显示
 * 创建人员：Robi
 * 创建时间：2017/09/08 15:31
 * 修改人员：Robi
 * 修改时间：2017/09/08 15:31
 * 修改备注：
 * Version: 1.0.0
 */
open class SoftInputDialogContentLayout(context: Context, attributeSet: AttributeSet? = null) :
        ViewGroup(context, attributeSet) {
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount > 0) {
            val view = getChildAt(0)
            view.layout(0, measuredHeight - view.measuredHeight, measuredWidth, measuredHeight)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (childCount > 0) {
            if (isSoftKeyboardShow()) {
                getChildAt(0).measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize - paddingTop))
            } else {
                getChildAt(0).measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize / 2))
            }
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        if (childCount > 1) {
            throw IllegalStateException("只允许一个ChildView")
        }
    }

    /**
     * 判断键盘是否显示
     */
    fun isSoftKeyboardShow(): Boolean {
        val screenHeight = getScreenHeightPixels()
        val keyboardHeight = getSoftKeyboardHeight()
        return screenHeight != keyboardHeight && keyboardHeight > 100
    }

    /**
     * 获取键盘的高度
     */
    fun getSoftKeyboardHeight(): Int {
        val screenHeight = getScreenHeightPixels()
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
        val visibleBottom = rect.bottom
        return screenHeight - visibleBottom
    }

    /**
     * 屏幕高度(不包含虚拟导航键盘的高度)
     */
    private fun getScreenHeightPixels(): Int {
        return resources.displayMetrics.heightPixels
    }

}