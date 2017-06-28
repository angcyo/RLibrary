package com.angcyo.uiview.base

import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.github.tablayout.SlidingTabLayout
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.view.IView
import com.angcyo.uiview.widget.viewpager.UIPagerAdapter
import com.angcyo.uiview.widget.viewpager.UIViewPager

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：标准的SlidingTab和UIViewPager组合的界面
 * 创建人员：Robi
 * 创建时间：2017/06/28 11:43
 * 修改人员：Robi
 * 修改时间：2017/06/28 11:43
 * 修改备注：
 * Version: 1.0.0
 */
abstract class UISlidingTabView : UIContentView() {
    val mViewPager: UIViewPager by lazy {
        mViewHolder.v<UIViewPager>(R.id.base_view_pager)
    }

    val mSlidingTab: SlidingTabLayout by lazy {
        mViewHolder.v<SlidingTabLayout>(R.id.base_tab_layout)
    }

    override fun inflateContentLayout(baseContentLayout: RelativeLayout?, inflater: LayoutInflater?) {
        inflate(R.layout.base_sliding_tab_view)
    }

    override fun onViewLoad() {
        super.onViewLoad()
        mViewPager.adapter = createAdapter()

        mSlidingTab.setViewPager(mViewPager)

        initTabLayout(mSlidingTab)
    }

    open fun initTabLayout(tabLayout: SlidingTabLayout) {
        tabLayout.textSelectColor = SkinHelper.getSkin().themeSubColor
        tabLayout.textUnselectColor = getColor(R.color.base_text_color)
        tabLayout.indicatorHeight = 1 * density()
        tabLayout.indicatorColor = SkinHelper.getSkin().themeSubColor
        tabLayout.setIndicatorWidthEqualTitle(true)
        tabLayout.indicatorStyle = SlidingTabLayout.STYLE_NORMAL
        tabLayout.indicatorCornerRadius = 3 * density() //指示器的圆角
        tabLayout.indicatorMarginLeft = 0f //指示器左偏移的距离
        tabLayout.tabPadding = 5 * density()
    }

    /**页面数量*/
    abstract fun getPageCount(): Int

    /**对应页面*/
    abstract fun getPageIView(position: Int): IView

    /**页面标题*/
    abstract fun getPageTitle(position: Int): String

    open fun createAdapter() = object : UIPagerAdapter() {
        override fun getIView(position: Int): IView = getPageIView(position)

        override fun getCount(): Int = getPageCount()

        override fun getPageTitle(position: Int): CharSequence = this@UISlidingTabView.getPageTitle(position)
    }
}