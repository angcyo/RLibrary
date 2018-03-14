package com.angcyo.uiview.base

import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.view.IView
import com.angcyo.uiview.widget.TouchMoveView
import com.angcyo.uiview.widget.viewpager.UIPagerAdapter
import com.angcyo.uiview.widget.viewpager.UIViewPager

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：自带类似QQ底部导航效果的主页面, 采用ViewPager页面结构
 * 创建人员：Robi
 * 创建时间：2017/06/06 10:51
 * 修改人员：Robi
 * 修改时间：2017/06/06 10:51
 * 修改备注：
 * Version: 1.0.0
 */
abstract class UINavigationPagerView : UINavigationView() {

    val uiViewPager: UIViewPager? by lazy {
        mViewHolder.v<UIViewPager>(R.id.base_view_pager)
    }

    override fun inflateBaseView(container: FrameLayout, inflater: LayoutInflater): View {
        //inflate(R.layout.base_navigation_view)
        val view = inflater.inflate(R.layout.base_navigation_pager_view, container, false)
        initAfterInflateView(view)
        container.addView(view, ViewGroup.LayoutParams(-1, -1))
        return view
    }

    override fun onCreatePagesEnd() {
        uiViewPager?.let {
            it.setParentUIView(this)
            it.offscreenPageLimit = pages.size
            it.adapter = object : UIPagerAdapter() {
                override fun getIView(position: Int): IView = pages[position].iview.apply {
                    isShowInViewPager = true
                }

                override fun getCount(): Int = pages.size
            }
            it.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    touchMoveGroupLayout?.let {
                        it.selectorPosition = position
                        it.updateSelectorStyle(false)
                    }
                }
            })
        }
        super.onCreatePagesEnd()
    }

    override fun onSelectorPosition(targetView: TouchMoveView, position: Int) {
        super.onSelectorPosition(targetView, position)
        uiViewPager?.setCurrentItem(position, true)
    }
}
