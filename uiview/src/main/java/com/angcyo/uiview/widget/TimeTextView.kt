package com.angcyo.uiview.widget

import android.content.Context
import android.util.AttributeSet
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.nowTime
import com.angcyo.uiview.utils.RUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来显示时间的控件, 支持模板自定义
 * 创建人员：Robi
 * 创建时间：2018/01/10 14:30
 * 修改人员：Robi
 * 修改时间：2018/01/10 14:30
 * 修改备注：
 * Version: 1.0.0
 */
open class TimeTextView(context: Context, attributeSet: AttributeSet? = null) : RTextView(context, attributeSet) {

    private val simpleFormat = SimpleDateFormat("yyyy-MM-dd HH:mm" /*ss:SSS*/, Locale.CHINA)

    /**毫秒*/
    var time = 0L
        set(value) {
            field = value
            text = if (field <= 0) {
                ""
            } else {
                if (showShotTime) {
                    RUtils.getShotTimeString(field)
                } else {
                    simpleFormat.format(Date(field))
                }
            }
        }

    /**缩短显示时间*/
    var showShotTime = false

    init {
        val array = context.obtainStyledAttributes(attributeSet, R.styleable.TimeTextView)
        val pattern = array.getString(R.styleable.TimeTextView_r_time_pattern)
        val timeString = array.getString(R.styleable.TimeTextView_r_time)
        showShotTime = array.getBoolean(R.styleable.TimeTextView_r_show_shot_time, showShotTime)
        pattern?.let {
            setPattern(it)
        }
        if (isInEditMode) {
            time = nowTime()
        }
        timeString?.let {
            time = timeString.toLong()
        }
        array.recycle()
    }

    /**重置模板样式*/
    fun setPattern(pattern: String) {
        simpleFormat.applyPattern(pattern)
        time = time
    }
}