package com.angcyo.uiview.base

import android.support.annotation.CallSuper
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.container.UIParam
import com.angcyo.uiview.view.IView
import com.angcyo.uiview.view.UIIViewImpl
import com.angcyo.uiview.viewgroup.RTabLayout
import com.angcyo.uiview.widget.viewpager.UIPagerAdapter
import com.angcyo.uiview.widget.viewpager.UIViewPager

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：需要Tab的界面基类, ViewPager形式
 * 创建人员：Robi
 * 创建时间：2018/06/26 15:25
 * 修改人员：Robi
 * 修改时间：2018/06/26 15:25
 * 修改备注：
 * Version: 1.0.0
 */
abstract class UIBaseTabPagerView : UIIViewImpl() {
    protected val iViews = SparseArray<IView>()

    protected lateinit var viewPager: UIViewPager
    protected lateinit var tablayout: RTabLayout
    override fun inflateBaseView(container: FrameLayout, inflater: LayoutInflater): View {
        return inflater.inflate(getBaseLayoutId(), container).apply {
            viewPager = findViewById(R.id.base_view_pager)
            tablayout = findViewById(R.id.base_tab_layout)

            viewPager.setParentUIView(this@UIBaseTabPagerView)

            initTabLayout(tablayout, viewPager)
            initAfterInflateView(container)
        }
    }

    open fun getBaseLayoutId(): Int = R.layout.base_uiview_tab_pager_layout

    @CallSuper
    open fun initAfterInflateView(container: FrameLayout) {

    }

    override fun onViewCreate(rootView: View, param: UIParam) {
        super.onViewCreate(rootView, param)
    }

    @CallSuper
    open fun initTabLayout(tabLayout: RTabLayout, viewPager: UIViewPager) {
        viewPager.adapter = object : UIPagerAdapter() {

            override fun getItemType(position: Int): Int {
                return this@UIBaseTabPagerView.getItemType(position)
            }

            override fun getCount(): Int {
                return this@UIBaseTabPagerView.getPagerCount()
            }

            override fun getIView(position: Int, itemType: Int): IView {
                val cacheIView = iViews.get(position)
                val iView = cacheIView ?: this@UIBaseTabPagerView.getPagerIView(position, itemType)
                if (cacheIView == null) {
                    iViews.put(position, iView)
                }
                return iView
            }
        }

        tablayout.onTabLayoutListener = object : RTabLayout.OnTabLayoutListener() {
            override fun onPageScrolled(tabLayout: RTabLayout, currentView: View?, nextView: View?, positionOffset: Float) {
                this@UIBaseTabPagerView.onPageScrolled(tabLayout, currentView, nextView, positionOffset)
            }

            override fun onSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
                this@UIBaseTabPagerView.onSelectorItemView(tabLayout, itemView, index)
            }

            override fun onUnSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
                this@UIBaseTabPagerView.onUnSelectorItemView(tabLayout, itemView, index)
            }

            override fun canSelectorTab(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int): Boolean {
                return this@UIBaseTabPagerView.canSelectorTab(tabLayout, fromIndex, toIndex)
            }

            override fun onTabSelector(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int) {
                this@UIBaseTabPagerView.onTabSelector(tabLayout, fromIndex, toIndex)
            }

            override fun onTabReSelector(tabLayout: RTabLayout, itemView: View, index: Int) {
                this@UIBaseTabPagerView.onTabReSelector(tabLayout, itemView, index)
            }
        }

        tabLayout.setupViewPager(viewPager)
    }

    open fun onPageScrolled(tabLayout: RTabLayout, currentView: View?, nextView: View?, positionOffset: Float) {
    }

    open fun onSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
    }

    open fun onUnSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
    }

    open fun canSelectorTab(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int): Boolean {
        return true
    }

    open fun onTabSelector(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int) {
        viewPager.setCurrentItem(toIndex, fromIndex != -1 && fromIndex != toIndex)
    }

    open fun onTabReSelector(tabLayout: RTabLayout, itemView: View, index: Int) {

    }

    abstract fun getPagerIView(position: Int, itemType: Int): IView
    abstract fun getPagerCount(): Int

    open fun getItemType(position: Int): Int {
        return 1
    }
}