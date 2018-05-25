package com.angcyo.uiview.base

import android.support.annotation.CallSuper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.model.TitleBarItem
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.utils.UI
import com.angcyo.uiview.widget.ExEditText
import com.angcyo.uiview.widget.RTextView
import com.angcyo.uiview.widget.viewpager.TextIndicator

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：快速用来输入文本的界面
 * 创建人员：Robi
 * 创建时间：2017/11/01 14:08
 * 修改人员：Robi
 * 修改时间：2017/11/01 14:08
 * 修改备注：
 * Version: 1.0.0
 */
abstract class UIInputView : UIItemUIView<SingleItem>() {
    override fun createItems(items: MutableList<SingleItem>) {
        items?.add(object : SingleItem() {
            override fun onBindView(holder: RBaseViewHolder, posInData: Int, dataBean: Item?) {
                initItemView(holder)
            }

            override fun getItemLayoutId(): Int {
                return this@UIInputView.getItemLayoutId()
            }

        })
    }

    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar()
                .addRightItem(TitleBarItem(getRightItemString()) {
                    if (onRightItemClick()) {
                        finishIView {
                            onInputTextResult(editView.string())
                        }
                    }
                })
    }

    open protected fun getItemLayoutId(): Int {
        return R.layout.base_item_input_layout
    }

    override fun getTitleString(): String {
        return "请输入"
    }

    open protected fun getRightItemString() = "确定"

    /**右边的按钮点击时, 是否需要处理*/
    open protected fun onRightItemClick() = true

    /**自动弹出键盘*/
    open protected fun isShowSoftInput() = true

    /**输入框 上方的提示文本*/
    open protected fun getTipInputString() = ""

    open protected fun getHintInputString() = "请输入内容"

    /**允许输入的最大长度, -1表示不限制*/
    open protected fun getMaxInputLength() = -1

    /**如果是多行输入, 指定高度就行, 否则就是单行输入并且高度固定*/
    open protected fun getInputViewHeight() = -1

    protected lateinit var tipView: RTextView
    protected lateinit var editView: ExEditText
    protected lateinit var indicatorView: TextIndicator

    @CallSuper
    open protected fun initItemView(holder: RBaseViewHolder) {
        holder.itemView.setPadding(0, (10 * density()).toInt(), 0, 0)

        tipView = holder.v(R.id.base_input_tip_view)
        editView = holder.v(R.id.base_edit_text_view)
        indicatorView = holder.v(R.id.base_single_text_indicator_view)

        if (!TextUtils.isEmpty(getTipInputString())) {
            tipView.visibility = View.VISIBLE
            tipView.text = getTipInputString()
        }

        initEditView()

        if (isShowSoftInput()) {
            post {
                showSoftInput(editView)
            }
        }
    }

    @CallSuper
    open protected fun initEditView() {
        if (getMaxInputLength() >= 0) {
            editView.setMaxLength(getMaxInputLength())
            indicatorView.apply {
                visibility = View.VISIBLE
                initIndicator(getMaxInputLength(), editView)
            }
        }

        if (getInputViewHeight() > 0) {
            UI.setViewHeight(editView, getInputViewHeight())
            editView.gravity = Gravity.TOP
        } else {
            editView.apply {
                gravity = Gravity.CENTER_VERTICAL
                setSingleLine(true)
                maxLines = 1
            }
        }

        editView.hint = getHintInputString()
    }

    /**最终结果返回回调*/
    open protected fun onInputTextResult(text: String) {

    }

}
