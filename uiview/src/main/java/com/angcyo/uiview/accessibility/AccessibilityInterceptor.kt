package com.angcyo.uiview.accessibility

import android.view.accessibility.AccessibilityEvent

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/01/26 08:57
 * 修改人员：Robi
 * 修改时间：2018/01/26 08:57
 * 修改备注：
 * Version: 1.0.0
 */
abstract class AccessibilityInterceptor {
    /**需要收到那个程序的事件*/
    var filterPackageName = ""

    /**当到达目标之后的回调*/
    var onJumpToTarget: (() -> Unit)? = null

    open fun onAccessibilityEvent(accService: BaseAccessibilityService, event: AccessibilityEvent) {

    }
}