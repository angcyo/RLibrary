package com.angcyo.picker.media.uiview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.angcyo.picker.media.RMediaLoader
import com.angcyo.picker.media.TakeUtils
import com.angcyo.picker.media.ThumbLoad
import com.angcyo.picker.media.bean.MediaFolder
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.picker.media.widget.NumCheckView
import com.angcyo.uiview.BuildConfig
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

    override fun onSelectorButtonClick() {
        super.onSelectorButtonClick()
        if (RUtils.isListEmpty(selectorMediaList)) {
            finishIView()
        } else {
            finishIView {
                onMediaSelectorObserver?.onMediaSelector(selectorMediaList)
            }
        }
    }

    private lateinit var mediaAdapter: MediaAdapter
    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()

        mediaAdapter = MediaAdapter()
        mViewHolder.rv(R.id.base_recycler_view).adapter = mediaAdapter
        view(R.id.base_bottom_control_layout).setBackgroundColor(titleBarBGColor)

        click(R.id.base_preview_selector) {
            if (RUtils.isListEmpty(selectorMediaList)) {
            } else {
                startMediaPager(selectorMediaList.filter { true } as MutableList<MediaItem>, selectorMediaList)
            }
        }
    }

    private fun startMediaPager(allMediaList: MutableList<MediaItem> /*总共的媒体*/,
                                selectorMediaList: MutableList<MediaItem> /*选中的媒体*/,
                                position: Int = 0 /*总媒体中的索引*/) {
        startIView(RMediaPagerUIView(mediaLoaderConfig, allMediaList, selectorMediaList, position).apply {
            this.onMediaSelectorObserver = this@RMediaLoaderUIView.onMediaSelectorObserver

            onIViewUnload = Runnable {
                updateNumCheck(true)
                updatePreviewText()
                updateSendButtonText()
            }

            onImageEditObserver = { mediaItem ->
                onMediaItemAdd(mediaItem)
            }
        })
    }

    /**添加一个新的item到视图*/
    private fun onMediaItemAdd(mediaItem: MediaItem, addInFirst: Boolean = false) {
        folderList?.forEach {
            if (folderList!![curFolderPosition] != it) {

                if (mediaItem.isImageItem()) {
                    if (it.folderPath == MediaLoaderConfig.FOLDER_PATH_IMAGE ||
                            it.folderPath == MediaLoaderConfig.FOLDER_PATH_ALL ||
                            it.folderPath == MediaLoaderConfig.FOLDER_PATH_IMAGE_VIDEO) {
                        if (addInFirst) {
                            it.mediaItemList.add(0, mediaItem)
                        } else {
                            it.mediaItemList.add(mediaItem)
                        }
                    }
                } else {
                    if (it.folderPath == MediaLoaderConfig.FOLDER_PATH_VIDEO ||
                            it.folderPath == MediaLoaderConfig.FOLDER_PATH_ALL ||
                            it.folderPath == MediaLoaderConfig.FOLDER_PATH_IMAGE_VIDEO) {
                        if (addInFirst) {
                            it.mediaItemList.add(0, mediaItem)
                        } else {
                            it.mediaItemList.add(mediaItem)
                        }
                    }
                }
            }
        }
        if (addInFirst) {
            mediaAdapter.notifyItemInsertedAndUpdate(mediaAdapter.headerCount)
        } else {
            mediaAdapter.notifyItemInsertedAndUpdate(mediaAdapter.headerCount + mediaAdapter.dataCount - 1)
        }
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

            //之前拍照的item
            takeFileMediaItem?.let {
                folderList!![curFolderPosition].mediaItemList.add(0, it)
                onMediaItemAdd(it, true)
            }

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

    private fun resetScanMedia() {
        (mActivity as FragmentActivity).supportLoaderManager.destroyLoader(mediaLoaderConfig.mediaLoaderType)
        postDelayed(160) {
            onLoadContentViewAfter()
        }
    }

    /**选择文件夹*/
    private fun onFolderSelector(position: Int) {
        if (RUtils.listSize(folderList) <= position) {

        } else {
            val mediaFolder = folderList?.get(position)
            mediaFolder?.let {
                if (mediaLoaderConfig.enableCamera &&
                        (it.folderPath == MediaLoaderConfig.FOLDER_PATH_IMAGE ||
                                it.folderPath == MediaLoaderConfig.FOLDER_PATH_VIDEO ||
                                it.folderPath == MediaLoaderConfig.FOLDER_PATH_ALL ||
                                it.folderPath == MediaLoaderConfig.FOLDER_PATH_IMAGE_VIDEO)) {
                    mediaAdapter.resetHeaderData("enableCamera")
                } else {
                    mediaAdapter.resetHeaderData(mutableListOf())
                }

                curFolderPosition = position
                tv(R.id.base_folder_selector).text = it.folderName
                mediaAdapter.resetData(it.mediaItemList)
            }
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

    /**拍照, 录制之后, 重新扫描一下*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            takeFileMediaItem?.let {
                RUtils.scanFile(mActivity, it.path)
            }
        } else {
            takeFileMediaItem = null
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

    /**拍摄的文件路径*/
    private var takeFileMediaItem: MediaItem? = null

    inner class MediaAdapter : RExBaseAdapter<String, MediaItem, String>(mActivity) {
        override fun getItemLayoutId(viewType: Int): Int {
            if (isHeaderItemType(viewType)) {
                return R.layout.base_item_camera_preview
            }
            return R.layout.base_item_media_layout
        }

        override fun onBindHeaderView(holder: RBaseViewHolder, posInHeader: Int, headerBean: String?) {
            super.onBindHeaderView(holder, posInHeader, headerBean)
            //摄像头, 拍照item
            if (mediaLoaderConfig.mediaLoaderType == MediaLoaderConfig.LOADER_TYPE_VIDEO ||
                    folderList!![curFolderPosition].folderPath == MediaLoaderConfig.FOLDER_PATH_VIDEO) {
                holder.tv(R.id.base_text_view).text = "拍摄视频"
                holder.clickItem {
                    TakeUtils.recordVideo(mActivity, MediaLoaderConfig.LOADER_TYPE_VIDEO)?.let {
                        takeFileMediaItem = MediaItem().apply {
                            path = it.absolutePath
                            displayName = it.name
                            mimeType = "video/mp4"
                        }
                    }
                }
            } else {
                holder.tv(R.id.base_text_view).text = "拍摄照片"
                holder.clickItem {
                    TakeUtils.takePicture(mActivity, MediaLoaderConfig.LOADER_TYPE_IMAGE)?.let {
                        takeFileMediaItem = MediaItem().apply {
                            path = it.absolutePath
                            displayName = it.name
                            mimeType = "image/png"
                        }
                    }
                }
            }
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
                        if (isChecked()) {
                            //取消选中
                            onSelectorMediaItem(bean, false)
                            holder.giv(R.id.base_image_view).mDrawMaskColor.drawMaskColorShow = true
                        } else if (checkMaxLimit() || !mediaLoaderConfig.canSelectorFile(bean.size)) {
                            //不能选中
                            holder.giv(R.id.base_image_view).mDrawMaskColor.drawMaskColorShow = false
                        } else {
                            //选中
                            onSelectorMediaItem(bean, true)
                            holder.giv(R.id.base_image_view).mDrawMaskColor.drawMaskColorShow = true
                        }

                        updateNumCheck()
                    }
                }

                if (BuildConfig.DEBUG || mediaLoaderConfig.limitFileSizeModel == MediaLoaderConfig.SIZE_MODEL_SELECTOR) {
                    holder.visible(R.id.base_file_size_view)
                    holder.tv(R.id.base_file_size_view).text = RUtils.formatFileSize(bean.size) /*+ "\n${bean.size}"*/
                }
            }
        }
    }

}