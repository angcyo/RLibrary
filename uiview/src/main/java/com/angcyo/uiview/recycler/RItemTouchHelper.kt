package com.angcyo.uiview.recycler

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/01/09 10:24
 * 修改人员：Robi
 * 修改时间：2018/01/09 10:24
 * 修改备注：
 * Version: 1.0.0
 */
open class RItemTouchHelper(callback: Callback) : ItemTouchHelper(callback) {
    private var mRecyclerView: RecyclerView? = null

//    private val mOnItemTouchListener = object : RecyclerView.OnItemTouchListener {
//        override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
//            //L.e("call: onInterceptTouchEvent -> ${event.actionMasked}")
//            return false
//        }
//
//        override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
//            //L.e("call: onTouchEvent -> ${event.actionMasked}")
//        }
//
//        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
//            //L.e("call: onRequestDisallowInterceptTouchEvent -> ")
//        }
//    }
//
//    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
//        super.attachToRecyclerView(recyclerView)
//        if (mRecyclerView == recyclerView) {
//            return  // nothing to do
//        }
//        mRecyclerView?.removeOnItemTouchListener(mOnItemTouchListener)
//        mRecyclerView = recyclerView
//        mRecyclerView?.addOnItemTouchListener(mOnItemTouchListener)
//    }
}