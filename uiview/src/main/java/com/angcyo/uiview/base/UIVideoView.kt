package com.angcyo.uiview.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.resources.AnimUtil

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：简单的视频播放按钮
 * 创建人员：Robi
 * 创建时间：2017/07/14 10:36
 * 修改人员：Robi
 * 修改时间：2017/07/14 10:36
 * 修改备注：
 * Version: 1.0.0
 */
open class UIVideoView : UIContentView() {

    lateinit var videoView: VideoView
    lateinit var videoPlayView: ImageView
    lateinit var videoRootLayout: ViewGroup

    /**需要播放的视频地址*/
    var videoPath: String? = null

    /**界面显示, 是否自动播放*/
    var autoPlay = true

    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar()
                .setFloating(true)
                .setShowBackImageView(true)
                .setTitleString("")
    }

    override fun getDefaultBackgroundColor(): Int {
        return Color.BLACK
    }

    override fun inflateContentLayout(baseContentLayout: ContentLayout?, inflater: LayoutInflater?) {
        inflate(R.layout.base_video_layout)
    }

    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()
        videoView = mViewHolder.v(R.id.base_video_view)
        videoPlayView = mViewHolder.v(R.id.base_play_view)
        videoRootLayout = mViewHolder.v(R.id.base_video_root_layout)

        videoView.setOnCompletionListener {
            pauseVideo()
        }

        videoView.setOnErrorListener { _, _, _ ->
            pauseVideo()
            return@setOnErrorListener false
        }

        videoRootLayout.setOnClickListener {
            if (isPlaying()) {
                pauseVideo()
            } else {
                //playVideo()
            }
        }

        videoPlayView.setOnClickListener {
            playVideo()
        }
    }

    fun pauseVideo() {
        if (isPlaying()) {
            videoView.pause()
        }

        uiTitleBarContainer.show(true)
        videoPlayView.visibility = View.VISIBLE
    }

    fun playVideo() {
        if (!isPlaying()) {
            videoView.start()
        }

        uiTitleBarContainer.hide(true)
        videoPlayView.visibility = View.INVISIBLE
    }

    private fun isPlaying(): Boolean {
        return videoView.isPlaying
    }

    override fun onViewHide() {
        super.onViewHide()
        pauseVideo()
    }

    override fun onViewShow(bundle: Bundle?) {
        super.onViewShow(bundle)
    }

    override fun onViewShowFirst(bundle: Bundle?) {
        super.onViewShowFirst(bundle)
        AnimUtil.startArgb(videoView, Color.BLACK, Color.TRANSPARENT, 200)

        videoPath?.let {
            videoView.setVideoPath(it)

            if (autoPlay) {
                videoView.start()
                postDelayed(200L) {
                    videoView.pause()
                }
            }
        }
    }

    override fun onViewUnload() {
        super.onViewUnload()
        videoView.stopPlayback()
    }
}