package com.angcyo.picker.media.uiview

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.angcyo.library.utils.L
import com.angcyo.picker.media.RMediaLoader
import com.angcyo.picker.media.bean.MediaFolder
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIBaseView
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.kotlin.isAudioMimeType
import com.angcyo.uiview.kotlin.isVideoMimeType
import com.angcyo.uiview.kotlin.toHHmmss
import com.angcyo.uiview.recycler.RBaseItemDecoration
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.RBaseAdapter
import com.angcyo.uiview.utils.RUtils

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
        return Color.parseColor("#303030")
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
        mViewHolder.rv(R.id.base_recycler_view).apply {
            adapter = mediaAdapter
            addItemDecoration(RBaseItemDecoration(2 * density().toInt()).apply {
                setDrawLastLine(true)
                setDrawFirstLine(true)
                setColor(Color.TRANSPARENT)
            })
        }
        view(R.id.base_bottom_control_layout).setBackgroundColor(titleBarBGColor)
    }

    override fun onViewShowFirst(bundle: Bundle?) {
        super.onViewShowFirst(bundle)
        onFolderSelector(0)
    }

    //文件夹列表
    var folderList: MutableList<MediaFolder>? = null

    override fun onViewLoad() {
        super.onViewLoad()
        RMediaLoader(mActivity as FragmentActivity, mediaLoaderConfig) {
            folderList = it
            onFolderSelector(0)
        }.startLoadMedia()
    }

    /**选择文件夹*/
    private fun onFolderSelector(position: Int) {
        if (RUtils.listSize(folderList) <= position) {

        } else {
            tv(R.id.base_folder_selector).text = folderList?.first()?.folderName
            mediaAdapter.resetData(folderList?.first()?.mediaItemList)
        }
    }

    inner class MediaAdapter : RBaseAdapter<MediaItem>(mActivity) {
        override fun getItemLayoutId(viewType: Int): Int {
            return R.layout.base_item_media_layout
        }

        override fun onBindView(holder: RBaseViewHolder, position: Int, bean: MediaItem?) {
            bean?.let {
                L.e("call: onBindView -> ${bean.path}")

                if (isIViewShowOver) {
                    holder.giv(R.id.base_image_view).apply {
                        reset()
                        url = bean.path
                    }
                }

                holder.visible(R.id.base_video_time_view, bean.mimeType.isVideoMimeType() || bean.mimeType.isAudioMimeType())
                holder.tv(R.id.base_video_time_view).text = bean.duration.toHHmmss() + if (bean.mimeType.isAudioMimeType()) "MP3" else ""
            }
        }

    }

}