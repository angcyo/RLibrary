package com.angcyo.uiview.viewgroup

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
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
open class RConstraintLayout : ConstraintLayout {

    private var drawLine: RDrawLine

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        drawLine = RDrawLine(this, attrs)
    }

    init {
    }
}