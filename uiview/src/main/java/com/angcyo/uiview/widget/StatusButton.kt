package com.angcyo.uiview.widget

import android.content.Context
import android.util.AttributeSet
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.getColor
import com.angcyo.uiview.kotlin.getDimensionPixelOffset
import com.angcyo.uiview.resources.ResUtil
import com.angcyo.uiview.skin.SkinHelper

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/09/12 17:16
 * 修改人员：Robi
 * 修改时间：2017/09/12 17:16
 * 修改备注：
 * Version: 1.0.0
 */
open class StatusButton(context: Context, attributeSet: AttributeSet? = null) : Button(context, attributeSet) {

    companion object {
        /**正常状态, 灰色背景, 黑色字体*/
        val STATUS_NORMAL = 1
        /**主体颜色字体, 白色字体*/
        val STATUS_PRESS = 2
        val STATUS_CHECK = 3
        val STATUS_ENABLE = 4
        /**消极*/
        val STATUS_DISABLE = 5
    }

    var buttonStatus = STATUS_NORMAL
        set(value) {
            field = value
            refreshButton()
        }

    init {

    }

    override fun initView() {
        super.initView()
        isClickable = false
        buttonStatus = STATUS_NORMAL
    }

    private fun refreshButton() {
        when (buttonStatus) {
            STATUS_NORMAL -> {
                setTextColor(getColor(R.color.base_text_color))
                background = ResUtil.createDrawable(getColor(R.color.default_base_line),
                        getDimensionPixelOffset(R.dimen.base_round_little_radius).toFloat())
            }
            STATUS_DISABLE -> {
                setTextColor(getColor(R.color.base_white))
                background = ResUtil.createDrawable(getColor(R.color.base_text_color_dark),
                        getDimensionPixelOffset(R.dimen.base_round_little_radius).toFloat())
            }
            STATUS_PRESS, STATUS_CHECK, STATUS_ENABLE -> {
                setTextColor(getColor(R.color.base_white))
                background = ResUtil.createDrawable(SkinHelper.getSkin().themeSubColor,
                        getDimensionPixelOffset(R.dimen.base_round_little_radius).toFloat())
            }
        }
    }

}