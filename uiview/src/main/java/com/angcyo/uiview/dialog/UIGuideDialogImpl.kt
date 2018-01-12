package com.angcyo.uiview.dialog

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIIDialogImpl
import com.angcyo.uiview.widget.group.GuideFrameLayout

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
open abstract class UIGuideDialogImpl : UIIDialogImpl() {

    /**锚点矩形列表*/
    protected val anchorRectList = mutableListOf<Rect>()

    var onFinishButtonClick: (() -> Unit)? = null

    init {
        layoutAnim = true
    }

//    init {
//        anchorView.getGlobalVisibleRect(anchorRect)
//        L.e("call: GuideLayoutUIView init -> $anchorRect")
//    }

//    override fun inflateDialogView(dialogRootLayout: FrameLayout?, inflater: LayoutInflater?): View {
//        return inflate(R.layout.dialog_guide_layout)
//    }

    override fun initDialogContentView() {
        super.initDialogContentView()

        val rootLayout: GuideFrameLayout? = getGuideFrameLayout()
        rootLayout?.addAnchorList(anchorRectList)

        getFinishButton()?.let {
            click(it) {
                finishDialog {
                    onFinishButtonClick?.invoke()
                }
            }
        }
    }

    open fun getGuideFrameLayout(): GuideFrameLayout? = v(R.id.base_guide_layout)

    open fun getFinishButton(): View? = v(R.id.base_finish_view)

    override fun onViewShow(bundle: Bundle?) {
        super.onViewShow(bundle)
        interceptTouchEvent(false)
    }

    override fun loadLayoutAnimation(): Animation {
        return super.loadLayoutAnimation()
    }

    override fun isDimBehind(): Boolean {
        return true
    }

    override fun loadStartAnimation(): Animation? {
        return null
    }

    override fun loadFinishAnimation(): Animation? {
        return null
    }

    override fun loadOtherEnterAnimation(): Animation? {
        return null
    }

    override fun loadOtherExitAnimation(): Animation? {
        return null
    }

}