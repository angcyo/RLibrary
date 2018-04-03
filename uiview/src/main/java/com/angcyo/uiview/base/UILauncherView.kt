package com.angcyo.uiview.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.angcyo.github.utilcode.utils.AppUtils
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.widget.viewpager.ImageAdapter
import com.angcyo.uiview.widget.viewpager.RViewPager
import com.orhanobut.hawk.Hawk

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：简单的图片滑动启动页
 * 创建人员：Robi
 * 创建时间：2017/06/27 14:11
 * 修改人员：Robi
 * 修改时间：2017/06/27 14:11
 * 修改备注：
 * Version: 1.0.0
 */
abstract class UILauncherView : UIContentView() {

    companion object {
        /**是否需要显示启动页*/
        fun checkNeedShow(activity: Activity): Boolean = Hawk.get(AppUtils.getAppVersionName(activity), true)

        fun checkNeedShow(activity: Activity, onNeed: () -> Unit) {
            if (checkNeedShow(activity)) {
                onNeed.invoke()
            }
        }
    }

    lateinit var mViewPager: RViewPager

    override fun inflateContentLayout(baseContentLayout: ContentLayout, inflater: LayoutInflater) {
        mViewPager = RViewPager(mActivity)
        mViewPager.adapter = object : ImageAdapter() {
            override fun initImageView(rootLayout: RelativeLayout, imageView: ImageView, position: Int) {
                onCreateItem(imageView, position)
            }

            override fun getCount(): Int {
                return getItemCount()
            }

        }
        baseContentLayout.addView(mViewPager, ViewGroup.LayoutParams(-1, -1))
        mViewPager.setOnPagerEndListener {
            onPagerEnd()
        }
    }

    override fun onViewShowFirst(bundle: Bundle?) {
        super.onViewShowFirst(bundle)
        //当前版本是否显示了导航页
        Hawk.put(AppUtils.getAppVersionName(mActivity), false)
    }

    abstract fun getItemCount(): Int

    abstract fun onCreateItem(imageView: ImageView, position: Int)

    /**在最后一页滑动的时候*/
    open fun onPagerEnd() {

    }

}