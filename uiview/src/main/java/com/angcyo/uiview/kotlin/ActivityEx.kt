package com.angcyo.uiview.kotlin

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/07/17 08:47
 * 修改人员：Robi
 * 修改时间：2018/07/17 08:47
 * 修改备注：
 * Version: 1.0.0
 */
/**
 * @see com.angcyo.uiview.view.UIIViewImpl.lightStatusBar
 */
public fun Activity.lightStatusBar(light: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val systemUiVisibility = this.window.decorView.systemUiVisibility
        if (light) {
            if (systemUiVisibility.have(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                return
            }
            this.window
                    .decorView.systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            if (!systemUiVisibility.have(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                return
            }
            this.window
                    .decorView.systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}

/**
 * 激活布局全屏, View 可以布局在 StatusBar 下面
 */
public fun Activity.enableLayoutFullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}