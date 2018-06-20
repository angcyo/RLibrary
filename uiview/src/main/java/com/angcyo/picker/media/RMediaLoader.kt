package com.angcyo.picker.media

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.text.TextUtils
import com.angcyo.library.utils.L
import com.angcyo.picker.media.bean.MediaFolder
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.kotlin.isAudioMimeType
import com.angcyo.uiview.kotlin.isImageMimeType
import com.angcyo.uiview.kotlin.isVideoMimeType
import com.angcyo.uiview.net.Rx
import com.angcyo.uiview.utils.Debug
import java.io.File

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：媒体加载工具, 图片/视频/音频
 * 创建人员：Robi
 * 创建时间：2018/06/15 14:03
 * 修改人员：Robi
 * 修改时间：2018/06/15 14:03
 * 修改备注：
 * Version: 1.0.0
 */
class RMediaLoader(private val activity: FragmentActivity,
                   private val loaderConfig: MediaLoaderConfig,
                   private val observer: (MutableList<MediaFolder>) -> Unit) {

    companion object {
        //查询所有媒体的uri
        private val ALL_QUERY_URI = MediaStore.Files.getContentUri("external")

        private const val DURATION = "duration"
        private const val SIZE = "_size"
        private const val LATITUDE = "latitude"    //纬度
        private const val LONGITUDE = "longitude"  //经度

        /**
         * 全部媒体数据 - PROJECTION
         * 需要返回的数据库字段
         */
        private val ALL_PROJECTION = arrayOf(MediaStore.Images.Media._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT,
                LATITUDE,
                LONGITUDE,
                DURATION)

        /**
         * 全部媒体数据 - SELECTION
         * 需要查询满足的条件
         * 不加条件所有的文件/文件夹都会出来
         */
        private const val ALL_SELECTION = (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
                        + " AND "
                        + MediaStore.Files.FileColumns.SIZE + ">0"
                        + " AND "
                        + DURATION + ">0")

        /**图片和视频, 去除音频*/
        private const val ALL_IMAGE_VIDEO_SELECTION = (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        + " AND "
                        + MediaStore.Files.FileColumns.SIZE + ">0"
                        + " AND "
                        + DURATION + ">0")

        /**音频 选择*/
        private const val AUDIO_SELECTION = (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
                        + " AND "
                        + MediaStore.Files.FileColumns.SIZE + ">0"
                        + " AND "
                        + DURATION + ">0")

        /**视频 选择*/
        private const val VIDEO_SELECTION = (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        + " AND "
                        + MediaStore.Files.FileColumns.SIZE + ">0"
                        + " AND "
                        + DURATION + ">0")

        /**图片 选择*/
        private const val IMAGE_SELECTION = (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " AND "
                        + MediaStore.Files.FileColumns.SIZE + ">0")

        private fun createMediaFolder(path: String, folderList: MutableList<MediaFolder>): MediaFolder {
            val file = File(path)

            //之前已经存在了文件夹
            for (f in folderList) {
                if (TextUtils.equals(f.folderPath, file.parentFile.absolutePath)) {
                    return f
                }
            }

            //创建新的文件夹
            val folder = MediaFolder().apply {
                folderPath = file.parentFile.absolutePath
                folderName = file.parentFile.name
            }
            folderList.add(folder)

            return folder
        }
    }

    /**开始扫描媒体*/
    fun startLoadMedia() {
        activity.supportLoaderManager.initLoader(loaderConfig.mediaLoaderType,
                null, object : LoaderManager.LoaderCallbacks<Cursor> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                val selection = when (id) {
                    MediaLoaderConfig.LOADER_TYPE_ALL -> ALL_SELECTION
                    MediaLoaderConfig.LOADER_TYPE_IMAGE_VIDEO -> ALL_IMAGE_VIDEO_SELECTION
                    MediaLoaderConfig.LOADER_TYPE_IMAGE -> IMAGE_SELECTION
                    MediaLoaderConfig.LOADER_TYPE_VIDEO -> VIDEO_SELECTION
                    MediaLoaderConfig.LOADER_TYPE_AUDIO -> AUDIO_SELECTION
                    else -> ALL_SELECTION
                }
                return CursorLoader(activity, ALL_QUERY_URI, ALL_PROJECTION,
                        selection + loaderConfig.getFileSelectorSelection(),
                        null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
            }

            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                val allFolderList = mutableListOf<MediaFolder>()

                Rx.base({
                    val mainMediaFolder = MediaFolder().apply {
                        mediaFolderType = loader.id
                    }
                    val audioMediaFolder = MediaFolder().apply {
                        mediaFolderType = MediaLoaderConfig.LOADER_TYPE_AUDIO
                        folderName = "所有音频"
                        folderPath = MediaLoaderConfig.FOLDER_PATH_AUDIO
                    }
                    val videoMediaFolder = MediaFolder().apply {
                        mediaFolderType = MediaLoaderConfig.LOADER_TYPE_VIDEO
                        folderName = "所有视频"
                        folderPath = MediaLoaderConfig.FOLDER_PATH_VIDEO
                    }
                    val imageMediaFolder = MediaFolder().apply {
                        mediaFolderType = MediaLoaderConfig.LOADER_TYPE_IMAGE
                        folderName = "所有图片"
                        folderPath = MediaLoaderConfig.FOLDER_PATH_IMAGE
                    }

                    Debug.logTimeStart("开始扫描媒体:")
                    if (data != null && data.count > 0) {
                        L.e("call: onLoadFinished -> ${data.count}")
                        data.moveToFirst()
                        do {
                            try {
                                val mimeType = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[4]))

                                if (mimeType.isImageMimeType() ||
                                        mimeType.isAudioMimeType() ||
                                        mimeType.isVideoMimeType()) {

                                    val path = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[1]))
                                    val displayName = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[2]))
                                    val addTime = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[3]))
                                    val size = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[5]))
                                    val width = data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[6]))
                                    val height = data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[7]))
                                    val latitude = data.getDouble(data.getColumnIndexOrThrow(ALL_PROJECTION[8]))
                                    val longitude = data.getDouble(data.getColumnIndexOrThrow(ALL_PROJECTION[9]))
                                    val duration = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[10]))

                                    val mediaItem = MediaItem().apply {
                                        this.path = path ?: ""
                                        this.displayName = displayName ?: ""
                                        this.addTime = addTime
                                        this.mimeType = mimeType ?: ""
                                        this.size = size
                                        this.width = width
                                        this.height = height
                                        this.latitude = latitude
                                        this.longitude = longitude
                                        this.duration = duration
                                    }

                                    mainMediaFolder.mediaItemList.add(mediaItem)
                                    if (mediaItem.mimeType.isAudioMimeType()) {
                                        //音频item
                                        audioMediaFolder.mediaItemList.add(mediaItem)

                                        if (loader.id == MediaLoaderConfig.LOADER_TYPE_AUDIO) {
                                            val mediaFolder = createMediaFolder(path, allFolderList)
                                            mediaFolder.mediaFolderType = MediaLoaderConfig.LOADER_TYPE_AUDIO
                                            mediaFolder.mediaItemList.add(mediaItem)
                                        }
                                    } else if (mediaItem.mimeType.isVideoMimeType()) {
                                        //视频item
                                        videoMediaFolder.mediaItemList.add(mediaItem)

//                                        val pathBuilder = StringBuilder()
//                                        pathBuilder.append(activity.getExternalFilesDir("video_thumb").absolutePath)
//                                        pathBuilder.append(File.separator)
//
//                                        //因为MD5获取太慢, 改为文件名和最后一次修改时间作为缩略图路径
//                                        val file = File(mediaItem.path)
//                                        pathBuilder.append(MD5.getStringMD5(file.name + file.lastModified()))
//
//                                        mediaItem.videoThumbPath = pathBuilder.toString()

//                                        Rx.back {
//                                            mediaItem.videoThumbPath = activity.getExternalFilesDir("video_thumb").absolutePath + File.separator + MD5.getStreamMD5(mediaItem.path)
//                                        }

                                        if (loader.id == MediaLoaderConfig.LOADER_TYPE_VIDEO) {
                                            val mediaFolder = createMediaFolder(path, allFolderList)
                                            mediaFolder.mediaFolderType = MediaLoaderConfig.LOADER_TYPE_VIDEO
                                            mediaFolder.mediaItemList.add(mediaItem)
                                        }
                                    } else {
                                        //其他 也就是图片
                                        imageMediaFolder.mediaItemList.add(mediaItem)

                                        val mediaFolder = createMediaFolder(path, allFolderList)
                                        mediaFolder.mediaFolderType = MediaLoaderConfig.LOADER_TYPE_IMAGE
                                        mediaFolder.mediaItemList.add(mediaItem)
                                    }

                                    //L.i("call: onLoadFinished -> $mediaItem")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        } while (data.moveToNext())
                        Debug.logTimeEnd("扫描媒体结束:")

                        if (!allFolderList.isEmpty()) {
                            if (loader.id == MediaLoaderConfig.LOADER_TYPE_ALL) {
                                mainMediaFolder.folderName = "所有媒体"
                                mainMediaFolder.folderPath = MediaLoaderConfig.FOLDER_PATH_ALL

                                allFolderList.add(0, mainMediaFolder)

                                if (audioMediaFolder.mediaCount > 0) {
                                    allFolderList.add(1, audioMediaFolder)
                                }
                                if (videoMediaFolder.mediaCount > 0) {
                                    allFolderList.add(1, videoMediaFolder)
                                }
                            } else if (loader.id == MediaLoaderConfig.LOADER_TYPE_IMAGE_VIDEO) {
                                mainMediaFolder.folderName = "图片和视频"
                                mainMediaFolder.folderPath = MediaLoaderConfig.FOLDER_PATH_IMAGE_VIDEO

                                allFolderList.add(0, mainMediaFolder)

                                if (videoMediaFolder.mediaCount > 0) {
                                    allFolderList.add(1, videoMediaFolder)
                                }
                            } else if (loader.id == MediaLoaderConfig.LOADER_TYPE_IMAGE) {
                                allFolderList.add(0, imageMediaFolder)
                            } else if (loader.id == MediaLoaderConfig.LOADER_TYPE_VIDEO) {
                                allFolderList.add(0, videoMediaFolder)
                            } else if (loader.id == MediaLoaderConfig.LOADER_TYPE_AUDIO) {
                                allFolderList.add(0, audioMediaFolder)
                            }
                        }
                    }
                }, {
                    //销毁之后 没有缓存加速了
                    //activity.supportLoaderManager.destroyLoader(loader.id)
                    observer.invoke(allFolderList)
                })
            }

            override fun onLoaderReset(loader: Loader<Cursor>) {
                L.i("RMediaLoader: onLoaderReset -> $loader")
            }
        })
    }

}