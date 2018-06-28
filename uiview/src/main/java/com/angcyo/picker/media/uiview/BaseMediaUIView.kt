package com.angcyo.picker.media.uiview

import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import com.angcyo.picker.media.OnMediaSelectorObserver
import com.angcyo.picker.media.OnSendButtonClickObserver
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIBaseView
import com.angcyo.uiview.container.ILayout
import com.angcyo.uiview.kotlin.isAudioMimeType
import com.angcyo.uiview.kotlin.isVideoMimeType
import com.angcyo.uiview.model.TitleBarItem
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.utils.T_
import com.angcyo.uiview.view.IViewAnimationType
import com.angcyo.uiview.widget.GlideImageView

/**
 * Created by angcyo on 2018/06/18 07:25
 */
abstract class BaseMediaUIView : UIBaseView() {

    companion object {
        fun loadImageView(imageView: GlideImageView, mediaItem: MediaItem) {
            imageView.apply {
                reset()
                when {
                    mediaItem.mimeType.isAudioMimeType() -> {
                        scaleType = ImageView.ScaleType.CENTER
                        setImageResource(R.drawable.base_audio_tip_ico)
                    }
                    mediaItem.mimeType.isVideoMimeType() -> {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        url = mediaItem.videoThumbPath
                    }
                    else -> {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        url = mediaItem.path
                    }
                }
            }
        }

        fun finish(iLayout: ILayout) {
            iLayout.finishIView(RMediaLoaderUIView::class.java)
            iLayout.finishIView(RMediaPagerUIView::class.java)
        }
    }


    /**回调监听*/
    var onMediaSelectorObserver: OnMediaSelectorObserver? = null
    var onSendButtonClickObserver: OnSendButtonClickObserver? = null

    //选中的媒体列表
    protected var selectorMediaList = mutableListOf<MediaItem>()

    /**
     * 配置信息
     * */
    var mediaLoaderConfig = MediaLoaderConfig().apply {
        mediaLoaderType = MediaLoaderConfig.LOADER_TYPE_ALL
    }

    init {
        mAnimationType = IViewAnimationType.TRANSLATE_VERTICAL
    }

    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar()
                .addRightItem(TitleBarItem.build("发送") {
                    if (!RUtils.isListEmpty(selectorMediaList)) {

                        if (onSendButtonClickObserver?.onSendButtonClick(this, selectorMediaList) == true) {
                        } else {
                            onSelectorButtonClick()
                        }
                    }
                }.setId(R.id.base_send_button)
                        .setTextColor(SkinHelper.getSkin().themeSubColor)
                        .setTextSize(12 * density()))
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

    override fun onShowContentLayout() {
        super.onShowContentLayout()
        mViewHolder.visible(R.id.base_origin_box, mediaLoaderConfig.showOriginButton)
        updateSendButtonText()
    }

    /**检查是否已达最大选择数量*/
    protected fun checkMaxLimit(): Boolean {
        if (RUtils.listSize(selectorMediaList) >= mediaLoaderConfig.maxSelectorLimit) {
            T_.error("最多选择${mediaLoaderConfig.maxSelectorLimit}个")
            return true
        }
        return false
    }

    /**是否可以选中*/
    protected fun canSelectorItem(item: MediaItem): Boolean {
        if (mediaLoaderConfig.mixSelectorModel == MediaLoaderConfig.LOADER_TYPE_ALL) {
            if (checkMaxLimit()) {
                //已达最大上限
                return false
            }
        }
        if (!mediaLoaderConfig.canSelectorFile(item.size)) {
            //文件大小受限
            return false
        }
        if (mediaLoaderConfig.mixSelectorModel != MediaLoaderConfig.LOADER_TYPE_ALL) {
            if (RUtils.listSize(selectorMediaList) > 0) {
                val firstMediaItem = selectorMediaList[0]
                if (firstMediaItem.isMimeTypeEqu(item)) {
                    if (firstMediaItem.isVideoItem() && mediaLoaderConfig.mixSelectorModel != MediaLoaderConfig.LOADER_TYPE_VIDEO) {
                        if (selectorMediaList.size >= mediaLoaderConfig.maxSelectorVideoLimit) {
                            T_.error("只能选择${mediaLoaderConfig.maxSelectorVideoLimit}个视频")
                            return false
                        }

                    } else if (firstMediaItem.isAudioItem() && mediaLoaderConfig.mixSelectorModel != MediaLoaderConfig.LOADER_TYPE_AUDIO) {
                        if (selectorMediaList.size >= mediaLoaderConfig.maxSelectorAudioLimit) {
                            T_.error("只能选择${mediaLoaderConfig.maxSelectorAudioLimit}个音频")
                            return false
                        }
                    } else if (checkMaxLimit()) {
                        //已达最大上限
                        return false
                    }
                } else {
                    T_.error("不能同时选择视频和图片")
                    return false
                }
            }
        }
        return true
    }

    open protected fun onSelectorMediaItem(mediaItem: MediaItem, selector: Boolean = true) {
        if (selector) {
            if (selectorMediaList.contains(mediaItem)) {

            } else {
                selectorMediaList.add(mediaItem)
            }
        } else {
            selectorMediaList.remove(mediaItem)
        }

        updateSendButtonText()
    }

    protected fun updateSendButtonText() {
        if (RUtils.isListEmpty(selectorMediaList)) {
            uiTitleBarContainer.getRightViewById<TextView>(R.id.base_send_button).text = "发送"
        } else {
            val maxSelectorLimit = if (mediaLoaderConfig.mixSelectorModel == MediaLoaderConfig.LOADER_TYPE_ALL) {
                mediaLoaderConfig.maxSelectorLimit
            } else {
                val firstMediaItem = selectorMediaList[0]
                if (firstMediaItem.isVideoItem()) {
                    mediaLoaderConfig.maxSelectorVideoLimit
                } else if (firstMediaItem.isAudioItem()) {
                    mediaLoaderConfig.maxSelectorAudioLimit
                } else {
                    mediaLoaderConfig.maxSelectorLimit
                }
            }
            uiTitleBarContainer.getRightViewById<TextView>(R.id.base_send_button).text = "发送(${RUtils.listSize(selectorMediaList)}/$maxSelectorLimit)"
        }
    }

    open protected fun onSelectorButtonClick() {

    }
}