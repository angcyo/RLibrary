package com.angcyo.uiview.widget

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.angcyo.github.utilcode.utils.ImageUtils
import com.angcyo.library.glide.GlideApp
import com.angcyo.library.okhttp.Ok
import com.angcyo.library.utils.L
import com.angcyo.uiview.R
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import pl.droidsonroids.gif.GifDrawableBuilder
import java.io.File

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持文件/网络图片加载, 支持动态检查gif, 支持显示gif提示, 支持动画
 * 创建人员：Robi
 * 创建时间：2017/07/04 11:11
 * 修改人员：Robi
 * 修改时间：2017/07/04 11:11
 * 修改备注：
 * Version: 1.0.0
 */
open class GlideImageView(context: Context, attributeSet: AttributeSet? = null) : RImageView(context, attributeSet) {

    private var defaultPlaceholderDrawable: Drawable? = null

    /**是否要检查Url的图片类型为Gif, 可以用来显示Gif指示图*/
    var checkGif = false

    /**强制url使用asGif的方式加载, 需要 checkGif=true*/
    var showAsGifImage = false

    /**占位资源*/
    var placeholderRes: Int = R.drawable.base_image_placeholder_shape
        set(value) {
            field = value
            if (value == -1) {
                placeholderDrawable = null
            } else {
                placeholderDrawable = ContextCompat.getDrawable(context, value)
            }
        }

    var placeholderDrawable: Drawable? = null

    var override = true

    /**动画样式*/
    var animType = AnimType.DEFAULT

    var bitmapTransform: Transformation<*>? = null

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.GlideImageView)
        defaultPlaceholderDrawable = typedArray.getDrawable(R.styleable.GlideImageView_r_placeholder_drawable)
        if (defaultPlaceholderDrawable != null) {
            placeholderDrawable = defaultPlaceholderDrawable
        }
        typedArray.recycle()
    }

    /**需要加载的图片地址, 优先判断是否是本地File*/
    open var url: String? = ""
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
        if (defaultPlaceholderDrawable != null) {
            placeholderDrawable = defaultPlaceholderDrawable
        }
        animType = AnimType.DEFAULT
        bitmapTransform = null
        url = ""
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startLoadUrl()
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
            //setImageResource(placeholderRes)
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

    private fun defaultConfig(isGif: Boolean): RequestOptions {
        val requestOptions = RequestOptions.placeholderOf(placeholderRes).error(placeholderRes)

        if (isGif) {
            requestOptions.diskCacheStrategy(DiskCacheStrategy.DATA).priority(Priority.HIGH)
        } else {
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.NORMAL)
        }

        if (scaleType == ScaleType.CENTER_CROP) {
            requestOptions.centerCrop()
        }

        when (animType) {
            AnimType.NONE, AnimType.TRANSITION -> requestOptions.dontAnimate()
            AnimType.CROSS_FADE -> {
            }
        }

        return requestOptions
    }

    private fun intoConfig(onDelayInto: () -> Unit) {
        if (override && (measuredWidth == 0 || measuredHeight == 0)) {
            post {
                onDelayInto.invoke()
            }
        } else {
            onDelayInto.invoke()
        }
    }

    private fun intoConfigFile(request: RequestBuilder<File>, requestOptions: RequestOptions) {
        intoConfig {
            requestOptions.override(measuredWidth, measuredHeight)
            request.apply(requestOptions)
            if (animType == AnimType.TRANSITION) {
                request.into(object : SimpleTarget<File>() {
                    override fun onResourceReady(resource: File?, transition: Transition<in File>?) {
                        if (resource != null && placeholderDrawable != null) {
                            setImageDrawable(placeholderDrawable!!, BitmapDrawable(resources, resource.absolutePath))
                        }
                    }
                })
            } else {
                request.into(this)
            }
        }
    }

    private fun intoConfigGifDrawable(request: RequestBuilder<GifDrawable>, requestOptions: RequestOptions) {
        intoConfig {
            requestOptions.override(measuredWidth, measuredHeight)
            request.apply(requestOptions)
            if (animType == AnimType.TRANSITION) {
                request.into(object : SimpleTarget<GifDrawable>() {
                    override fun onResourceReady(resource: GifDrawable?, transition: Transition<in GifDrawable>?) {
                        if (resource != null && placeholderDrawable != null) {
                            setImageDrawable(placeholderDrawable!!, resource)
                            resource.start()
                        }
                    }
                })
            } else {
                request.into(this)
            }
        }
    }

    private fun intoConfigDrawable(request: RequestBuilder<Drawable>, requestOptions: RequestOptions) {
        intoConfig {
            requestOptions.override(measuredWidth, measuredHeight)
            request.apply(requestOptions)
            if (animType == AnimType.TRANSITION) {
                request.into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable?, transition: Transition<in Drawable>?) {
                        if (resource != null && placeholderDrawable != null) {
                            setImageDrawable(placeholderDrawable!!, resource)
                        }
                    }
                })
            } else {
                request.into(this)
            }
        }
    }

    private fun intoConfigBitmap(request: RequestBuilder<Bitmap>, requestOptions: RequestOptions) {
        intoConfig {
            requestOptions.override(measuredWidth, measuredHeight)
            request.apply(requestOptions)
            if (animType == AnimType.TRANSITION) {
                request.into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                        if (resource != null && placeholderDrawable != null) {
                            setImageDrawable(placeholderDrawable!!, BitmapDrawable(resources, resource))
                        }
                    }
                })
            } else {
                request.into(this)
            }
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
//        val tag = this.tag
//        if (tag is GenericRequest<*, *, *, *>) {
//            tag.clear()
//            L.d("cancelRequest() ->" + this.javaClass.simpleName + "  GenericRequest Clear")
//        }
        if (isInEditMode) {
        } else {
            GlideApp.with(this).clear(this)
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
                .downloadOnly()
                .load(file)

        intoConfig {
            request.apply(defaultConfig(true))
            request.into(object : SimpleTarget<File>() {
                override fun onResourceReady(resource: File?, transition: Transition<in File>?) {
                    resource?.let {
                        try {
                            L.e("call: 加载Gif 本地地址 -> ${resource.absolutePath}")
                            val gifDrawable = GifDrawableBuilder().from(resource).build()
                            this@GlideImageView.setImageDrawable(gifDrawable)
                        } catch (e: Exception) {
                            this@GlideImageView.setImageDrawable(BitmapDrawable(resources, resource.absolutePath))
                            L.e("call: 加载Gif ${resource.absolutePath} 失败.")
                        }
                    }
                }
            })
        }
    }

    private fun showJpegFile(file: File) {
        val request = Glide.with(context)
                .asBitmap()
                .load(file)

        intoConfigBitmap(request, defaultConfig(false))
    }

    private fun showFile(file: File) {
        val request = Glide.with(context)
                .load(file)

        intoConfigDrawable(request, defaultConfig(false))
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

        intoConfigDrawable(request, defaultConfig(false))
    }

    private fun loadJpegUrl(url: String) {
        if (!canLoad(url)) {
            return
        }

        val request = Glide.with(context)
                .asBitmap()
                .load(url)

        intoConfigBitmap(request, defaultConfig(false))
    }

    private fun loadGifUrl(url: String) {
        if (!canLoad(url)) {
            return
        }

        val request = Glide.with(context)
                .downloadOnly()
                .load(url)

        intoConfig {
            request.apply(defaultConfig(true))
            request.into(object : SimpleTarget<File>() {
                override fun onResourceReady(resource: File?, transition: Transition<in File>?) {
                    resource?.let {
                        try {
                            L.i("call: 加载Gif 本地地址 -> ${resource.absolutePath}")
                            val gifDrawable = GifDrawableBuilder().from(resource).build()
                            this@GlideImageView.setImageDrawable(gifDrawable)
                        } catch (e: Exception) {
                            this@GlideImageView.setImageDrawable(BitmapDrawable(resources, resource.absolutePath))
                            L.i("call: 加载Gif ${resource.absolutePath} 失败.")
                        }
                    }
                }
            })
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

