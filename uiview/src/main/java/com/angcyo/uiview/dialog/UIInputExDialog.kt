package com.angcyo.uiview.dialog

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIIDialogImpl
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.utils.UI
import com.angcyo.uiview.widget.ExEditText
import com.angcyo.uiview.widget.viewpager.TextIndicator

/**
 * 高级自定义的文本输入对话框
 * Created by angcyo on 2018-01-27.
 */
open class UIInputExDialog : UIIDialogImpl {

    /**自动显示键盘*/
    var autoShowSoftInput = true

    constructor() : super() {
        /*兼容输入法, 所以不采用此方法控制重心*/
        setGravity(Gravity.NO_GRAVITY)
    }

    open val editText: ExEditText?
        get() {
            return mViewHolder.v(R.id.base_edit_text_view)
        }

    /**标题显示字符串*/
    var inputTitleString = ""
    /**提示字符串*/
    var inputTipString = ""
    /**Hint字符串*/
    var inputHintString = "请输入内容..."
    /**输入框默认字符串*/
    var inputDefaultString = ""

    var okButtonTextString = "确定"

    /**允许输入最大字符串长度, -1不限制*/
    var maxInputLength = -1

    /**是否是单行输入*/
    var isSingleLine = true

    /**文本返回回调*/
    var onInputTextResult: ((String) -> Unit)? = null

    /**初始化回调方法*/
    var onInitLayoutCallback: ((RBaseViewHolder) -> Unit)? = null

    /**拦截OK按钮事件, 返回true表示可以关闭界面*/
    var onOkButtonClick: ((UIInputExDialog, RBaseViewHolder, String) -> Boolean)? = null

    /**重写此方法, 定制布局*/
    override fun inflateDialogView(dialogRootLayout: FrameLayout, inflater: LayoutInflater): View {
        return inflate(R.layout.base_dialog_input_layout)
    }

    override fun initDialogContentView() {
        super.initDialogContentView()
        onInitLayout()
        onInitLayoutCallback?.invoke(`$`)
    }

    /**基础控制初始化*/
    open fun onInitLayout() {
        if (canCanceledOnOutside) {
            click(R.id.base_input_dialog_root_layout) {
                finishDialog()
            }
        }

        /*输入框*/
        editText?.let {

            it.hint = inputHintString
            post {
                setInputText(inputDefaultString)
            }

            if (autoShowSoftInput) {
                post {
                    showSoftInput(it)
                }
            }

            if (isSingleLine) {
                it.gravity = Gravity.CENTER_VERTICAL
                it.setSingleLine(true)
                it.maxLines = 1
            } else {
                it.gravity = Gravity.TOP
                it.setSingleLine(false)
                it.maxLines = 1000
                UI.setViewHeight(it, (80 * density()).toInt())
            }

            if (maxInputLength > 0) {
                it.setMaxLength(maxInputLength)

                v<TextIndicator>(R.id.base_single_text_indicator_view).apply {
                    visibility = View.VISIBLE
                    initIndicator(maxInputLength, it)
                }
            }
        }

        /*标题*/
        if (inputTitleString.isNotEmpty()) {
            mViewHolder.tv(R.id.base_input_title_view).apply {
                visibility = View.VISIBLE
                text = inputTitleString
            }
        }

        /*提示*/
        if (inputTipString.isNotEmpty()) {
            mViewHolder.tv(R.id.base_input_tip_view).apply {
                visibility = View.VISIBLE
                text = inputTipString
            }
        }

        /*确定按钮*/
        mViewHolder.tv(R.id.base_dialog_ok_view).apply {
            text = okButtonTextString
            click(this) {
                if (onOkButtonClick == null) {
                    finishDialog {
                        onInputTextResult?.invoke(editText?.string() ?: "")
                    }
                } else {
                    if (onOkButtonClick!!.invoke(this@UIInputExDialog, `$`, editText?.string()
                                    ?: "")) {
                        //验证通过了
                        finishDialog {
                            onInputTextResult?.invoke(editText?.string() ?: "")
                        }
                    } else {
                        editText?.error()
                    }
                }
            }
        }
    }

    fun setInputText(string: String) {
        /*输入框*/
        editText?.let {
            it.setInputText(string)
        }
    }

    override fun needHideSoftInputForStart(): Boolean {
        return !autoShowSoftInput
    }
}

