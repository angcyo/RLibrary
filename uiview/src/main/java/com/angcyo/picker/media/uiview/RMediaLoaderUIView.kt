package com.angcyo.picker.media.uiview

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.angcyo.picker.media.RMediaLoader
import com.angcyo.picker.media.ThumbLoad
import com.angcyo.picker.media.bean.MediaFolder
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.picker.media.widget.NumCheckView
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.kotlin.clickIt
import com.angcyo.uiview.kotlin.isAudioMimeType
import com.angcyo.uiview.kotlin.isVideoMimeType
import com.angcyo.uiview.kotlin.toHHmmss
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.RExBaseAdapter
import com.angcyo.uiview.recycler.adapter.RExBaseAdapter.localRefresh
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.widget.RTextView
import java.lang.ref.WeakReference

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：媒体选择界面
 * 创建人员：Robi
 * 创建时间：2018/06/15 16:08
 * 修改人员：Robi
 * 修改时间：2018/06/15 16:08
 * 修改备注：
 * Version: 1.0.0
 */
class RMediaLoaderUIView : BaseMediaUIView() {

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

    val mediaAdapter = MediaAdapter()
    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()
        mViewHolder.rv(R.id.base_recycler_view).adapter = mediaAdapter
        view(R.id.base_bottom_control_layout).setBackgroundColor(titleBarBGColor)

        click(R.id.base_preview_selector) {
            if (RUtils.isListEmpty(selectorMediaList)) {
            } else {
                startMediaPager(selectorMediaList.filter { true }, selectorMediaList)
            }
        }
    }

    private fun startMediaPager(allMediaList: List<MediaItem> /*总共的媒体*/,
                                selectorMediaList: MutableList<MediaItem> /*选中的媒体*/,
                                position: Int = 0 /*总媒体中的索引*/) {
        startIView(RMediaPagerUIView(mediaLoaderConfig, allMediaList, selectorMediaList, position).apply {
            onIViewUnload = Runnable {
                updateNumCheck(true)
                updatePreviewText()
                updateSendButtonText()
            }
        })
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

    override fun onSelectorMediaItem(mediaItem: MediaItem, selector: Boolean) {
        super.onSelectorMediaItem(mediaItem, selector)
        updatePreviewText()
    }

    private fun updatePreviewText() {
        if (RUtils.isListEmpty(selectorMediaList)) {
            tv(R.id.base_preview_selector).text = "预览"
        } else {
            tv(R.id.base_preview_selector).text = "预览(${RUtils.listSize(selectorMediaList)})"
        }
    }

    /**更新数字*/
    private fun updateNumCheck(updateMaskColor: Boolean = false) {
        localRefresh(mViewHolder.rv(R.id.base_recycler_view)) { viewHolder, position ->
            viewHolder?.let {
                val numCheckView: NumCheckView? = viewHolder.v(R.id.base_num_check_view)

                numCheckView?.let {
                    it.setNum(selectorMediaList.indexOf(mediaAdapter.getDataByIndex(position)) + 1)

                    if (updateMaskColor) {
                        viewHolder.giv(R.id.base_image_view).mDrawMaskColor.drawMaskColorShow = it.isChecked()
                    }
                }
            }
        }
    }

    inner class MediaAdapter : RExBaseAdapter<String, MediaItem, String>(mActivity) {
        override fun getItemLayoutId(viewType: Int): Int {
            if (isHeaderItemType(viewType)) {

            }
            return R.layout.base_item_media_layout
        }

        override fun onBindHeaderView(holder: RBaseViewHolder, posInHeader: Int, headerBean: String?) {
            super.onBindHeaderView(holder, posInHeader, headerBean)
            //摄像头, 拍照item

        }

        override fun onBindDataView(holder: RBaseViewHolder, posInData: Int, bean: MediaItem?) {
            bean?.let {
                //L.e("call: onBindView -> ${bean.path}")

                if (isIViewShowOver) {
                    //加载完全显示后, 加载图片耗时操作
                    holder.giv(R.id.base_image_view).apply {
                        loadImageView(this, bean)
                        if (bean.mimeType.isVideoMimeType()) {
                            //创建视频缩略图
                            ThumbLoad.createThumbFile(WeakReference(mActivity), WeakReference(this@MediaAdapter), bean)
                        }

                        //蒙层
                        mDrawMaskColor.drawMaskColorShow = selectorMediaList.indexOf(bean) > -1
                    }
                }

                holder.click(R.id.base_image_view) {
                    if (mediaLoaderConfig.selectorModel == MediaLoaderConfig.SELECTOR_MODEL_MULTI) {
                        startMediaPager(allDatas, selectorMediaList, posInData)
                    } else {
                        selectorMediaList.add(bean)
                        onSelectorButtonClick()
                    }
                }

                //时长
                holder.visible(R.id.base_video_time_view, bean.mimeType.isVideoMimeType() || bean.mimeType.isAudioMimeType())
                holder.tv(R.id.base_video_time_view).apply {
                    RTextView.setLeftIco(this, if (bean.mimeType.isAudioMimeType()) R.drawable.base_audio_ico else R.drawable.base_video_icon)
                    text = bean.duration.toHHmmss()
                }

                val numCheckView: NumCheckView = holder.v(R.id.base_num_check_view)
                numCheckView.apply {
                    setNum(selectorMediaList.indexOf(bean) + 1)

                    clickIt {
                        if (isChecked() || !checkMaxLimit()) {
                            onSelectorMediaItem(bean, !isChecked())
                            holder.giv(R.id.base_image_view).mDrawMaskColor.drawMaskColorShow = !isChecked()

                            updateNumCheck()
                        }
                    }
                }
            }
        }
    }

}