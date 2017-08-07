package com.angcyo.uiview.view

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：界面事件回调
 * 创建人员：Robi
 * 创建时间：2017/08/05 10:06
 * 修改人员：Robi
 * 修改时间：2017/08/05 10:06
 * 修改备注：
 * Version: 1.0.0
 */
abstract class OnUIViewListener {
    open fun onViewUnload(uiview: IView) {

    }

    open fun onViewLoadData(page: String?) {

    }

    open fun onViewLoadDataSuccess() {

    }
}