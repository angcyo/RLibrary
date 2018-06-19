package com.angcyo.picker.media.bean

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：加载媒体的一些配置参数
 * 创建人员：Robi
 * 创建时间：2018/06/15 14:05
 * 修改人员：Robi
 * 修改时间：2018/06/15 14:05
 * 修改备注：
 * Version: 1.0.0
 */
class MediaLoaderConfig {
    companion object {
        //图片和视频, (音频要不要支持呢?)
        const val LOADER_TYPE_ALL = 1
        const val LOADER_TYPE_IMAGE = 2
        const val LOADER_TYPE_VIDEO = 3
        const val LOADER_TYPE_AUDIO = 4
        const val LOADER_TYPE_IMAGE_VIDEO = 5

        /**单选*/
        const val SELECTOR_MODEL_SINGLE = 1
        /**多选*/
        const val SELECTOR_MODEL_MULTI = 2

        const val FOLDER_PATH_AUDIO = "audio"
        const val FOLDER_PATH_IMAGE = "image"
        const val FOLDER_PATH_VIDEO = "video"
        const val FOLDER_PATH_ALL = "all"
        const val FOLDER_PATH_IMAGE_VIDEO = "image_video"
    }

    var mediaLoaderType = LOADER_TYPE_IMAGE

    /**是否显示原图按钮, 如果支持就打开, 否则关闭*/
    var showOriginButton = false

    /**最大选择数量*/
    var maxSelectorLimit = 9

    /**选择模式*/
    var selectorModel = SELECTOR_MODEL_MULTI

    /**是否显示编辑按钮, 目前只支持图片编辑*/
    var enableImageEdit = true

    /**显示拍照按钮*/
    var enableCamera = true

    /**指定输出图片的宽度/高度*/
    var outputImageWidth = 0
    var outputImageHeight = 0
}