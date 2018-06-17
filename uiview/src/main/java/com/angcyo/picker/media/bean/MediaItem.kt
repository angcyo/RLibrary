package com.angcyo.picker.media.bean

import com.angcyo.uiview.kotlin.toTime
import com.angcyo.uiview.utils.RUtils

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：数据bean
 * 创建人员：Robi
 * 创建时间：2018/06/15 14:13
 * 修改人员：Robi
 * 修改时间：2018/06/15 14:13
 * 修改备注：
 * Version: 1.0.0
 */
open class MediaItem {
    //以下字段都是从数据库中获取的
    var path = ""//文件位置
    var displayName = ""
    var addTime = 0L
        set(value) {
            field = value
            addTimeString = (addTime * 1000L).toTime()
        }
    var mimeType = ""
    var size = 0L
    var width = 0
    var height = 0
    var latitude = 0.0
    var longitude = 0.0
    var duration = 0L
    //end...

    var addTimeString = "" //年月日的时间格式
    var videoThumbPath = ""//视频截帧图片 根据md5 值创建路径
    var thumbPath = "" //图片缩略图路径

    override fun toString(): String {
        val builder = StringBuilder("{")
        builder.append("\n\t")
        builder.append("path:")
        builder.append(path)

        builder.append("\n\t")
        builder.append("displayName:")
        builder.append(displayName)

        builder.append("\n\t")
        builder.append("addTime:")
        builder.append(addTime)
        builder.append(" ")
        builder.append(addTimeString)

        builder.append("\n\t")
        builder.append("mimeType:")
        builder.append(mimeType)

        builder.append("\n\t")
        builder.append("size:")
        builder.append(RUtils.formatFileSize(size))

        builder.append("\n\t")
        builder.append("width:")
        builder.append(width)

        builder.append("\n\t")
        builder.append("height:")
        builder.append(height)

        builder.append("\n\t")
        builder.append("latitude:")
        builder.append(latitude)

        builder.append("\n\t")
        builder.append("longitude:")
        builder.append(longitude)

        builder.append("\n\t")
        builder.append("duration:")
        builder.append(duration)

        builder.append("\n\t")
        builder.append("videoThumbPath:")
        builder.append(videoThumbPath)

        builder.append("\n\t")
        builder.append("thumbPath:")
        builder.append(thumbPath)

        builder.append("\n}")
        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MediaItem) {
            path == other.path
        } else {
            false
        }
    }
}