package com.angcyo.picker.media.uiview

import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.widget.TextView
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.kotlin.clickIt
import com.angcyo.uiview.kotlin.hideFromBottom
import com.angcyo.uiview.kotlin.showFromBottom
import com.angcyo.uiview.model.TitleBarItem
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.widget.viewpager.RPagerAdapter
import com.bumptech.glide.Glide

/**
 * Created by angcyo on 2018/06/18 07:13
 * 媒体大图预览选择界面
 */
class RMediaPagerUIView(mediaLoaderConfig: MediaLoaderConfig,
                        val allMediaList: List<MediaItem> /*总共的媒体*/,
                        selectorMediaList: MutableList<MediaItem> /*选中的媒体*/,
                        var position: Int = 0 /*总媒体中的索引*/) : BaseMediaUIView() {
    init {
        this@RMediaPagerUIView.selectorMediaList = selectorMediaList
        this@RMediaPagerUIView.mediaLoaderConfig = mediaLoaderConfig
    }

    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar()
                .setTitleString("")
                .setFloating(true)
                .addLeftItem(TitleBarItem.build().setClickable(false).setId(R.id.base_text_view))
    }

    override fun inflateContentLayout(baseContentLayout: ContentLayout, inflater: LayoutInflater) {
        inflate(R.layout.view_media_pager_layout)
    }

    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()

        view(R.id.base_bottom_control_layout).setBackgroundColor(SkinHelper.getTranColor(titleBarBGColor, 180))

        mViewHolder.pager(R.id.base_view_pager).apply {
            offscreenPageLimit = 1
            adapter = object : RPagerAdapter() {
                override fun getLayoutId(position: Int): Int {
                    return R.layout.pager_image_layout
                }

                override fun initItemView(viewHolder: RBaseViewHolder, position: Int) {
                    super.initItemView(viewHolder, position)

                    viewHolder.imgV(R.id.base_image_view).apply {
                        Glide.with(mActivity).load(allMediaList[position].path).into(this)

                        clickIt {
                            if (uiTitleBarContainer.isTitleBarShow) {
                                uiTitleBarContainer.hide(true)
                                view(R.id.base_bottom_control_layout).hideFromBottom()
                            } else {
                                uiTitleBarContainer.show(true)
                                view(R.id.base_bottom_control_layout).showFromBottom()
                            }
                        }
                    }
                }

                override fun getCount(): Int {
                    return allMediaList.size
                }
            }

            setCurrentItem(position, false)

            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    this@RMediaPagerUIView.onPageSelected(position)
                }
            })
        }

        onPageSelected(position)
    }

    private fun onPageSelected(position: Int) {
        this.position = position
        uiTitleBarContainer.getLeftViewById<TextView>(R.id.base_text_view).text = "${position + 1}/${allMediaList.size}"

        mViewHolder.cb(R.id.base_check_button).apply {
            val contains = selectorMediaList.contains(allMediaList[position])
            isChecked = contains

            clickIt {
                if (contains || !checkMaxLimit()) {
                    onSelectorMediaItem(allMediaList[position], !contains)
                } else {
                    isChecked = false
                }
            }
        }
    }
}