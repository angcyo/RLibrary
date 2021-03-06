package com.angcyo.uiview.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.angcyo.library.utils.L
import com.angcyo.uiview.R
import com.angcyo.uiview.container.UILayoutImpl
import com.angcyo.uiview.container.UIParam
import com.angcyo.uiview.rsen.RGestureDetector
import com.angcyo.uiview.utils.UI
import com.angcyo.uiview.view.UIIViewImpl
import com.angcyo.uiview.viewgroup.SliderMenuLayout
import com.angcyo.uiview.widget.TouchMoveGroupLayout
import com.angcyo.uiview.widget.TouchMoveView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：自带类似QQ底部导航效果的主页面
 * 创建人员：Robi
 * 创建时间：2017/06/06 10:51
 * 修改人员：Robi
 * 修改时间：2017/06/06 10:51
 * 修改备注：
 * Version: 1.0.0
 */
abstract class UINavigationView : UIIViewImpl() {

    /**默认选择的页面*/
    var selectorPosition = 0
        set(value) {
            field = value
            touchMoveGroupLayout?.selectorPosition = selectorPosition
        }

    /**保存所有界面*/
    var pages = arrayListOf<PageBean>()

    var pageViews = arrayListOf<TouchMoveView>()

    var lastIndex = selectorPosition

    /**导航的root layout, 用来包裹 TouchMoveView*/
    val touchMoveGroupLayout: TouchMoveGroupLayout? by lazy {
        mViewHolder.v<TouchMoveGroupLayout>(R.id.navigation_bar_wrapper)
    }

    /**底部导航上层的阴影图层*/
    val shadowView: View? by lazy {
        val view = mViewHolder.v<View>(R.id.shadow_view)
        view?.visibility = if (showShadowView) View.VISIBLE else View.GONE
        view
    }

    /**用来管理子页面的ILayout*/
    val mainLayoutImpl: UILayoutImpl? by lazy {
        val impl = mViewHolder.v<UILayoutImpl>(R.id.main_layout_imp)
        impl
    }

    var showShadowView = true
        set(value) {
            field = value
            shadowView?.visibility = if (value) View.VISIBLE else View.GONE
        }

    var sliderMenuLayout: SliderMenuLayout? = null

    override fun onBackPressed(): Boolean {
        if (haveSliderMenu()) {
            if (sliderMenuLayout?.isMenuOpen() == true) {
                sliderMenuLayout?.closeMenu()
                return false
            }
        }
        return super.onBackPressed()
    }

    //    override fun inflateContentLayout(baseContentLayout: ContentLayout, inflater: LayoutInflater) {
//        inflate(R.layout.base_navigation_view)
//    }
    @ColorInt
    fun getDefaultBackgroundColor(): Int {
        return Color.WHITE
    }

    fun getDefaultBackgroundDrawable(): Drawable {
        return ColorDrawable(getDefaultBackgroundColor())
    }

    override fun inflateBaseView(container: FrameLayout, inflater: LayoutInflater): View {
        //inflate(R.layout.base_navigation_view)
        val view: View
        if (haveSliderMenu()) {
            sliderMenuLayout = SliderMenuLayout(mActivity).apply {
                initSliderMenu(this, inflater)
                inflater.inflate(R.layout.base_navigation_view, this)
                UI.setBackgroundDrawable(this.getChildAt(this.childCount - 1), getDefaultBackgroundDrawable())
            }
            view = sliderMenuLayout!!
        } else {
            view = inflater.inflate(R.layout.base_navigation_view, container, false)
        }

        initAfterInflateView(view)
        container.addView(view, ViewGroup.LayoutParams(-1, -1))
        return view
    }

    /**是否需要滑动菜单*/
    open fun haveSliderMenu() = false

    /**请将菜单add到 sliderMenuLayout*/
    open fun initSliderMenu(sliderMenuLayout: SliderMenuLayout, inflater: LayoutInflater) {

    }

    open fun initAfterInflateView(rootView: View) {
        UI.setBackgroundDrawable(rootView, getDefaultBackgroundDrawable())
    }

    override fun onViewLoad() {
        super.onViewLoad()
        mainLayoutImpl?.let {
            it.setEnableSwipeBack(false)//关闭侧滑
            parentILayout.setChildILayout(it)
        }
        createPages(pages)
        onCreatePages()
        onCreatePagesEnd()
    }

    open fun onCreatePages() {
        pages.forEachIndexed { index, pageBean ->
            val navItem = createNavItem(pageBean)
            pageViews.add(navItem)
            touchMoveGroupLayout?.addView(navItem)

            RGestureDetector.onDoubleTap(navItem) {
                onNavItemDoubleTap(navItem, index)
            }
        }
    }

    open fun onCreatePagesEnd() {
        touchMoveGroupLayout?.selectorPosition = selectorPosition
        touchMoveGroupLayout?.updateSelectorStyle()
        touchMoveGroupLayout?.listener = object : TouchMoveGroupLayout.OnSelectorPositionListener {
            override fun onRepeatSelectorPosition(targetView: TouchMoveView, position: Int) {
                //重复选择页面
                this@UINavigationView.onRepeatSelectorPosition(targetView, position)
            }

            override fun onSelectorPosition(targetView: TouchMoveView, position: Int) {
                //选择新页面
                this@UINavigationView.onSelectorPosition(targetView, position)
            }
        }
    }

    open fun createNavItem(page: PageBean): TouchMoveView {
        val view = TouchMoveView(mActivity)
        view.textNormal = page.textNormal
        view.textSelected = page.textSelected

        //文本与图标之间的间隙
        view.mTextSpace = 4 * density()

        if (page.textColorNormal != null) {
            view.mTextColorNormal = page.textColorNormal
        }
        if (page.textColorSelected != null) {
            view.mTextColorSelected = page.textColorSelected
        }
        if (page.icoResNormal != null) {
            view.mDrawableNormal = getDrawable(page.icoResNormal)
        } else {
            view.mDrawableNormal = null
        }
        if (page.icoResSelected != null) {
            view.mDrawableSelected = getDrawable(page.icoResSelected)
        } else {
            view.mDrawableSelected = null
        }
        if (page.icoSubResNormal != null) {
            view.mSubDrawableNormal = getDrawable(page.icoSubResNormal)
        } else {
            view.mSubDrawableNormal = null
        }
        if (page.icoSubResSelected != null) {
            view.mSubDrawableSelected = getDrawable(page.icoSubResSelected)
        } else {
            view.mSubDrawableSelected = null
        }
        page.iview.bindParentILayout(iLayout)
        return view
    }

    /**创建页面*/
    abstract fun createPages(pages: ArrayList<PageBean>)

    open fun onSelectorPosition(targetView: TouchMoveView, position: Int) {
        L.i("UINavigationView 选择 -> 位置:$position IView:${UILayoutImpl.name(pages[position].iview)}")
        mainLayoutImpl?.startIView(pages[position].iview.setIsRightJumpLeft(lastIndex > position),
                UIParam(lastIndex != position).setAsync(false).setLaunchMode(UIParam.SINGLE_TOP))

        lastIndex = position
    }

    open fun onRepeatSelectorPosition(targetView: TouchMoveView, position: Int) {
        L.i("UINavigationView 重复选择 -> 位置:$position IView:${UILayoutImpl.name(pages[position].iview)}")
    }

    open fun onNavItemDoubleTap(targetView: TouchMoveView, position: Int) {
        L.i("UINavigationView 双击 -> 位置:$position IView:${UILayoutImpl.name(pages[position].iview)}")

        val iview = pages[position].iview
        if (iview is UIRecyclerUIView<*, *, *>) {
            iview.onDoubleScrollToTop()
        }
    }

    /**显示未读消息数*/
    fun showNoReadNum(position: Int, num: Int) {
        (touchMoveGroupLayout?.getChildAt(position) as TouchMoveView?)?.noReadNum = num
    }
}
