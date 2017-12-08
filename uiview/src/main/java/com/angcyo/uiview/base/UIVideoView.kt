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
open class UIVideoView(var videoPath: String? = null /*需要播放的视频地址*/) : UIContentView() {

    lateinit var videoView: VideoView
    lateinit var videoPlayView: ImageView
    lateinit var videoRootLayout: ViewGroup

    /**界面显示, 是否自动播放*/
    var autoPlay = false
    /**是否循环播放*/
    var loopPlay = false

    var listener: OnVideoPlayListener? = null

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
            if (loopPlay) {
                videoView.start()
            } else {
                pauseVideo()
            }
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

        onPlayEnd()
    }

    fun playVideo() {
        if (!isPlaying()) {
            videoView.start()
        }

        uiTitleBarContainer.hide(true)
        videoPlayView.visibility = View.INVISIBLE

        onPlayStart()
    }

    open fun onPlayStart() {
        listener?.onVideoPlayStart()
    }

    open fun onPlayEnd() {
        listener?.onVideoPlayEnd()
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

        videoPath?.let {
            videoView.setOnPreparedListener {
                videoView.start()

                postDelayed(160L) {
                    AnimUtil.startArgb(videoView, Color.BLACK, Color.TRANSPARENT, 160)
//                    videoView.setBackgroundColor(Color.TRANSPARENT)
                    videoView.setOnPreparedListener { }
                    if (!autoPlay) {
                        videoView.pause()
                    }
                }
            }

            videoView.setVideoPath(it)
        }
    }

    override fun onViewUnload() {
        super.onViewUnload()
        videoView.stopPlayback()
    }

    interface OnVideoPlayListener {
        fun onVideoPlayStart()
        fun onVideoPlayEnd()
    }
}