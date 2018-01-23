package com.angcyo.uiview.view

import android.view.LayoutInflater
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.container.UITitleBarContainer
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.recycler.RBaseViewHolder

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

    open fun onViewUnloadDelay(uiview: IView) {

    }

    open fun onViewLoad(uiview: IView) {

    }

    open fun onViewShow(uiview: IView) {

    }

    open fun onViewLoadData(page: String?) {

    }

    open fun onViewLoadDataSuccess() {

    }

    /**当开始显示TitleBar时, 会回调此方法*/
    open fun onCreateTitleBar(titleBarPattern: TitleBarPattern) {

    }

    /**当TitleBar创建结束后时, 会回调此方法*/
    open fun onCreateTitleBarEnd(titleBarPattern: TitleBarPattern, uiTitleBarContainer: UITitleBarContainer) {

    }

    open fun inflateContentLayout(uiview: UIIViewImpl, baseContentLayout: ContentLayout?, inflater: LayoutInflater?) {

    }

    open fun initOnShowContentLayout(uiview: UIIViewImpl, viewHolder: RBaseViewHolder) {

    }
}