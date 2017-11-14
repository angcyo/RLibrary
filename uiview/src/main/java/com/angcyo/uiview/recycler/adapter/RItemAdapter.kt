package com.angcyo.uiview.recycler.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.angcyo.uiview.base.Item
import com.angcyo.uiview.recycler.RBaseViewHolder


/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/11/14 15:35
 * 修改人员：Robi
 * 修改时间：2017/11/14 15:35
 * 修改备注：
 * Version: 1.0.0
 */
abstract class RItemAdapter<T : Item> : RExBaseAdapter<String, T, String> {
    constructor(context: Context) : super(context)
    constructor(context: Context, items: List<T>) : super(context, items)

    override fun onBindDataView(holder: RBaseViewHolder, posInData: Int, dataBean: T) {
        dataBean.onBindView(holder, posInData, dataBean)
    }

    override fun getDataItemType(posInData: Int): Int {
        return posInData
    }

    final override fun getItemLayoutId(viewType: Int): Int {
        val t = mAllDatas[viewType]
        val itemLayoutId = t.itemLayoutId

        return if (itemLayoutId == -1) {
            getItemLayoutIdNeed(viewType)
        } else {
            itemLayoutId
        }
    }

    /**如果Item没有指定布局, 那么就回调此方法*/
    open fun getItemLayoutIdNeed(posInData: Int) = -1

    /**当 getItemLayoutIdNeed 返回-1是, 回调此方法用来 new出布局*/
    override fun createItemView(parent: ViewGroup, viewType: Int): View? = null
}