package com.angcyo.uiview.recycler.adapter

import android.content.Context

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/07/24 14:06
 * 修改人员：Robi
 * 修改时间：2018/07/24 14:06
 * 修改备注：
 * Version: 1.0.0
 */
open class RExStringAdapter : RExItemAdapter<String, IExStringDataType> {
    constructor(context: Context, itemFactory: RExStringItemFactory) : super(context, itemFactory)
    constructor(context: Context, datas: List<IExStringDataType>, itemFactory: RExStringItemFactory) : super(context, datas, itemFactory)
}