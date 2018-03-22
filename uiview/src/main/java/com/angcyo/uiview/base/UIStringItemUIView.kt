package com.angcyo.uiview.base

import com.angcyo.uiview.recycler.adapter.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用String来当做Item的type, 用来实现items
 * 创建人员：Robi
 * 创建时间：2018/03/21 14:42
 * 修改人员：Robi
 * 修改时间：2018/03/21 14:42
 * 修改备注：
 * Version: 1.0.0
 */

abstract class UIStringItemUIView<T> : UIRecyclerUIView<String, T, String>() {
    override fun createAdapter(): RExBaseAdapter<String, T, String> {
        return RExItemAdapter(mActivity, createItemFactory())
    }

    private fun createItemFactory(): RExItemFactory<String, T> {
        return object : RExItemFactory<String, T>() {

            override fun onCreateItemHolder(itemHolder: RExItemHolder<T>) {
                super.onCreateItemHolder(itemHolder)
                itemHolder.iLayout = mParentILayout
            }

            override fun registerItems(allRegItems: ArrayList<RExItem<String, T>>) {
                this@UIStringItemUIView.registerItems(allRegItems)
            }

            override fun getItemTypeFromData(data: T): String {
                return this@UIStringItemUIView.getItemTypeFromData(data)
            }
        }
    }

    abstract fun registerItems(allRegItems: ArrayList<RExItem<String, T>>)
    abstract fun getItemTypeFromData(data: T): String

}