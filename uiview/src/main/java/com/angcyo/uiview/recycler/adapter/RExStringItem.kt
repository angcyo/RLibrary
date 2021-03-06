package com.angcyo.uiview.recycler.adapter

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/07/24 14:18
 * 修改人员：Robi
 * 修改时间：2018/07/24 14:18
 * 修改备注：
 * Version: 1.0.0
 */
open class RExStringItem : RExItem<String, IExStringDataType> {
    constructor(itemType: String, layoutId: Int, itemHolder: Class<out RExItemHolder<IExStringDataType>>) : super(itemType, layoutId, itemHolder)
    constructor(itemType: String, layoutId: Int, itemHolderObj: RExItemHolder<IExStringDataType>) : super(itemType, layoutId, itemHolderObj)
}