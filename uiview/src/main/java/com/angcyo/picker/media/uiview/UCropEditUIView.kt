package com.angcyo.picker.media.uiview

import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import com.angcyo.library.utils.L
import com.angcyo.picker.media.bean.MediaItem
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.model.TitleBarItem
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.RUtils
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.GestureCropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.TransformImageView
import com.yalantis.ucrop.view.UCropView
import java.io.File
import java.util.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：图片编辑
 * 创建人员：Robi
 * 创建时间：2018/06/19 13:35
 * 修改人员：Robi
 * 修改时间：2018/06/19 13:35
 * 修改备注：
 * Version: 1.0.0
 */
class UCropEditUIView(mediaLoaderConfig: MediaLoaderConfig, private val mediaItem: MediaItem) : BaseMediaUIView() {

    companion object {
        private const val SCALE_WIDGET_SENSITIVITY_COEFFICIENT = 15000
        val DEFAULT_COMPRESS_FORMAT: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
        const val DEFAULT_COMPRESS_QUALITY = 90
    }

    init {
        this@UCropEditUIView.mediaLoaderConfig = mediaLoaderConfig
    }

    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar()
                .setFloating(false)
                .setTitleString("裁剪")
                .addRightItem(0, TitleBarItem.build(R.drawable.ucrop_ic_angle_white) {
                    mGestureCropImageView.postRotate(90f)
                })
                .addRightItem(TitleBarItem.build("保存") {
                    cropAndSaveImage()
                }.setTextColor(SkinHelper.getSkin().themeSubColor)
                        .setTextSize(12 * density()))


    }

    /**图片编辑完成*/
    var onImageEditObserver: ((MediaItem) -> Unit)? = null

    override fun inflateContentLayout(baseContentLayout: ContentLayout, inflater: LayoutInflater) {
        inflate(R.layout.view_media_ucrop_edit_layout)
    }

    private var mCompressFormat = DEFAULT_COMPRESS_FORMAT
    private var mCompressQuality = DEFAULT_COMPRESS_QUALITY

    private val mImageListener = object : TransformImageView.TransformImageListener {
        override fun onRotate(currentAngle: Float) {
            //setAngleText(currentAngle)
        }

        override fun onScale(currentScale: Float) {
            //setScaleText(currentScale)
        }

        override fun onLoadComplete() {
            mUCropView.animate().alpha(1f).setDuration(300).interpolator = AccelerateInterpolator()
        }

        override fun onLoadFailure(e: Exception) {
        }
    }

    /**保存图片*/
    fun cropAndSaveImage() {
        mGestureCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, object : BitmapCropCallback {

            override fun onBitmapCropped(resultUri: Uri, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {

                RUtils.scanFile(mActivity, resultUri.path)

                L.e("编辑完成: onBitmapCropped -> $resultUri $offsetX $offsetY $imageWidth  $imageHeight")

                finishIView {
                    (mActivity as FragmentActivity).supportLoaderManager.destroyLoader(mediaLoaderConfig.mediaLoaderType)
                    onImageEditObserver?.invoke(MediaItem().apply {
                        path = resultUri.path
                        width = imageWidth
                        height = imageHeight
                        displayName = File(resultUri.path).name
                        mimeType = "image/png"
                    })
                }
            }

            override fun onCropFailure(t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private lateinit var mUCropView: UCropView
    private lateinit var mGestureCropImageView: GestureCropImageView
    private lateinit var mOverlayView: OverlayView

    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()
        uiTitleBarContainer.hideRightItemById(R.id.base_send_button)

        mUCropView = v(R.id.base_ucrop_view)
        mGestureCropImageView = mUCropView.cropImageView
        mOverlayView = mUCropView.overlayView

        mGestureCropImageView.setTransformImageListener(mImageListener)

        val inputFile = File(mediaItem.path)
//        val outPath = "${inputFile.parentFile.absolutePath}/${MD5.getStringMD5(inputFile.name + inputFile.lastModified())}.png"
        val outPath = "${inputFile.parentFile.absolutePath}/${UUID.randomUUID()}.png"
        mGestureCropImageView.setImageUri(Uri.fromFile(inputFile), Uri.fromFile(File(outPath)))
        L.e("编辑文件 ${mediaItem.path} -> $outPath")

        //关闭手势旋转, 采用按钮旋转
        mGestureCropImageView.isRotateEnabled = false

        if (mediaLoaderConfig.outputImageWidth > 10 &&
                mediaLoaderConfig.outputImageHeight > 10) {

            //设置裁剪区域的比例 (宽度/高度)
            mGestureCropImageView.targetAspectRatio = mediaLoaderConfig.outputImageWidth * 1f / mediaLoaderConfig.outputImageHeight

            mGestureCropImageView.setMaxResultImageSizeX(mediaLoaderConfig.outputImageWidth)
            mGestureCropImageView.setMaxResultImageSizeY(mediaLoaderConfig.outputImageHeight)
        } else {
            mGestureCropImageView.targetAspectRatio = 1f
        }
    }

}