package com.angcyo.uiview.base

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.VideoView
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout

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

    override fun inflateContentLayout(baseContentLayout: ContentLayout?, inflater: LayoutInflater?) {
        inflate(R.layout.base_video_layout)
    }

    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()
        videoView = mViewHolder.v(R.id.base_video_view)
    }

    override fun onViewHide() {
        super.onViewHide()
        videoView.pause()
    }

    override fun onViewShow(bundle: Bundle?) {
        super.onViewShow(bundle)
        videoView.resume()
    }
}