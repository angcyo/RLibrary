package com.angcyo.uiview.recycler.adapter

import android.util.SparseArray
import android.util.SparseIntArray
import com.angcyo.uiview.utils.Reflect


/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来建立Item 数据, 类型, 处理器之间的联系
 * 创建人员：Robi
 * 创建时间：2018/03/16 16:44
 * 修改人员：Robi
 * 修改时间：2018/03/16 16:44
 * 修改备注：
 * Version: 1.0.0
 */
abstract class RExItemFactory<ItemType, DataType> {

    //protected val allRegItems = ArrayMap<ItemType, ItemHolder>()
    /**所有注册的Item列表, 数据类型-数据布局-数据处理类*/
    protected val allRegItems = ArrayList<RExItem<ItemType, DataType>>()
    /**注册类型, 对应Adapter的item类型, 数据类型-Item布局类型*/
    protected val allItemTypes = SparseArray<ItemType>()
    /**Adapter的item类型, 对应的item处理类Holder, 数据处理-Item布局类型*/
    protected val allItemHolder = SparseArray<RExItemHolder<DataType>>()
    /**Adapter的item类型, 对应的item 的布局id, 数据布局-Item布局类型*/
    protected val allItemLayout = SparseIntArray()

    init {
        registerItems(allRegItems)
        allRegItems.mapIndexed { index, rExItem ->
            allItemTypes.put(index, rExItem.itemType)
            allItemLayout.put(index, rExItem.layoutId)

            val itemHolder = Reflect.newObject<RExItemHolder<DataType>>(rExItem.itemHolder)
            allItemHolder.put(index, itemHolder)
            onCreateItemHolder(itemHolder)
        }
    }

    open fun onCreateItemHolder(itemHolder: RExItemHolder<DataType>) {
        //在此可以初始化一些itemHolder成员变量
    }

    /**返回在Adapter中的item type*/
    fun getItemType(data: DataType): Int = allItemTypes.indexOfValue(getItemTypeFromData(data))

    /**返回Item的布局*/
    fun getItemLayoutId(viewType: Int): Int = allItemLayout[viewType]

    /**返回处理Item的Holder*/
    fun getItemLayoutHolder(viewType: Int): RExItemHolder<DataType> = allItemHolder[viewType]

    /**注册Items*/
    abstract fun registerItems(allRegItems: ArrayList<RExItem<ItemType, DataType>>)

    /**根据数据, 返回数据对应的ItemType*/
    abstract fun getItemTypeFromData(data: DataType): ItemType
}

data class RExItem<out ItemType, DataType>(
        val itemType: ItemType, val layoutId: Int, val itemHolder: Class<out RExItemHolder<DataType>>)

