package com.angcyo.uiview.recycler.adapter

import android.content.Context

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：实现Item Layout, Type , Bind 分发
 * 创建人员：Robi
 * 创建时间：2018/03/16 14:01
 * 修改人员：Robi
 * 修改时间：2018/03/16 14:01
 * 修改备注：
 * Version: 1.0.0
 */
class RExItemAdapter<T> : RExBaseAdapter<String, T, String> {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, datas: List<T>?) : super(context, datas)

    init {

    }
}