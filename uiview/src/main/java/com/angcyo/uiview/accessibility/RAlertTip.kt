package com.angcyo.uiview.accessibility

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.annotation.LayoutRes
import android.view.*
import com.angcyo.uiview.RApplication

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来全屏覆盖提示的AlertDialog, 具有Toast Type的窗口
 * 创建人员：Robi
 * 创建时间：2018/01/25 14:00
 * 修改人员：Robi
 * 修改时间：2018/01/25 14:00
 * 修改备注：
 * Version: 1.0.0
 */
class RAlertTip {

    /**是否需要点击事件*/
    var needTouch = false

    var windowWidth = -1
    var windowHeight = -1

    var windowGravity = Gravity.CENTER

    var offsetX = 0
    var offsetY = 0

    fun show(@LayoutRes contentLayoutId: Int, initView: ((View) -> Unit)? = null): Dialog {
        return show(LayoutInflater.from(RApplication.getApp()).inflate(contentLayoutId, null), initView)
    }

    fun show(contentView: View, initView: ((View) -> Unit)? = null): Dialog {
        return createTipDialog(contentView).apply {
            initView?.invoke(contentView)
            this.show()
        }
    }

    fun hide() {
        dialog?.let {
            it.dismiss()
        }
    }

    private var dialog: Dialog? = null
    private fun createTipDialog(contentView: View): Dialog {
//        val alertDialog = AlertDialog.Builder(RApplication.getApp())
//                .setCancelable(false)
//                .create()
//        val window = alertDialog.window
//        window.setType(WindowManager.LayoutParams.TYPE_TOAST)
//        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        alertDialog.setContentView(View(RApplication.getApp()).apply {
//            setBackgroundColor(Color.parseColor("#60008900"))
//            setOnClickListener {
//                Tip.tip(System.currentTimeMillis().toString())
//            }
//        })
////        val attributes = window.attributes
////        attributes.width = -1
////        attributes.height = -1
////        attributes.gravity = Gravity.CENTER
////        attributes.y = 330
//        //        attributes.dimAmount = 0f;
////        window.attributes = attributes
//        window.setLayout(-1, -2)
//        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN) //
//        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
//        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) //窗口内是否可点击
//        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//        alertDialog.setCanceledOnTouchOutside(false)
//
//        //alertDialog.setContentView()
//        //        alertDialog.show();
//        return alertDialog

        return Dialog(RApplication.getApp()).apply {
            dialog = this
            window.let {
                it.requestFeature(Window.FEATURE_NO_TITLE) //去掉标题部分
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.setContentView(contentView)
                it.setLayout(windowWidth, windowHeight)
                it.setGravity(windowGravity)

                it.attributes.let {
                    it.x = offsetX
                    it.y = offsetY
                    window.attributes = it
                }

                /**
                 * 需要权限 Manifest.permission.SYSTEM_ALERT_WINDOW
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                } else {
                    it.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                }
                //it.setType(WindowManager.LayoutParams.TYPE_TOAST) //显示在其他窗口上
                it.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN) //能够显示到状态栏
                if (!needTouch) {
                    it.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) //去掉点击事件
                }

                //下面是啥?
                it.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)  //窗口外可以点击
                it.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                it.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)  //常亮
                it.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
                it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) //背景变暗
            }
            //setContentView()
        }
    }

}