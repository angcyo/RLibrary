package com.angcyo.uiview.widget.group

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.widget.RTextView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：游戏倒计时提示类
 * 创建人员：Robi
 * 创建时间：2017/12/18 11:30
 * 修改人员：Robi
 * 修改时间：2017/12/18 11:30
 * 修改备注：
 * Version: 1.0.0
 */
class GameTipView(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    var tipTextView: RTextView
    var timeTextView: RTextView

    init {
        View.inflate(context, R.layout.base_game_tip_layout, this)
        tipTextView = findViewById(R.id.tip_text_view)
        timeTextView = findViewById(R.id.time_text_view)
    }

    fun setTipText(text: String) {
        tipTextView.text = text
    }

    fun setTimeText(text: String) {
        timeTextView.text = text
    }

    /**开始倒计时*/
    fun startCountDown(fromTime: Int /*从多少秒开始倒计时*/) {
        //tipTextView.text = text
    }
}