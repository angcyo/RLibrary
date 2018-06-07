package com.angcyo.uiview.recycler.adapter

import android.util.SparseArray
import android.util.SparseIntArray
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIBaseRxView
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
    var uiview: UIBaseRxView? = null
    var adapter: RExItemAdapter<ItemType, DataType>? = null

    open fun initItemFactory(uiview: UIBaseRxView?, adapter: RExItemAdapter<ItemType, DataType>?) {
        this.uiview = uiview
        this.adapter = adapter

        registerItems(allRegItems)
        allRegItems.mapIndexed { index, rExItem ->
            allItemTypes.put(index, rExItem.itemType)
            allItemLayout.put(index, rExItem.layoutId)

            if (rExItem.itemHolder == null && rExItem.itemHolderObj == null) {
                throw NullPointerException("请检查对象成员")
            }
            val itemHolder = if (rExItem.itemHolder == null) {
                rExItem.itemHolderObj!!
            } else {
                Reflect.newObject<RExItemHolder<DataType>>(rExItem.itemHolder)
            }
            allItemHolder.put(index, itemHolder)
            onCreateItemHolder(itemHolder)
        }
        onItemFactoryInit()
    }

    companion object {
        //未注册的消息类型
        const val NO_SUPPORT_ITEM_TYPE = -404
        const val NO_ITEM_TYPE = -4040
    }

    //protected val allRegItems = ArrayMap<ItemType, ItemHolder>()
    /**所有注册的Item列表, 数据类型-数据布局-数据处理类*/
    protected val allRegItems = ArrayList<RExItem<ItemType, DataType>>()
    /**注册类型, 对应Adapter的item类型, 数据类型-Item布局类型*/
    protected val allItemTypes = SparseArray<ItemType>()
    /**Adapter的item类型, 对应的item处理类Holder, 数据处理-Item布局类型*/
    protected val allItemHolder = SparseArray<RExItemHolder<DataType>>()
    /**Adapter的item类型, 对应的item 的布局id, 数据布局-Item布局类型*/
    protected val allItemLayout = SparseIntArray()


    //不支持的消息类型处理
    var noSupportTypeItem = RExItem(null, R.layout.base_no_support_item_type_layout, RNoSupportItemHolder<DataType>())

    open fun onItemFactoryInit() {
        //初始化结束后
    }

    open fun onCreateItemHolder(itemHolder: RExItemHolder<DataType>) {
        //在此可以初始化一些itemHolder成员变量
        uiview?.let {
            itemHolder.exUIView = it
            itemHolder.iLayout = it.parentILayout
        }
        adapter?.let {
            itemHolder.exItemAdapter = it
        }

        itemHolder.onCreateItemHolderAfter()
    }

    /**返回在Adapter中的item type*/
    fun getItemType(data: DataType?, position: Int): Int {
        //不支持的item类型
        val itemTypeFromData = getItemTypeFromData(data, position) ?: return NO_SUPPORT_ITEM_TYPE

        //获取item type
        val indexOfValue = allItemTypes.indexOfValue(itemTypeFromData)
        if (indexOfValue == -1) {
            if (itemTypeFromData is String) {
                //枚举手动匹配一次
                for (i in 0 until allItemTypes.size()) {
                    val keyAt = allItemTypes.keyAt(i)
                    val value = allItemTypes[keyAt]
                    if (itemTypeFromData == value) {
                        return i
                    }
                }
            }
            return NO_SUPPORT_ITEM_TYPE
        }
        return indexOfValue
    }

    /**返回Item的布局*/
    fun getItemLayoutId(viewType: Int): Int {
        return if (viewType == NO_SUPPORT_ITEM_TYPE) {
            noSupportTypeItem.layoutId
        } else {
            allItemLayout[viewType]
        }
    }

    /**返回处理Item的Holder*/
    fun getItemLayoutHolder(viewType: Int): RExItemHolder<DataType>? {
        return if (viewType == NO_SUPPORT_ITEM_TYPE) {
            noSupportTypeItem.itemHolderObj!!
        } else if (viewType == NO_ITEM_TYPE) {
            return null
        } else {
            allItemHolder[viewType]
        }
    }

    /**注册Items*/
    abstract fun registerItems(allRegItems: ArrayList<RExItem<ItemType, DataType>>)

    /**根据数据, 返回数据对应的ItemType
     *
     * @see getItemTypeFromData(DataType, Int)
     * */
    @Deprecated("2018-5-25")
    open fun getItemTypeFromData(data: DataType?): ItemType? {
        return null
    }

    open fun getItemTypeFromData(data: DataType?, position: Int): ItemType? {
        return getItemTypeFromData(data)
    }
}

class RExItem<out ItemType, DataType> {
    val itemType: ItemType
    val layoutId: Int

    var itemHolder: Class<out RExItemHolder<DataType>>? = null

    /**可以用已经存在的ItemHolder处理, 否则会根据itemHolder对应的类, 自动实例化一个对象*/
    var itemHolderObj: RExItemHolder<DataType>? = null

    constructor(itemType: ItemType, layoutId: Int, itemHolder: Class<out RExItemHolder<DataType>>) {
        this.itemType = itemType
        this.layoutId = layoutId
        this.itemHolder = itemHolder
    }

    constructor(itemType: ItemType, layoutId: Int, itemHolderObj: RExItemHolder<DataType>) {
        this.itemType = itemType
        this.layoutId = layoutId
        this.itemHolderObj = itemHolderObj
    }
}

