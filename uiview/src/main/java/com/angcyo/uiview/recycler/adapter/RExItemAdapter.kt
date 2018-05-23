package com.angcyo.uiview.recycler.adapter

import android.content.Context
import com.angcyo.uiview.recycler.RBaseViewHolder

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
open class RExItemAdapter<ItemType, DataType> : RExBaseAdapter<String, DataType, String> {

    var itemFactory: RExItemFactory<ItemType, DataType>

    constructor(context: Context, itemFactory: RExItemFactory<ItemType, DataType>) : super(context) {
        this.itemFactory = itemFactory
    }

    constructor(context: Context, datas: List<DataType>, itemFactory: RExItemFactory<ItemType, DataType>) : super(context, datas) {
        this.itemFactory = itemFactory
    }

    init {

    }

    override fun getItemLayoutId(viewType: Int): Int {
        return itemFactory.getItemLayoutId(viewType)
    }

    override fun getItemType(position: Int): Int {
        return itemFactory.getItemType(mAllDatas[position])
    }

    override fun onBindDataView(holder: RBaseViewHolder, posInData: Int, dataBean: DataType?) {
        super.onBindDataView(holder, posInData, dataBean)
        val itemHolder = itemFactory.getItemLayoutHolder(holder.itemViewType)
        itemHolder.onBindItemDataView(holder, posInData, dataBean)
    }

    open fun getItemHolderByPosition(position: Int): RExItemHolder<DataType> {
        return itemFactory.getItemLayoutHolder(getItemType(position))
    }
}