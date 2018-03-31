package com.angcyo.uiview.dialog

import android.view.Gravity
import com.angcyo.uiview.R
import com.angcyo.uiview.recycler.RBaseItemDecoration
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.RExBaseAdapter

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/02/24 16:51
 * 修改人员：Robi
 * 修改时间：2018/02/24 16:51
 * 修改备注：
 * Version: 1.0.0
 */
class UIItemSelectorDialog<T>(val items: List<T>) : UIRecyclerDialog<String, T, String>() {

    var onItemSelector: ((position: Int, bean: T) -> Unit)? = null

    var onInitItemLayout: ((holder: RBaseViewHolder, posInData: Int, dataBean: T) -> Unit)? = null

    var itemLayoutId = R.layout.base_text_item_selector_layout

    override fun getGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun createAdapter(): RExBaseAdapter<String, T, String> = object : RExBaseAdapter<String, T, String>(mActivity, items) {

        override fun onBindDataView(holder: RBaseViewHolder, posInData: Int, dataBean: T) {
            super.onBindDataView(holder, posInData, dataBean)
            if (dataBean is String) {
                holder.tv(R.id.base_text_view).text = dataBean
            }

            holder.click(R.id.base_item_root_layout) {
                finishDialog {
                    onItemSelector?.invoke(posInData, dataBean)
                }
            }

            onInitItemLayout?.invoke(holder, posInData, dataBean)
        }

        override fun getItemLayoutId(viewType: Int): Int {
            return itemLayoutId
        }
    }


    override fun initRecyclerView() {
        super.initRecyclerView()
        recyclerView.addItemDecoration(RBaseItemDecoration(getDimensionPixelOffset(R.dimen.base_line),
                getColor(R.color.base_chat_bg_color)))
    }
}