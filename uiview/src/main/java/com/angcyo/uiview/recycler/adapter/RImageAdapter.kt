package com.angcyo.uiview.recycler.adapter

import android.content.Context
import com.angcyo.uiview.R
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.widget.GlideImageView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：快速用来显示图片的Adapter
 * 创建人员：Robi
 * 创建时间：2018/01/03 16:39
 * 修改人员：Robi
 * 修改时间：2018/01/03 16:39
 * 修改备注：
 * Version: 1.0.0
 */

open class RImageAdapter<T> : RModelAdapter<T> {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, datas: List<T>?) : super(context, datas)
    constructor(context: Context?, model: Int) : super(context, model)

    override fun getItemLayoutId(viewType: Int): Int {
        return R.layout.baes_item_image_adapter_layout
    }

    override fun onBindCommonView(holder: RBaseViewHolder, position: Int, bean: T?) {
        val imageView = holder.glideImgV(R.id.base_image_view)
        //L.e("call: onBindCommonView 111-> $position")
        onBindImageView(imageView, holder, position, bean)
    }

    open fun onBindImageView(imageView: GlideImageView?, holder: RBaseViewHolder, position: Int, bean: T?) {
        imageView?.let {
            it.reset()
            if (bean is String) {
                it.url = bean
            }
        }
    }
}