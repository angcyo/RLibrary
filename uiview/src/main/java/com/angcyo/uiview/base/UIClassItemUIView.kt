package com.angcyo.uiview.base

import com.angcyo.uiview.recycler.adapter.RExBaseAdapter
import com.angcyo.uiview.recycler.adapter.RExItem
import com.angcyo.uiview.recycler.adapter.RExItemAdapter
import com.angcyo.uiview.recycler.adapter.RExItemFactory

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用Class当做Item的type, 来实现Item布局
 * 创建人员：Robi
 * 创建时间：2018/03/21 14:42
 * 修改人员：Robi
 * 修改时间：2018/03/21 14:42
 * 修改备注：
 * Version: 1.0.0
 */

abstract class UIClassItemUIView<T> : UIRecyclerUIView<String, T, String>() {
    override fun createAdapter(): RExBaseAdapter<String, T, String> {
        return RExItemAdapter(mActivity, createItemFactory())
    }

    private fun createItemFactory(): RExItemFactory<Class<*>, T> {
        return object : RExItemFactory<Class<*>, T>() {
            override fun registerItems(allRegItems: ArrayList<RExItem<Class<*>, T>>) {
                this@UIClassItemUIView.registerItems(allRegItems)
            }

            override fun getItemTypeFromData(data: T): Class<*> {
                return this@UIClassItemUIView.getItemTypeFromData(data)
            }
        }
    }

    abstract fun registerItems(allRegItems: ArrayList<RExItem<Class<*>, T>>)
    abstract fun getItemTypeFromData(data: T): Class<*>

}