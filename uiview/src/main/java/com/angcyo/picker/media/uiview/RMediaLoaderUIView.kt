package com.angcyo.picker.media.uiview

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.widget.ImageView
import com.angcyo.picker.media.RMediaLoader
import com.angcyo.picker.media.ThumbLoad
import com.angcyo.picker.media.bean.MediaFolder
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIBaseView
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.kotlin.isAudioMimeType
import com.angcyo.uiview.kotlin.isVideoMimeType
import com.angcyo.uiview.kotlin.toHHmmss
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.RBaseAdapter
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.widget.RTextView
import java.lang.ref.WeakReference

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/06/15 16:08
 * 修改人员：Robi
 * 修改时间：2018/06/15 16:08
 * 修改备注：
 * Version: 1.0.0
 */
class RMediaLoaderUIView : UIBaseView() {

    override fun getTitleShowString(): String {
        return when (mediaLoaderConfig.mediaLoaderType) {
            MediaLoaderConfig.LOADER_TYPE_IMAGE_VIDEO -> "图片和视频"
            MediaLoaderConfig.LOADER_TYPE_IMAGE -> "选择图片"
            MediaLoaderConfig.LOADER_TYPE_AUDIO -> "选择音频"
            MediaLoaderConfig.LOADER_TYPE_VIDEO -> "选择视频"
            else -> "选择媒体"
        }
    }

    override fun inflateContentLayout(baseContentLayout: ContentLayout, inflater: LayoutInflater) {
        inflate(R.layout.view_media_loader_layout)
    }

    override fun getDefaultLayoutState(): LayoutState {
        return LayoutState.CONTENT
    }

    override fun getTitleBarBGColor(): Int {
        return getColor(R.color.base_wx_dark)
    }

    override fun getDefaultBackgroundColor(): Int {
        return Color.parseColor("#191919")
    }

    /**
     * 配置信息
     * */
    var mediaLoaderConfig = MediaLoaderConfig().apply {
        mediaLoaderType = MediaLoaderConfig.LOADER_TYPE_ALL
    }

    val mediaAdapter = MediaAdapter()
    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()
        mViewHolder.rv(R.id.base_recycler_view).adapter = mediaAdapter
        view(R.id.base_bottom_control_layout).setBackgroundColor(titleBarBGColor)
    }

    override fun onViewShowFirst(bundle: Bundle?) {
        super.onViewShowFirst(bundle)
        onFolderSelector(curFolderPosition)
    }

    //文件夹列表
    var folderList: MutableList<MediaFolder>? = null
    var curFolderPosition = 0

    override fun onLoadContentViewAfter() {
        super.onLoadContentViewAfter()
        RMediaLoader(mActivity as FragmentActivity, mediaLoaderConfig) {
            folderList = it
            onFolderSelector(curFolderPosition)

            if (RUtils.isListEmpty(folderList)) {
                tv(R.id.base_folder_selector).text = "暂无媒体"
            } else {
                click(R.id.base_folder_selector) {
                    startIView(MediaFolderSelectorUIDialog(folderList!!, curFolderPosition) {
                        onFolderSelector(it)
                    })
                }
            }
        }.startLoadMedia()
    }

    /**选择文件夹*/
    private fun onFolderSelector(position: Int) {
        if (RUtils.listSize(folderList) <= position) {

        } else {
            curFolderPosition = position
            tv(R.id.base_folder_selector).text = folderList?.get(position)?.folderName
            mediaAdapter.resetData(folderList?.get(position)?.mediaItemList)
        }
    }

    inner class MediaAdapter : RBaseAdapter<MediaItem>(mActivity) {
        override fun getItemLayoutId(viewType: Int): Int {
            return R.layout.base_item_media_layout
        }

        override fun onBindView(holder: RBaseViewHolder, position: Int, bean: MediaItem?) {
            bean?.let {
                //L.e("call: onBindView -> ${bean.path}")

                if (isIViewShowOver) {
                    //加载完全显示后, 加载图片耗时操作
                    holder.giv(R.id.base_image_view).apply {
                        reset()
                        when {
                            bean.mimeType.isAudioMimeType() -> {
                                scaleType = ImageView.ScaleType.CENTER
                                setImageResource(R.drawable.base_audio_tip_ico)
                            }
                            bean.mimeType.isVideoMimeType() -> {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                url = bean.videoThumbPath

                                //创建视频缩略图
                                ThumbLoad.createThumbFile(WeakReference(mActivity), WeakReference(this@MediaAdapter), bean)
                            }
                            else -> {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                url = bean.path
                            }
                        }
                    }
                }

                //时长
                holder.visible(R.id.base_video_time_view, bean.mimeType.isVideoMimeType() || bean.mimeType.isAudioMimeType())
                holder.tv(R.id.base_video_time_view).apply {
                    RTextView.setLeftIco(this, if (bean.mimeType.isAudioMimeType()) R.drawable.base_audio_ico else R.drawable.base_video_icon)
                    text = bean.duration.toHHmmss()
                }
            }
        }

    }

}