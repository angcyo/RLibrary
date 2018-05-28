package com.angcyo.uiview.recycler.adapter

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.support.annotation.ColorRes
import android.text.TextPaint
import android.view.View
import com.angcyo.uiview.base.UIBaseRxView
import com.angcyo.uiview.container.ILayout
import com.angcyo.uiview.container.UIParam
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.RExItemDecoration
import com.angcyo.uiview.utils.ScreenUtil
import com.angcyo.uiview.view.IView
import rx.Subscription

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：Item处理类
 * 创建人员：Robi
 * 创建时间：2018/03/16 16:53
 * 修改人员：Robi
 * 修改时间：2018/03/16 16:53
 * 修改备注：
 * Version: 1.0.0
 */
abstract class RExItemHolder<DataType> {
    var iLayout: ILayout? = null
    var exUIView: UIBaseRxView? = null
    var exItemAdapter: RExItemAdapter<*, DataType>? = null

    open fun startIView(iView: IView) {
        iLayout?.startIView(iView)
    }

    /**启动一个IView页面*/
    open fun startIView(iView: IView, param: UIParam) {
        iLayout?.startIView(iView, param)
    }

    /**当创建完之后*/
    open fun onCreateItemHolderAfter() {

    }

    /**重写此方法, 核心*/
    abstract fun onBindItemDataView(holder: RBaseViewHolder, posInData: Int, dataBean: DataType?)

    fun add(subscription: Subscription) {
        exUIView?.add(subscription)
    }

    fun cancel() {
        exUIView?.onCancel()
    }

    fun getActivity(): Activity {
        return if (exUIView == null) {
            iLayout!!.layout.context as Activity
        } else {
            exUIView!!.mActivity
        }
    }

    fun density(): Float {
        return ScreenUtil.density()
    }

    fun getColor(@ColorRes id: Int): Int {
        return exUIView?.getColor(id) ?: Color.TRANSPARENT
    }

    /**用来返回 RecyclerView的分割线距离*/
    open fun getItemOffsets(itemDecoration: RExItemDecoration, outRect: Rect, position: Int, edge: Int) {

    }

    /**绘制分割线*/
    open fun draw(itemDecoration: RExItemDecoration, canvas: Canvas, paint: TextPaint, itemView: View, offsetRect: Rect, itemCount: Int, position: Int) {
    }

    fun drawTop(canvas: Canvas, paint: TextPaint, itemView: View, offsetRect: Rect) {
        canvas.drawRect(0f, itemView.top.toFloat() - offsetRect.top, itemView.right.toFloat(), itemView.top.toFloat(), paint)
    }

    fun drawBottom(canvas: Canvas, paint: TextPaint, itemView: View, offsetRect: Rect) {
        canvas.drawRect(0f, itemView.bottom.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat() + offsetRect.bottom, paint)
    }

    fun notifyItemChanged(position: Int) {
        exItemAdapter?.notifyItemChanged(position)
    }
}