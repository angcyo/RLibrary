package com.angcyo.picker.media.uiview

import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIBaseView
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
    }

    init {
        mAnimationType = IViewAnimationType.TRANSLATE_VERTICAL
    }

    //选中的媒体列表
    protected var selectorMediaList = mutableListOf<MediaItem>()

    /**
     * 配置信息
     * */
    var mediaLoaderConfig = MediaLoaderConfig().apply {
        mediaLoaderType = MediaLoaderConfig.LOADER_TYPE_ALL
    }

    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar()
                .addRightItem(TitleBarItem.build("发送") {
                    if (!RUtils.isListEmpty(selectorMediaList)) {
                        onSelectorButtonClick()
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
            uiTitleBarContainer.getRightViewById<TextView>(R.id.base_send_button).text = "发送(${RUtils.listSize(selectorMediaList)}/${mediaLoaderConfig.maxSelectorLimit})"
        }
    }

    protected fun onSelectorButtonClick() {

    }
}