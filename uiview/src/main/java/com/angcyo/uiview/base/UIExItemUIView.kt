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

            override fun onItemFactoryInit() {
                super.onItemFactoryInit()
                post {
                    noSupportTypeItem?.let {
                        it.itemHolderObj?.let {
                            it.iLayout = mParentILayout
                            it.exItemUIView = this@UIExItemUIView
                            it.exItemAdapter = mExBaseAdapter as RExItemAdapter<*, DataType>
                        }
                    }
                }
                this@UIExItemUIView.onItemFactoryInit()
            }

            override fun onCreateItemHolder(itemHolder: RExItemHolder<DataType>) {
                super.onCreateItemHolder(itemHolder)
                itemHolder.iLayout = mParentILayout
                itemHolder.exItemUIView = this@UIExItemUIView

                post {
                    itemHolder.exItemAdapter = mExBaseAdapter as RExItemAdapter<*, DataType>
                }

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

    override fun isShowInViewPager(): Boolean {
        return super.isShowInViewPager()
    }

    open fun onCreateItemHolder(itemHolder: RExItemHolder<DataType>) {

    }

    open fun onItemFactoryInit() {

    }

    abstract fun registerItems(allRegItems: ArrayList<RExItem<ItemType, DataType>>)
    abstract fun getItemTypeFromData(data: DataType): ItemType
}