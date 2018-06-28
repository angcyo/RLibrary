package com.angcyo.uiview.base

import android.support.annotation.CallSuper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.angcyo.library.utils.L
import com.angcyo.uiview.R
import com.angcyo.uiview.container.UILayoutImpl
import com.angcyo.uiview.container.UIParam
import com.angcyo.uiview.view.IView
import com.angcyo.uiview.view.UIIViewImpl
import com.angcyo.uiview.viewgroup.RTabLayout

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：需要Tab的界面基类
 * 创建人员：Robi
 * 创建时间：2018/06/26 15:25
 * 修改人员：Robi
 * 修改时间：2018/06/26 15:25
 * 修改备注：
 * Version: 1.0.0
 */
abstract class UIBaseTabView : UIIViewImpl() {

    protected val iViews = mutableListOf<IView>()

    protected lateinit var subUILayoutImpl: UILayoutImpl
    protected lateinit var tablayout: RTabLayout
    override fun inflateBaseView(container: FrameLayout, inflater: LayoutInflater): View {
        return inflater.inflate(getBaseLayoutId(), container).apply {
            subUILayoutImpl = findViewById(R.id.base_sub_layout_impl)
            tablayout = findViewById(R.id.base_tab_layout)

            initTabLayout(tablayout, iViews)
            initAfterInflateView(container)
        }
    }

    /**重写此方法, 自定义布局*/
    open fun getBaseLayoutId(): Int = R.layout.base_uiview_tab_layout

    @CallSuper
    open fun initAfterInflateView(container: FrameLayout) {
        parentILayout.setChildILayout(subUILayoutImpl)
        setChildILayout(subUILayoutImpl)
    }

    override fun onViewCreate(rootView: View, param: UIParam) {
        super.onViewCreate(rootView, param)
        iViews.map {
            it.bindParentILayout(mParentILayout)
        }
    }

    /**重写此方法,设置界面*/
    @CallSuper
    open fun initTabLayout(tabLayout: RTabLayout /*添加tab item*/, iViews: MutableList<IView> /*对应的界面*/) {
        tablayout.onTabLayoutListener = object : RTabLayout.OnTabLayoutListener() {
            override fun onPageScrolled(tabLayout: RTabLayout, currentView: View?, nextView: View?, positionOffset: Float) {
                this@UIBaseTabView.onPageScrolled(tabLayout, currentView, nextView, positionOffset)
            }

            override fun onSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
                this@UIBaseTabView.onSelectorItemView(tabLayout, itemView, index)
            }

            override fun onUnSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
                this@UIBaseTabView.onUnSelectorItemView(tabLayout, itemView, index)
            }

            override fun canSelectorTab(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int): Boolean {
                return this@UIBaseTabView.canSelectorTab(tabLayout, fromIndex, toIndex)
            }

            override fun onTabSelector(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int) {
                this@UIBaseTabView.onTabSelector(tabLayout, fromIndex, toIndex)
            }

            override fun onTabReSelector(tabLayout: RTabLayout, itemView: View, index: Int) {
                this@UIBaseTabView.onTabReSelector(tabLayout, itemView, index)
            }
        }
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
        if (toIndex >= iViews.size) {
            L.w("请求第$toIndex 个页面, 实际只有${iViews.size} 个页面.")
            return
        }
        subUILayoutImpl.startIView(iViews[toIndex].setIsRightJumpLeft(fromIndex > toIndex),
                UIParam(fromIndex != -1 && fromIndex != toIndex).setAsync(false).setLaunchMode(UIParam.SINGLE_TOP))
    }

    open fun onTabReSelector(tabLayout: RTabLayout, itemView: View, index: Int) {

    }
}