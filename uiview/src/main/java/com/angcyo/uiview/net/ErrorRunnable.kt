package com.angcyo.uiview.net

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/11/30 10:13
 * 修改人员：Robi
 * 修改时间：2017/11/30 10:13
 * 修改备注：
 * Version: 1.0.0
 */
open class ErrorRunnable : Runnable {
    var code: Int = -1
    override fun run() {
        onErrorRun(code)
    }

    open fun onErrorRun(errorCode: Int) {

    }

}