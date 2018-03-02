package com.angcyo.uiview.base

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.github.tablayout.SlidingTabLayout
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.UI
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

    companion object {
        fun baseInitTabLayout(tabLayout: SlidingTabLayout) {
            val density = tabLayout.context.resources.displayMetrics.density

            tabLayout.textSelectColor = SkinHelper.getSkin().themeSubColor
            tabLayout.textUnselectColor = ContextCompat.getColor(tabLayout.context, R.color.base_text_color)
            tabLayout.indicatorHeight = 1 * density
            tabLayout.indicatorColor = SkinHelper.getSkin().themeSubColor
            tabLayout.indicatorStyle = SlidingTabLayout.STYLE_NORMAL
            tabLayout.indicatorCornerRadius = 3 * density //指示器的圆角
            tabLayout.indicatorMarginLeft = 0f //指示器左偏移的距离
            tabLayout.tabPadding = 5 * density
            tabLayout.setTextSizePx(SkinHelper.getSkin().mainTextSize)
            tabLayout.setItemNoBackground(false) //item 是否禁用点击效果

            tabLayout.setIndicatorWidthEqualTitle(true)
            //tabLayout.indicatorWidth = 100 //px单位, 自动换算
            tabLayout.isTabSpaceEqual = false //tab 是否平分 TabLayout的宽度
            //UI.setViewWidth(tabLayout, (300 * density).toInt())
        }
    }

    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar()
    }

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
        val uiViewPager = mViewHolder.v<UIViewPager>(R.id.base_view_pager)
        uiViewPager.setParentUIView(this)
        uiViewPager
    }

    val mSlidingTab: SlidingTabLayout by lazy {
        mViewHolder.v<SlidingTabLayout>(R.id.base_tab_layout)
    }

    val mTabWrapperLayout: View by lazy {
        mViewHolder.v<View>(R.id.base_tab_wrapper_layout)
    }

    override fun inflateContentLayout(baseContentLayout: ContentLayout?, inflater: LayoutInflater?) {
        inflate(R.layout.base_sliding_tab_view)
    }

    override fun onViewLoad() {
        super.onViewLoad()

        createPages(pages)
        initTabLayout(mSlidingTab)
        initViewPager(mViewPager)

        mViewPager.adapter = createAdapter()
        mSlidingTab.setViewPager(mViewPager)
    }

    open fun getCurrentIndex(): Int {
        return mSlidingTab.currentTab
    }

    override fun onViewShowFirst(bundle: Bundle?) {
        super.onViewShowFirst(bundle)

//        createPages(pages)
//        mViewPager.adapter = createAdapter()
//
//        initTabLayout(mSlidingTab)
//        initViewPager(mViewPager)
//
//        mSlidingTab.setViewPager(mViewPager)
    }

    open fun initTabLayout(tabLayout: SlidingTabLayout) {
        baseInitTabLayout(tabLayout)
    }

    /**居中展示TabLayout*/
    fun centerTabLayout(tabLayout: SlidingTabLayout) {
        tabLayout.indicatorWidth = 80f
        tabLayout.isTabSpaceEqual = true
        UI.setViewWidth(tabLayout, (300 * density()).toInt())
    }

    open fun initViewPager(viewPager: UIViewPager) {
        viewPager.offscreenPageLimit = getPageCount()
    }

    /**重写此方法, 创建页面*/
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