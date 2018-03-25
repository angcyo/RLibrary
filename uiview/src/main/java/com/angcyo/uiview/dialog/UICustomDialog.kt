package com.angcyo.uiview.dialog

import android.support.annotation.LayoutRes
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.angcyo.uiview.base.UIIDialogImpl
import com.angcyo.uiview.container.ILayout
import com.angcyo.uiview.recycler.RBaseViewHolder

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：对话框的壳子
 * 创建人员：Robi
 * 创建时间：2018/02/06 16:06
 * 修改人员：Robi
 * 修改时间：2018/02/06 16:06
 * 修改备注：
 * Version: 1.0.0
 */
open class UICustomDialog : UIIDialogImpl {

    private var layoutId = 0
    private lateinit var initDialog: OnInitDialog

    companion object {
        fun build(@LayoutRes layoutId: Int): UICustomDialog {
            return UICustomDialog(layoutId)
        }
    }

    private constructor(@LayoutRes layoutId: Int) : super() {
        this.layoutId = layoutId
        setGravity(Gravity.CENTER)
    }

    fun show(iLayout: ILayout): UICustomDialog {
        iLayout.startIView(this)
        return this
    }

    fun onInitDialog(initDialog: OnInitDialog): UICustomDialog {
        this.initDialog = initDialog
        return this
    }

    override fun setDimBehind(dimBehind: Boolean): UICustomDialog {
        return super.setDimBehind(dimBehind) as UICustomDialog
    }

    override fun setDimColor(dimColor: Int): UICustomDialog {
        return super.setDimColor(dimColor) as UICustomDialog
    }

    override fun setCanCanceledOnOutside(canCanceledOnOutside: Boolean): UICustomDialog {
        return super.setCanCanceledOnOutside(canCanceledOnOutside) as UICustomDialog
    }

    override fun inflateDialogView(dialogRootLayout: FrameLayout, inflater: LayoutInflater): View {
        val view = inflate(layoutId)
        initDialog.onInitDialog(this, RBaseViewHolder(view))
        return view
    }

    interface OnInitDialog {
        fun onInitDialog(window: UICustomDialog, viewHolder: RBaseViewHolder)
    }
}