package com.angcyo.uiview.base

import android.graphics.Canvas
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.text.TextPaint
import android.view.MotionEvent
import android.view.View
import com.angcyo.uiview.recycler.RExItemDecoration
import com.angcyo.uiview.recycler.RRecyclerView
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
                        it.getItemHolderByPosition(position)?.getItemOffsets(this@apply, outRect, position, edge)
                    }
                }

                override fun draw(canvas: Canvas, paint: TextPaint, itemView: View, offsetRect: Rect, itemCount: Int, position: Int) {
                    //super.draw(canvas, paint, itemView, offsetRect, itemCount, position)
                    exItemAdapter?.let {
                        it.getItemHolderByPosition(position)?.draw(this@apply, canvas, paint, itemView, offsetRect, itemCount, position)
                    }
                }
            })
        }
    }

    override fun createAdapter(): RExBaseAdapter<String, DataType, String> {
        exItemAdapter = object : RExItemAdapter<ItemType, DataType>(mActivity, createItemFactory()) {
            override fun onScrollStateChanged(recyclerView: RRecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrollStateEnd(rRecyclerView: RRecyclerView,
                                          firstItemVisible: Boolean, lastItemVisible: Boolean,
                                          topCanScroll: Boolean, bottomCanScroll: Boolean) {
                super.onScrollStateEnd(rRecyclerView, firstItemVisible, lastItemVisible, topCanScroll, bottomCanScroll)
            }

            override fun onScrolled(recyclerView: RRecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                onRecyclerViewScrolled(recyclerView, dx, dy)
            }

            override fun onScrolledInTouch(recyclerView: RRecyclerView, e1: MotionEvent, e2: MotionEvent,
                                           distanceX: Float, distanceY: Float) {
                super.onScrolledInTouch(recyclerView, e1, e2, distanceX, distanceY)
            }
        }
        exItemAdapter?.initItemFactory(this)
        return exItemAdapter!!
    }

    open fun onRecyclerViewScrolled(recyclerView: RRecyclerView, dx: Int, dy: Int) {

    }

    private fun createItemFactory(): RExItemFactory<ItemType, DataType> {
        return object : RExItemFactory<ItemType, DataType>() {

            override fun onItemFactoryInit() {
                super.onItemFactoryInit()
                noSupportTypeItem.let {
                    it.itemHolderObj?.let {
                        it.iLayout = mParentILayout
                        it.exUIView = this@UIExItemUIView
                        it.exItemAdapter = mExBaseAdapter as RExItemAdapter<*, DataType>?
                    }
                }
                this@UIExItemUIView.onItemFactoryInit()
            }

            override fun onCreateItemHolder(itemHolder: RExItemHolder<DataType>) {
                super.onCreateItemHolder(itemHolder)
                itemHolder.iLayout = mParentILayout
                itemHolder.exUIView = this@UIExItemUIView

                itemHolder.exItemAdapter = exItemAdapter

                this@UIExItemUIView.onCreateItemHolder(itemHolder)
            }

            override fun registerItems(allRegItems: ArrayList<RExItem<ItemType, DataType>>) {
                this@UIExItemUIView.registerItems(allRegItems)
            }

            override fun getItemTypeFromData(data: DataType?, position: Int): ItemType? {
                return this@UIExItemUIView.getItemTypeFromData(data, position)
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
            it.allDatas.forEachIndexed { index, dataType ->
                if (itemType == getItemTypeFromData(dataType, index)) {
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
    abstract fun getItemTypeFromData(data: DataType?, position: Int): ItemType?
}