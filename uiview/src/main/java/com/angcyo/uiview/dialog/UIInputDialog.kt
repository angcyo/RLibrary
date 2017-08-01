package com.angcyo.uiview.dialog

import android.graphics.Color
import android.support.design.widget.TextInputLayout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIIDialogImpl
import com.angcyo.uiview.widget.Button
import com.angcyo.uiview.widget.ExEditText
import com.angcyo.uiview.widget.TitleBarLayout

/**
 * 简单的文本输入对话框
 * Created by angcyo on 2017-08-01.
 */
open class UIInputDialog : UIIDialogImpl() {

    override fun getGravity(): Int {
        return Gravity.TOP
    }

    override fun inflateDialogView(dialogRootLayout: FrameLayout?, inflater: LayoutInflater?): View {
        //用来过滤状态栏的根布局
        val titleBarLayout = TitleBarLayout(mActivity)
        titleBarLayout.layoutParams = ViewGroup.LayoutParams(-1, -2)
        titleBarLayout.setEnablePadding(getGravity() == Gravity.TOP)
        titleBarLayout.setBackgroundColor(Color.WHITE)

        //内容根布局
        val linearLayout = LinearLayout(mActivity)
        linearLayout.orientation = LinearLayout.VERTICAL
        val offset = getDimensionPixelOffset(R.dimen.base_xhdpi)
        linearLayout.setPadding(offset, offset, offset, offset)

        //输入视图
        val textInputLayout = TextInputLayout(mActivity)
        val editText = ExEditText(mActivity)

        //确定按钮
        val okButton = Button(mActivity)

        //初始化视图数据
        editText.hint = "测试文本"
        okButton.text = "确定"

        //add view
        textInputLayout.addView(editText, LinearLayout.LayoutParams(-1, -2))

        linearLayout.addView(textInputLayout, ViewGroup.LayoutParams(-1, -2))
        linearLayout.addView(okButton, ViewGroup.LayoutParams(-1, -2))

        titleBarLayout.addView(linearLayout, ViewGroup.LayoutParams(-1, -2))

        dialogRootLayout?.let {
            it.addView(titleBarLayout, ViewGroup.LayoutParams(-1, -2))
        }

        return titleBarLayout
    }

}
