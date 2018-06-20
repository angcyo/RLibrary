package com.angcyo.picker.media

import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.picker.media.uiview.RMediaLoaderUIView
import com.angcyo.uiview.container.ILayout

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/06/20 08:52
 * 修改人员：Robi
 * 修改时间：2018/06/20 08:52
 * 修改备注：
 * Version: 1.0.0
 */
object RPicker {
    fun pickerImage(iLayout: ILayout, limit: Int = 1, observer: OnMediaSelectorObserver) {
        iLayout.startIView(RMediaLoaderUIView().apply {
            mediaLoaderConfig = MediaLoaderConfig().apply {
                mediaLoaderType = MediaLoaderConfig.LOADER_TYPE_IMAGE
                maxSelectorLimit = limit
            }

            onMediaSelectorObserver = observer
        })
    }
}