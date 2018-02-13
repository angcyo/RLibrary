package com.angcyo.uiview.base

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/08/02 10:07
 * 修改人员：Robi
 * 修改时间：2017/08/02 10:07
 * 修改备注：
 * Version: 1.0.0
 */
abstract class UIDialogConfig {
    open fun onInflateDialogView(dialogRootLayout: FrameLayout?, inflater: LayoutInflater?): View? {
        return null
    }
}