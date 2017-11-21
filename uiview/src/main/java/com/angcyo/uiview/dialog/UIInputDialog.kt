package com.angcyo.uiview.dialog

import android.support.design.widget.TextInputLayout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIDialogConfig
import com.angcyo.uiview.base.UIIDialogImpl
import com.angcyo.uiview.widget.Button
import com.angcyo.uiview.widget.ExEditText
import com.angcyo.uiview.widget.TitleBarLayout

/**
 * 简单的文本输入对话框
 * Created by angcyo on 2017-08-01.
 */
open class UIInputDialog : UIIDialogImpl {

    var dialogConfig: UIInputDialogConfig? = null

    private var editText: ExEditText? = null

    constructor() : super() {
        setGravity(Gravity.TOP)
    }

    override fun inflateDialogView(dialogRootLayout: FrameLayout?, inflater: LayoutInflater?): View {
        val view: View? = dialogConfig?.onInflateDialogView(dialogRootLayout, inflater)
        if (view == null) {
            //用来过滤状态栏的根布局
            val titleBarLayout = TitleBarLayout(mActivity)
            titleBarLayout.layoutParams = ViewGroup.LayoutParams(-1, -2)
            titleBarLayout.setEnablePadding(getGravity() == Gravity.TOP)
            titleBarLayout.setBackgroundColor(getColor(R.color.base_chat_bg_color))
            titleBarLayout.setMaxHeight(-2)

            //内容根布局
            val linearLayout = LinearLayout(mActivity)
            linearLayout.orientation = LinearLayout.VERTICAL
            val offset = getDimensionPixelOffset(R.dimen.base_xhdpi)
            linearLayout.setPadding(offset, offset, offset, offset)

            //输入视图
            val textInputLayout = TextInputLayout(mActivity)
            editText = ExEditText(mActivity)

            //确定按钮
            val okButton = Button(mActivity)

            //初始化视图数据
            editText?.hint = "请输入..."
            okButton.text = "确定"

            dialogConfig?.onInitInputDialog(this, titleBarLayout, textInputLayout, editText!!, okButton)

            //add view
            textInputLayout.addView(editText, LinearLayout.LayoutParams(-1, -2))

            linearLayout.addView(textInputLayout, LinearLayout.LayoutParams(-1, 0, 1f))
            linearLayout.addView(okButton, ViewGroup.LayoutParams(-1, -2))

            titleBarLayout.addView(linearLayout, ViewGroup.LayoutParams(-1, -2))

            dialogRootLayout?.let {
                it.addView(titleBarLayout, ViewGroup.LayoutParams(-1, -2))
            }

            return titleBarLayout
        } else {
            return view
        }
    }

    override fun initDialogContentView() {
        super.initDialogContentView()
        dialogConfig?.autoShowSoftInput()?.let { b ->
            editText?.let {
                if (b) {
                    showSoftInput(it)
                }
            }
        }
    }

    abstract class UIInputDialogConfig : UIDialogConfig() {

        /*自动显示软键盘*/
        open fun autoShowSoftInput(): Boolean = true

        open fun onInitInputDialog(inputDialog: UIInputDialog,
                                   titleBarLayout: TitleBarLayout,
                                   textInputLayout: TextInputLayout,
                                   editText: ExEditText,
                                   okButton: Button) {

        }

    }
}

