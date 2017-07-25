package com.angcyo.uiview.widget

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.angcyo.github.utilcode.utils.ImageUtils
import com.angcyo.library.okhttp.Ok
import com.angcyo.library.utils.L
import com.angcyo.uiview.R
import com.bumptech.glide.*
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.GenericRequest
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import java.io.File

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/04 11:11
 * 修改人员：Robi
 * 修改时间：2017/07/04 11:11
 * 修改备注：
 * Version: 1.0.0
 */
open class GlideImageView(context: Context, attributeSet: AttributeSet? = null) : RImageView(context, attributeSet) {
    /**是否要检查Url的图片类型为Gif, 可以用来显示Gif指示图*/
    var checkGif = false

    /**强制url使用asGif的方式加载, 需要 checkGif=true*/
    var showAsGifImage = false

    /**占位资源*/
    var placeholderRes: Int = R.drawable.base_image_placeholder_shape

    var override = true

    /**动画样式*/
    var animType = AnimType.DEFAULT

    var bitmapTransform: Transformation<*>? = null

    /**需要加载的图片地址, 优先判断是否是本地File*/
    var url: String? = ""
        set(value) {
            field = value
            startLoadUrl()
        }

    /**重置所有属性, 方便在RecyclerView中使用*/
    fun reset() {
        checkGif = false
        showAsGifImage = false
        mShowGifTip = false
        override = true
        placeholderRes = R.drawable.base_image_placeholder_shape
        animType = AnimType.DEFAULT
        bitmapTransform = null
        url = ""
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startLoadUrl()
    }

    private fun getPlaceholderDrawable(): Drawable {
        return ContextCompat.getDrawable(context, placeholderRes)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        drawable?.let {
            //L.e("call: setImageDrawable -> ${it.javaClass.simpleName} $checkGif $measuredWidth $measuredHeight")
        }
        super.setImageDrawable(drawable)
//        if (animType == AnimType.TRANSITION) {
//            if (drawable != null && drawable !is TransitionDrawable) {
////                var fromDrawable: Drawable? = getDrawable()
////                if (fromDrawable == null) {
//                var fromDrawable = ContextCompat.getDrawable(context, placeholderRes)
////                }
//                setImageDrawable(fromDrawable, drawable)
////                super.setImageDrawable(drawable)
//            } else {
//                super.setImageDrawable(drawable)
//            }
//        } else {
//            super.setImageDrawable(drawable)
//        }
    }

    //启动加载流程
    private fun startLoadUrl() {
        setShowGifTip(false)
        if (url.isNullOrEmpty()) {
            setTagUrl("")
            cancelRequest()
            setImageResource(placeholderRes)
        } else {
            setTagUrl(url!!)
            if (measuredHeight == 0 || measuredWidth == 0) {

            } else {
                val file = File(url)
                if (file.exists()) {
                    loadWithFile(file)
                } else {
                    loadWidthUrl(url!!)
                }
            }
        }
    }

    private fun defaultConfig(request: GenericRequestBuilder<*, *, *, *>, isGif: Boolean) {
        request.placeholder(placeholderRes)
                .error(placeholderRes)

        if (isGif) {
            request.diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .priority(Priority.HIGH)
        } else {
            request.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.NORMAL)
        }

        when (request) {
            is DrawableRequestBuilder -> {
                if (scaleType == ScaleType.CENTER_CROP) {
                    request.centerCrop()
                }
                bitmapTransform?.let {
                    request.bitmapTransform(it as Transformation<Bitmap>?)
                }
            }
            is BitmapRequestBuilder -> {
                if (scaleType == ScaleType.CENTER_CROP) {
                    request.centerCrop()
                }
                bitmapTransform?.let {
                    request.transform(it as BitmapTransformation?)
                }
            }
            is GifRequestBuilder -> {
                if (scaleType == ScaleType.CENTER_CROP) {
                    request.centerCrop()
                }
                bitmapTransform?.let {
                    request.transform(it as Transformation<GifDrawable>?)
                }
            }
        }

        when (animType) {
            AnimType.NONE, AnimType.TRANSITION -> request.dontAnimate()
            AnimType.CROSS_FADE -> {
                when (request) {
                    is DrawableRequestBuilder -> request.crossFade()
                    is BitmapRequestBuilder -> request.crossFade()
                    is GifRequestBuilder -> request.crossFade()
                }
            }
        }
    }

    private fun intoConfig(request: GenericRequestBuilder<*, *, *, *>, onDelayInto: () -> Unit) {
        if (override) {
            if (measuredWidth == 0 || measuredHeight == 0) {
                post {
                    onDelayInto.invoke()
                }
            } else {
                request.override(measuredWidth, measuredHeight)
                if (animType == AnimType.TRANSITION) {
                    when (request) {
                        is DrawableTypeRequest -> {
                            request.into(object : SimpleTarget<GlideDrawable>() {
                                override fun onResourceReady(resource: GlideDrawable?, glideAnimation: GlideAnimation<in GlideDrawable>?) {
                                    resource?.let {
                                        setImageDrawable(getPlaceholderDrawable(), it)
                                    }
                                }
                            })
                        }
                        is BitmapTypeRequest -> {
                            request.into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                                    resource?.let {
                                        setImageDrawable(getPlaceholderDrawable(), BitmapDrawable(resources, it))
                                    }
                                }
                            })
                        }
                        is GifTypeRequest -> {
                            request.into(object : SimpleTarget<GifDrawable>() {
                                override fun onResourceReady(resource: GifDrawable?, glideAnimation: GlideAnimation<in GifDrawable>?) {
                                    resource?.let {
                                        setImageDrawable(getPlaceholderDrawable(), it)
                                        it.start()
                                    }
                                }
                            })
                        }
                    }
                } else {
                    request.into(this)
                }
            }
        } else {
            request.into(this)
        }
    }

    fun setTagUrl(url: String?) {
        if (url == null) {
            setTag(R.id.tag_url, "")
        } else {
            setTag(R.id.tag_url, url)
        }
    }

    fun getTagUrl(): String {
        val tag = getTag(R.id.tag_url)
        if (tag == null) {
            return ""
        } else {
            return tag as String
        }
    }

    /**取消请求*/
    fun cancelRequest() {
        val tag = this.tag
        if (tag is GenericRequest<*, *, *, *>) {
            tag.clear()
            L.d("cancelRequest() ->" + this.javaClass.simpleName + "  GenericRequest Clear")
        }
    }

    //加载方法
    private fun loadWithFile(file: File) {
        if (checkGif) {
            if ("GIF".equals(ImageUtils.getImageType(file), ignoreCase = true)) {
                if (showAsGifImage) {
                    showGifFile(file)
                } else {
                    setShowGifTip(true)
                    showJpegFile(file)
                }
            } else {
                showJpegFile(file)
            }
        } else {
            showFile(file)
        }
    }

    private fun showGifFile(file: File) {
        val request = Glide.with(context)
                .load(file)
                .asGif()

        defaultConfig(request, true)

        intoConfig(request) {
            showGifFile(file)
        }
    }

    private fun showJpegFile(file: File) {
        val request = Glide.with(context)
                .load(file)
                .asBitmap()

        defaultConfig(request, false)

        intoConfig(request) {
            showJpegFile(file)
        }
    }

    private fun showFile(file: File) {
        val request = Glide.with(context)
                .load(file)

        defaultConfig(request, false)

        intoConfig(request) {
            showFile(file)
        }
    }

    private fun loadWidthUrl(url: String) {
        if (!canLoad(url)) {
            return
        }
        if (checkGif) {
            Ok.instance()
                    .type(url, object : Ok.OnImageTypeListener {
                        override fun onLoadStart() {
                            this@GlideImageView.setImageResource(placeholderRes)
                        }

                        override fun onImageType(imageUrl: String, imageType: Ok.ImageType) {
                            if (!canLoad(imageUrl)) {
                                return
                            }

                            if (context is Activity) {
                                if ((context as Activity).isDestroyed) {
                                    return
                                }
                            }

                            if (imageType == Ok.ImageType.GIF) {
                                if (showAsGifImage) {
                                    loadGifUrl(imageUrl)
                                } else {
                                    setShowGifTip(true)
                                    loadJpegUrl(imageUrl)
                                }
                            } else {
                                loadJpegUrl(imageUrl)
                            }
                        }
                    })
        } else {
            loadUrl(url)
        }
    }

    private fun loadUrl(url: String) {
        if (!canLoad(url)) {
            return
        }

        val request = Glide.with(context)
                .load(url)

        defaultConfig(request, false)

        intoConfig(request) {
            loadUrl(url)
        }
    }

    private fun loadJpegUrl(url: String) {
        if (!canLoad(url)) {
            return
        }

        val request = Glide.with(context)
                .load(url)
                .asBitmap()

        defaultConfig(request, false)

        intoConfig(request) {
            loadJpegUrl(url)
        }
    }

    private fun loadGifUrl(url: String) {
        if (!canLoad(url)) {
            return
        }

        val request = Glide.with(context)
                .load(url)
                .asGif()

        defaultConfig(request, true)

        intoConfig(request) {
            loadGifUrl(url)
        }
    }

    //动画类型
    enum class AnimType(nativeInt: Int) {
        DEFAULT(0), NONE(-1), CROSS_FADE(1), TRANSITION(2);
    }

    //判断是否加载url
    private fun canLoad(url: String?): Boolean {
        val tagUrl = getTagUrl()

        if (tagUrl.isNullOrEmpty() || url.isNullOrEmpty()) {
            return false
        }

        if (!url!!.contains(tagUrl)) {
            return false
        }

        return true
    }
}

