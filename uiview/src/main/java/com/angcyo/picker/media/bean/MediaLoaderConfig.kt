package com.angcyo.picker.media.bean

import android.provider.MediaStore
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.utils.T_

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

        //不限制文件大小
        const val SIZE_MODEL_NONE = 0
        //扫描媒体的时候 加上大小限制条件
        const val SIZE_MODEL_MEDIA = 1
        //选择媒体的时候, 判断大小是否合适
        const val SIZE_MODEL_SELECTOR = 2
    }

    var mediaLoaderType = LOADER_TYPE_IMAGE

    /**是否显示原图按钮, 如果支持就打开, 否则关闭*/
    var showOriginButton = false

    /**最大选择数量*/
    var maxSelectorLimit = 9

    /**选择模式, 建议单选模式 用 maxSelectorLimit=1  控制*/
    var selectorModel = SELECTOR_MODEL_MULTI

    /**是否显示编辑按钮, 目前只支持图片编辑*/
    var enableImageEdit = true

    /**显示拍照按钮*/
    var enableCamera = true

    /**指定编辑输出图片的宽度/高度*/
    var outputImageWidth = 0
    var outputImageHeight = 0

    /**限制文件大小模式, 混合文件类型时 无法过滤, 请使用 SIZE_MODEL_SELECTOR */
    var limitFileSizeModel = SIZE_MODEL_NONE
    //kb
    var limitFileMinSize = 0f
    //kb
    var limitFileMaxSize = 0f

    /**显示文件大小*/
    var showFileSize = false


    /**
     * 混合选择模式
     *
     * LOADER_TYPE_ALL     --都能选
     * LOADER_TYPE_IMAGE   --主要选择图片, 其他只能选择单个
     * LOADER_TYPE_VIDEO   --主要选择视频, 其他只能选择单个
     * LOADER_TYPE_AUDIO   --主要选择音频, 其他只能选择单个
     * LOADER_TYPE_IMAGE_VIDEO  --图片和视频都能选, 其他只能选择单个
     * */
    var mixSelectorModel = LOADER_TYPE_ALL
    /**非混合模式下, 可以选择视频的数量*/
    var maxSelectorVideoLimit = 1
    var maxSelectorAudioLimit = 1

    /**是否是多选模式*/
    fun isMultiModel(): Boolean = selectorModel == SELECTOR_MODEL_MULTI

    /**是否可以选中文件*/
    fun canSelectorFile(fileSize: Long): Boolean {
        if (limitFileSizeModel == SIZE_MODEL_SELECTOR) {
            if (limitFileMinSize == 0f && limitFileMaxSize == 0f) {
                return true
            }
            if (limitFileMinSize > 0f && limitFileMaxSize > 0f) {
                val result = fileSize * 1f / 1024 in limitFileMinSize..limitFileMaxSize
                return if (result) {
                    true
                } else {
                    T_.error("文件大小需要在 ${RUtils.formatFileSize((limitFileMinSize * 1024).toLong())}~${RUtils.formatFileSize((limitFileMaxSize * 1024).toLong())} 之间")
                    false
                }
            }
            if (limitFileMinSize > 0f) {
                val result = fileSize * 1f / 1024 >= limitFileMinSize
                return if (result) {
                    true
                } else {
                    T_.error("文件大小需要大于 ${RUtils.formatFileSize((limitFileMinSize * 1024).toLong())}")
                    false
                }
            }

            if (limitFileMaxSize > 0f) {
                val result = fileSize * 1f / 1024 <= limitFileMaxSize
                return if (result) {
                    true
                } else {
                    T_.error("文件大小需要小于 ${RUtils.formatFileSize((limitFileMaxSize * 1024).toLong())}")
                    false
                }
            }
        }
        return true
    }

    fun getFileSelectorSelection(): String {
        if (limitFileSizeModel == SIZE_MODEL_MEDIA) {
            if (limitFileMinSize == 0f && limitFileMaxSize == 0f) {
                return ""
            }
            if (limitFileMinSize > 0f && limitFileMaxSize > 0f) {
                return " AND ${MediaStore.Files.FileColumns.SIZE}<=${limitFileMaxSize * 1024} AND ${MediaStore.Files.FileColumns.SIZE}>=${limitFileMinSize * 1024}"
            }
            if (limitFileMinSize > 0f) {
                return " AND ${MediaStore.Files.FileColumns.SIZE}>=${limitFileMinSize * 1024}"
            }

            if (limitFileMaxSize > 0f) {
                return " AND ${MediaStore.Files.FileColumns.SIZE}<=${limitFileMaxSize * 1024}"
            }
        }
        return ""
    }
}