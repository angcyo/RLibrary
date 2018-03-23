package com.angcyo.uiview.dialog

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来显示引导图的对话框
 * 创建人员：Robi
 * 创建时间：2018/01/11 10:01
 * 修改人员：Robi
 * 修改时间：2018/01/11 10:01
 * 修改备注：
 * Version: 1.0.0
 */
open class UIGuideDialog : UIGuideDialogImpl {

    private var layoutId: Int = 0

    constructor(layoutId: Int, anchorViewList: List<View>) : super() {
        this.layoutId = layoutId
        val list = mutableListOf<Rect>()
        for (view in anchorViewList) {
            val rect = Rect()
            view.getGlobalVisibleRect(rect)
            list.add(rect)

            if (view.measuredWidth == 0 || view.measuredHeight == 0) {
                view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        view.getGlobalVisibleRect(rect)
                        view.viewTreeObserver.removeOnPreDrawListener(this)
                        getGuideFrameLayout()?.requestLayout()
                        return true
                    }
                })
            }
        }
        anchorRectList.addAll(list)
    }

    constructor(anchorRectList: List<Rect>, layoutId: Int) : super() {
        this.layoutId = layoutId
        this.anchorRectList.addAll(anchorRectList)
    }

    constructor(layoutId: Int) : super() {
        this.layoutId = layoutId
    }

    override fun inflateDialogView(dialogRootLayout: FrameLayout, inflater: LayoutInflater): View {
        return inflate(layoutId)
    }
}