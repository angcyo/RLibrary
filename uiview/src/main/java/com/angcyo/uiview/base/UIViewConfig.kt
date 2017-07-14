package com.angcyo.uiview.base

import android.view.LayoutInflater
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.recycler.RBaseViewHolder

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/14 14:34
 * 修改人员：Robi
 * 修改时间：2017/07/14 14:34
 * 修改备注：
 * Version: 1.0.0
 */
abstract class UIViewConfig {
    open fun inflateContentLayout(uiview: UIBaseView, baseContentLayout: ContentLayout?, inflater: LayoutInflater?) {

    }

    open fun initOnShowContentLayout(uiview: UIBaseView, viewHolder: RBaseViewHolder) {

    }
}