package com.angcyo.picker.media

import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.uiview.BaseMediaUIView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/06/19 16:03
 * 修改人员：Robi
 * 修改时间：2018/06/19 16:03
 * 修改备注：
 * Version: 1.0.0
 */
interface OnSendButtonClickObserver {

    /**
     * @return 是否需要拦截默认的处理
     * */
    fun onSendButtonClick(uiview: BaseMediaUIView, mediaItemList: MutableList<MediaItem>): Boolean
}