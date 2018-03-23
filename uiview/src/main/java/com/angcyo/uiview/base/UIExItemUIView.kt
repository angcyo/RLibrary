package com.angcyo.uiview.base

import com.angcyo.uiview.recycler.adapter.*

/**
 * Created by angcyo on 2018/03/23 23:01
 */

abstract class UIExItemUIView<ItemType, DataType> : UIRecyclerUIView<String, DataType, String>() {
    override fun createAdapter(): RExBaseAdapter<String, DataType, String> {
        return RExItemAdapter(mActivity, createItemFactory())
    }

    private fun createItemFactory(): RExItemFactory<ItemType, DataType> {
        return object : RExItemFactory<ItemType, DataType>() {

            override fun onCreateItemHolder(itemHolder: RExItemHolder<DataType>) {
                super.onCreateItemHolder(itemHolder)
                itemHolder.iLayout = mParentILayout

                this@UIExItemUIView.onCreateItemHolder(itemHolder)
            }

            override fun registerItems(allRegItems: ArrayList<RExItem<ItemType, DataType>>) {
                this@UIExItemUIView.registerItems(allRegItems)
            }

            override fun getItemTypeFromData(data: DataType): ItemType {
                return this@UIExItemUIView.getItemTypeFromData(data)
            }
        }
    }

    open fun onCreateItemHolder(itemHolder: RExItemHolder<DataType>) {

    }

    abstract fun registerItems(allRegItems: ArrayList<RExItem<ItemType, DataType>>)
    abstract fun getItemTypeFromData(data: DataType): ItemType
}