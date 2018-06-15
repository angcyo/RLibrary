package com.angcyo.picker.media.bean

import com.angcyo.uiview.utils.RUtils

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：加载后的媒体信息, 都分组在相同的文件夹中
 * 创建人员：Robi
 * 创建时间：2018/06/15 14:12
 * 修改人员：Robi
 * 修改时间：2018/06/15 14:12
 * 修改备注：
 * Version: 1.0.0
 */
class MediaFolder {
    //文件夹名
    var folderName = ""
    //文件夹路径
    var folderPath = ""

    var mediaItemList = mutableListOf<MediaItem>()

    //媒体数量
    val mediaCount: Int
        get() = RUtils.listSize(mediaItemList)

    override fun equals(other: Any?): Boolean {
        return if (other is MediaFolder) {
            folderPath == other.folderPath
        } else {
            false
        }
    }
}