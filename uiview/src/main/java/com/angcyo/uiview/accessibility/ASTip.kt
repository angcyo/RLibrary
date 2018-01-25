package com.angcyo.uiview.accessibility

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.angcyo.uiview.R
import com.angcyo.uiview.RApplication

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模仿360 滑动找到 辅助工具, 并开启的Toast提示
 * 创建人员：Robi
 * 创建时间：2018/01/24 14:46
 * 修改人员：Robi
 * 修改时间：2018/01/24 14:46
 * 修改备注：
 * Version: 1.0.0
 */
object ASTip {
    private var toast: Toast? = null

    fun show() {
        tip("请找到${RApplication.getApp().resources.getString(R.string.base_accessibility_summary)}并开启")
    }

    fun show(tipText: CharSequence, tipImageResId: Int) {
        show(RApplication.getApp(), tipText, tipImageResId)
    }

    fun tip(tipText: CharSequence) {
        show(tipText, R.drawable.base_tip_ico)
    }

    fun ok(tipText: CharSequence) {
        show(tipText, R.drawable.base_tip_ok)
    }

    private fun show(context: Context, tipText: CharSequence, tipImageResId: Int) {
        val layout: View
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_LONG)
            layout = LayoutInflater.from(context).inflate(R.layout.base_accessibility_toast_tip, null)
            (layout.findViewById<View>(R.id.base_toast_text_view) as TextView).text = tipText
            toast!!.view = layout
            toast!!.setGravity(Gravity.END, 0, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toast!!.view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        } else {
            layout = toast!!.view
        }

        val titleView = find<TextView>(layout, R.id.base_toast_text_view)
        val imageView = find<ImageView>(layout, R.id.base_toast_image_view)

        if (titleView != null) {
            titleView.text = tipText
        }
        imageView?.setImageResource(tipImageResId)
        toast!!.show()
    }

    private fun <T> find(view: View, id: Int): T? {
        return view.findViewById<View>(id) as T
    }
}
