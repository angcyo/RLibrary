package com.angcyo.picker.media.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.draw.RDrawNumCheck

/**
 * Created by angcyo on 2018/06/17 11:18
 * 用数字的形式显示选择状态
 */
class NumCheckView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    var drawNumCheck: RDrawNumCheck = RDrawNumCheck(this, attributeSet)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawNumCheck.onDraw(canvas)
    }

    fun isChecked(): Boolean = drawNumCheck.isCheck()

    fun setNum(num: Int) {
        drawNumCheck.drawNum = if (num <= 0) "" else "$num"
    }

}