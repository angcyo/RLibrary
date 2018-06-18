package com.angcyo.picker.media.uiview

import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.kotlin.clickIt
import com.angcyo.uiview.kotlin.hideFromBottom
import com.angcyo.uiview.kotlin.minValue
import com.angcyo.uiview.kotlin.showFromBottom
import com.angcyo.uiview.model.TitleBarItem
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.RBaseAdapter
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.viewgroup.RFrameLayout
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

    private val mediaSmallPreviewAdapter = MediaSmallPreviewAdapter()
    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()

        view(R.id.base_bottom_control_layout).setBackgroundColor(SkinHelper.getTranColor(titleBarBGColor, 180))

        mViewHolder.pager(R.id.base_view_pager).apply {
            offscreenPageLimit = 2
            adapter = object : RPagerAdapter() {
                override fun getLayoutId(position: Int): Int {
                    return R.layout.pager_image_layout
                }

                override fun initItemView(rootView: View, position: Int) {
                    super.initItemView(rootView, position)

                    rootView.findViewById<ImageView>(R.id.base_image_view).apply {
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

        mViewHolder.rv(R.id.base_recycler_view).adapter = mediaSmallPreviewAdapter
        mViewHolder.visible(R.id.base_recycler_view, !RUtils.isListEmpty(selectorMediaList))

        onPageSelected(position)
    }

    private fun onPageSelected(position: Int) {
        this.position = position
        uiTitleBarContainer.getLeftViewById<TextView>(R.id.base_text_view).text = "${position + 1}/${allMediaList.size}"

        mViewHolder.cb(R.id.base_check_button).apply {
            isChecked = selectorMediaList.contains(allMediaList[position])

            clickIt {
                val contains = selectorMediaList.contains(allMediaList[position])
                if (contains || !checkMaxLimit()) {
                    onSelectorMediaItem(allMediaList[position], !contains)
                } else {
                    isChecked = false
                }
            }
        }

        mediaSmallPreviewAdapter.updateAllItem()
    }

    override fun onSelectorMediaItem(mediaItem: MediaItem, selector: Boolean) {
        super.onSelectorMediaItem(mediaItem, selector)

        mediaSmallPreviewAdapter.resetData(selectorMediaList)
        mViewHolder.visible(R.id.base_recycler_view, !RUtils.isListEmpty(selectorMediaList))
    }

    inner class MediaSmallPreviewAdapter : RBaseAdapter<MediaItem>(mActivity, selectorMediaList) {
        override fun getItemLayoutId(viewType: Int): Int {
            return R.layout.base_item_media_small_preview_layout
        }

        override fun onBindView(holder: RBaseViewHolder, position: Int, bean: MediaItem?) {
            bean?.let {
                holder.giv(R.id.base_image_view).apply {
                    reset()
                    url = bean.path
                }

                if (holder.itemView is RFrameLayout) {
                    holder.itemView.innerBorderColor = SkinHelper.getSkin().themeSubColor
                    holder.itemView.showInnerBorder = bean == allMediaList[this@RMediaPagerUIView.position]
                }

                holder.clickItem {
                    val indexOf = allMediaList.indexOf(bean).minValue(0)
                    val preIndex = if (indexOf == 0) {
                        indexOf + 1
                    } else {
                        indexOf - 1
                    }

                    mViewHolder.rpager(R.id.base_view_pager).apply {
                        //在没有动画滚动的情况下, 直接滚动到指定位置会出现 黑屏的BUG, 所以先滚动到邻居的位置,再滚动到真实的位置
                        if ((adapter as RPagerAdapter).getCacheView(indexOf) == null) {
                            setCurrentItem(preIndex, false)
                            postDelayed(16) {
                                setCurrentItem(indexOf, false)
                            }
                        } else {
                            setCurrentItem(indexOf, false)
                        }
                    }
                }
            }
        }
    }
}