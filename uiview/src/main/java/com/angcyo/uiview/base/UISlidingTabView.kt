package com.angcyo.uiview.base

import android.view.LayoutInflater
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout
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
abstract class UISlidingTabView : UIContentView(), UIBaseView.OnViewLoadListener {
    override fun onShowLoadView() {
        showLoadView()
    }

    override fun onHideLoadView() {
        hideLoadView()
    }

    val pages: ArrayList<TabPageBean> by lazy {
        arrayListOf<TabPageBean>()
    }

    val mViewPager: UIViewPager by lazy {
        mViewHolder.v<UIViewPager>(R.id.base_view_pager)
    }

    val mSlidingTab: SlidingTabLayout by lazy {
        mViewHolder.v<SlidingTabLayout>(R.id.base_tab_layout)
    }

    override fun inflateContentLayout(baseContentLayout: ContentLayout?, inflater: LayoutInflater?) {
        inflate(R.layout.base_sliding_tab_view)
    }

    override fun onViewLoad() {
        super.onViewLoad()

        createPages(pages)
        mViewPager.adapter = createAdapter()
        mSlidingTab.setViewPager(mViewPager)

        initTabLayout(mSlidingTab)
        initViewPager(mViewPager)
    }

    open fun initTabLayout(tabLayout: SlidingTabLayout) {
        tabLayout.setIndicatorWidthEqualTitle(true)
        tabLayout.textSelectColor = SkinHelper.getSkin().themeSubColor
        tabLayout.textUnselectColor = getColor(R.color.base_text_color)
        tabLayout.indicatorHeight = 1 * density()
        tabLayout.indicatorColor = SkinHelper.getSkin().themeSubColor
        tabLayout.indicatorStyle = SlidingTabLayout.STYLE_NORMAL
        tabLayout.indicatorCornerRadius = 3 * density() //指示器的圆角
        tabLayout.indicatorMarginLeft = 0f //指示器左偏移的距离
        tabLayout.tabPadding = 5 * density()
    }

    open fun initViewPager(viewPager: UIViewPager) {

    }

    /**创建页面*/
    open fun createPages(pages: ArrayList<TabPageBean>) {

    }

    /**页面数量*/
    open fun getPageCount(): Int = pages.size

    /**对应页面*/
    open fun getPageIView(position: Int): IView = pages[position].iView

    /**页面标题*/
    open fun getPageTitle(position: Int): String? = pages[position].title

    open fun createAdapter() = object : UIPagerAdapter() {
        override fun getIView(position: Int): IView = (getPageIView(position) as UIBaseView).apply {
            this.bindParentILayout(mILayout)
            this.setOnViewLoadListener(this@UISlidingTabView)
        }

        override fun getCount(): Int = getPageCount()

        override fun getPageTitle(position: Int): CharSequence? = this@UISlidingTabView.getPageTitle(position)
    }

    /**页面*/
    data class TabPageBean(val iView: IView, val title: String? = null)
}