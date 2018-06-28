package com.angcyo.picker.media.uiview

import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.container.UIParam
import com.angcyo.uiview.kotlin.*
import com.angcyo.uiview.model.TitleBarItem
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.RBaseAdapter
import com.angcyo.uiview.resources.AnimUtil
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.RPlayer
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.viewgroup.RFrameLayout
import com.angcyo.uiview.widget.viewpager.RPagerAdapter
import com.bumptech.glide.Glide

/**
 * Created by angcyo on 2018/06/18 07:13
 * 媒体大图预览选择界面
 */
class RMediaPagerUIView(mediaLoaderConfig: MediaLoaderConfig,
                        val allMediaList: MutableList<MediaItem> /*总共的媒体*/,
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

    override fun onSelectorButtonClick() {
        super.onSelectorButtonClick()
        if (RUtils.isListEmpty(selectorMediaList)) {
            finishIView()
        } else {
            mParentILayout.finishIView(RMediaLoaderUIView::class.java)
            finishIView {
                onMediaSelectorObserver?.onMediaSelector(selectorMediaList)
            }
        }
    }

    private lateinit var mediaSmallPreviewAdapter: MediaSmallPreviewAdapter
    private lateinit var mediaPagerAdapter: MediaPagerAdapter
    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()
        mediaSmallPreviewAdapter = MediaSmallPreviewAdapter()
        mediaPagerAdapter = MediaPagerAdapter()

        view(R.id.base_bottom_control_layout).setBackgroundColor(SkinHelper.getTranColor(titleBarBGColor, 180))

        mViewHolder.pager(R.id.base_view_pager).apply {
            offscreenPageLimit = 2
            adapter = mediaPagerAdapter
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

        //小图预览自动滚动到选中的那个item
        selectorMediaList.indexOf(allMediaList[position]).let {
            if (it > -1) {
                post {
                    mViewHolder.rv(R.id.base_recycler_view).smoothScrollToPosition(it)
                }
            }
        }

        if (!mediaLoaderConfig.isMultiModel()) {
            //大图界面放出 发送 按钮
            uiTitleBarContainer.getRightViewById<View>(R.id.base_send_button).visibility = View.VISIBLE

            //隐藏 选择 按钮
            mViewHolder.invisible(R.id.base_check_button)
        }
    }

    override fun checkSendObserver() {
        if (!mediaLoaderConfig.isMultiModel()) {
            selectorMediaList.add(allMediaList[position])
        }
        super.checkSendObserver()
    }

    /**图片编辑完成*/
    var onImageEditObserver: ((MediaItem) -> Unit)? = null

    private fun onPageSelected(position: Int) {
        this.position = position
        uiTitleBarContainer.getLeftViewById<TextView>(R.id.base_text_view).text = "${position + 1}/${allMediaList.size}"

        mViewHolder.cb(R.id.base_check_button).apply {
            isChecked = selectorMediaList.contains(allMediaList[position])

            clickIt {
                val contains = selectorMediaList.contains(allMediaList[position])
                if (contains) {
                    //取消选择
                    onSelectorMediaItem(allMediaList[position], false)
                } else if (!canSelectorItem(allMediaList[position])) {
                    //不能选中
                    isChecked = false
                } else {
                    //选中
                    onSelectorMediaItem(allMediaList[position], true)
                }
            }
        }

        mediaSmallPreviewAdapter.updateAllItem()

        if (mediaLoaderConfig.enableImageEdit) {
            mViewHolder.invisible(R.id.base_edit_media, mediaPagerAdapter.getItemType(position) == MediaLoaderConfig.LOADER_TYPE_IMAGE)
            click(R.id.base_edit_media) {
                startIView(UCropEditUIView(mediaLoaderConfig, allMediaList[position]).apply {
                    this.onImageEditObserver = {
                        //编辑之后产生的新图片
                        allMediaList.add(it)
                        mediaPagerAdapter.notifyDataSetChanged()
                        mViewHolder.rpager(R.id.base_view_pager).currentItem = allMediaList.size - 1

                        this@RMediaPagerUIView.onImageEditObserver?.invoke(it)
                    }
                })
            }
        } else {
            mViewHolder.invisible(R.id.base_edit_media)
        }
    }

    override fun onSelectorMediaItem(mediaItem: MediaItem, selector: Boolean) {
        super.onSelectorMediaItem(mediaItem, selector)

        mediaSmallPreviewAdapter.resetData(selectorMediaList)
        mViewHolder.visible(R.id.base_recycler_view, !RUtils.isListEmpty(selectorMediaList))

        if (selector) {
            //小图预览自动滚动到选中的那个item
            selectorMediaList.indexOf(mediaItem).let {
                if (it > -1) {
                    post {
                        mViewHolder.rv(R.id.base_recycler_view).smoothScrollToPosition(it)
                    }
                }
            }
        }
    }

    override fun onViewUnload(uiParam: UIParam) {
        super.onViewUnload(uiParam)
        mediaPagerAdapter.releaseVideo()
        mediaPagerAdapter.releaseAudio()
    }

    /**选中 小图预览*/
    inner class MediaSmallPreviewAdapter : RBaseAdapter<MediaItem>(mActivity, selectorMediaList) {
        override fun getItemLayoutId(viewType: Int): Int {
            return R.layout.base_item_media_small_preview_layout
        }

        override fun onBindView(holder: RBaseViewHolder, position: Int, bean: MediaItem?) {
            bean?.let {
                holder.giv(R.id.base_image_view).apply {
                    reset()

                    BaseMediaUIView.loadImageView(this, bean)
                }

                if (holder.itemView is RFrameLayout) {
                    holder.itemView.innerBorderColor = SkinHelper.getSkin().themeSubColor
                    if (bean == allMediaList[this@RMediaPagerUIView.position]) {
                        holder.itemView.showInnerBorder = true
                        mViewHolder.rv(R.id.base_recycler_view).smoothScrollToPosition(position)
                    } else {
                        holder.itemView.showInnerBorder = false
                    }
                }

                holder.clickItem {

                    val rViewPager = mViewHolder.rpager(R.id.base_view_pager)
                    val rPagerAdapter: RPagerAdapter = rViewPager.adapter as RPagerAdapter

                    val indexOf = allMediaList.indexOf(bean).minValue(0)
                    val preIndex = if (indexOf == 0) {
                        indexOf + 1
                    } else if (indexOf == allMediaList.size - 1) {
                        indexOf - 1
                    } else {
                        //取相同type的索引item
                        val itemType = rPagerAdapter.getItemType(indexOf)
                        val itemTypeNext = rPagerAdapter.getItemType(indexOf + 1)
                        if (itemType == itemTypeNext) {
                            indexOf + 1
                        } else {
                            indexOf - 1
                        }
                    }

                    rViewPager.apply {
                        //在没有动画滚动的情况下, 直接滚动到指定位置会出现 黑屏的BUG, 所以先滚动到邻居的位置,再滚动到真实的位置
                        if (rPagerAdapter.getCacheView(indexOf) == null) {
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

    /**大图预览*/
    inner class MediaPagerAdapter : RPagerAdapter() {

        private var playVideoView: VideoView? = null
        private var animView: View? = null
        private var playPosition = -1

        private val rPlayer = RPlayer()

        override fun getItemType(position: Int): Int {
            val mediaItem = allMediaList[position]
            return when {
                mediaItem.mimeType.isAudioMimeType() -> {
                    MediaLoaderConfig.LOADER_TYPE_AUDIO
                }
                mediaItem.mimeType.isVideoMimeType() -> {
                    MediaLoaderConfig.LOADER_TYPE_VIDEO
                }
                else -> {
                    MediaLoaderConfig.LOADER_TYPE_IMAGE
                }
            }
        }

        override fun getLayoutId(position: Int, itemType: Int): Int {
            return when (itemType) {
                MediaLoaderConfig.LOADER_TYPE_AUDIO -> {
                    R.layout.pager_audio_layout
                }
                MediaLoaderConfig.LOADER_TYPE_VIDEO -> {
                    R.layout.pager_video_layout
                }
                else -> {
                    R.layout.pager_image_layout
                }
            }
        }

        override fun initItemView(rootView: View, position: Int, itemType: Int) {
            super.initItemView(rootView, position, itemType)

            val mediaItem = allMediaList[position]

            when (itemType) {
                MediaLoaderConfig.LOADER_TYPE_AUDIO -> {
                    rootView.findViewById<ImageView>(R.id.base_play_view).apply {
                        clickIt {
                            //RUtils.openFile(mActivity, File(mediaItem.path))

                            if (rPlayer.isPlaying()) {
                                playPosition = -1
                                animView = null
                                it.clearAnimation()
                                rPlayer.pausePlay()
                            } else {
                                playPosition = position
                                it.startAnimation(AnimUtil.rotateAnimation(true))
                                animView = it
                                rPlayer.startPlay(mediaItem.path)
                            }
                        }
                    }
                    rootView.findViewById<TextView>(R.id.base_time_view).text = mediaItem.duration.toHHmmss()
                    rootView.findViewById<TextView>(R.id.base_name_view).text = mediaItem.displayName
                }
                MediaLoaderConfig.LOADER_TYPE_VIDEO -> {
                    rootView.findViewById<TextView>(R.id.base_time_view).text = mediaItem.duration.toHHmmss()
                    rootView.findViewById<TextView>(R.id.base_name_view).text = mediaItem.displayName

                    val playView = rootView.findViewById<ImageView>(R.id.base_play_view)
                    val videoView = rootView.findViewById<VideoView>(R.id.base_video_view)
                    val clickView = rootView.findViewById<View>(R.id.base_click_view)

                    fun playEnd() {
                        playView.visibility = View.VISIBLE

                        playVideoView = null
                        playPosition = -1

                        uiTitleBarContainer.show(true)
                        view(R.id.base_bottom_control_layout).showFromBottom()
                    }

                    fun playStart() {
                        playVideoView = videoView
                        playPosition = position

                        videoView.start()
                        playView.visibility = View.INVISIBLE

                        uiTitleBarContainer.hide(true)
                        view(R.id.base_bottom_control_layout).hideFromBottom()
                    }

                    videoView.apply {
                        setVideoPath(mediaItem.path)
                        setOnPreparedListener {
                            start()
                            post { pause() }
                        }
                        setOnCompletionListener {
                            playEnd()
                        }
                    }

                    clickView.clickIt {
                        if (videoView.isPlaying) {
                            videoView.pause()
                            playEnd()
                        } else {
                            playStart()
                        }
                    }
                }
                else -> {
                    rootView.findViewById<ImageView>(R.id.base_image_view).apply {
                        Glide.with(mActivity).load(mediaItem.path).into(this)

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
            }
        }

        override fun onItemDestroy(rootView: View, itemType: Int) {
            super.onItemDestroy(rootView, itemType)
            if (itemType == MediaLoaderConfig.LOADER_TYPE_VIDEO) {
                releaseVideo()
            } else if (itemType == MediaLoaderConfig.LOADER_TYPE_AUDIO) {
                releaseAudio()
            }
        }

        override fun getCount(): Int {
            return allMediaList.size
        }

        fun releaseVideo() {
            playVideoView?.stopPlayback()
            playVideoView = null
            playPosition = -1
        }

        fun releaseAudio() {
            animView?.clearAnimation()
            rPlayer.pausePlay()
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (playPosition != position) {
                releaseVideo()

                releaseAudio()

                uiTitleBarContainer.show(true)
                view(R.id.base_bottom_control_layout).showFromBottom()
            }
        }
    }
}