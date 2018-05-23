package com.angcyo.uiview.base

import android.graphics.Canvas
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.text.TextPaint
import android.view.View
import com.angcyo.uiview.recycler.RExItemDecoration
import com.angcyo.uiview.recycler.adapter.*

/**
 * Created by angcyo on 2018/03/23 23:01
 */

abstract class UIExItemUIView<ItemType, DataType> : UIRecyclerUIView<String, DataType, String>() {

    var exItemAdapter: RExItemAdapter<ItemType, DataType>? = null

    override fun createItemDecoration(): RecyclerView.ItemDecoration {
        return RExItemDecoration().apply {
            setItemDecorationCallback(object : RExItemDecoration.SingleItemCallback() {
                override fun getItemOffsets2(outRect: Rect, position: Int, edge: Int) {
                    //super.getItemOffsets2(outRect, position, edge)
                    exItemAdapter?.let {
                        it.getItemHolderByPosition(position).getItemOffsets(this@apply, outRect, position, edge)
                    }
                }

                override fun draw(canvas: Canvas, paint: TextPaint, itemView: View, offsetRect: Rect, itemCount: Int, position: Int) {
                    //super.draw(canvas, paint, itemView, offsetRect, itemCount, position)
                    exItemAdapter?.let {
                        it.getItemHolderByPosition(position).draw(this@apply, canvas, paint, itemView, offsetRect, itemCount, position)
                    }
                }
            })
        }
    }

    override fun createAdapter(): RExBaseAdapter<String, DataType, String> {
        exItemAdapter = RExItemAdapter(mActivity, createItemFactory())
        return exItemAdapter!!
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

    /**根据itemType, 返回有多少条相同类型的数据*/
    fun getDataTypeCountByItemType(itemType: ItemType): Int {
        var count = 0
        exItemAdapter?.let {
            for (itemData in it.allDatas) {
                if (itemType == getItemTypeFromData(itemData)) {
                    count++
                }
            }
        }
        return count
    }

    open fun onCreateItemHolder(itemHolder: RExItemHolder<DataType>) {

    }

    open fun onItemFactoryInit() {

    }

    abstract fun registerItems(allRegItems: ArrayList<RExItem<ItemType, DataType>>)
    abstract fun getItemTypeFromData(data: DataType): ItemType
}