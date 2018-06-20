package com.angcyo.uiview.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import com.angcyo.github.utilcode.utils.ImageUtils
import com.angcyo.library.okhttp.Ok
import com.angcyo.library.utils.L
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.textHeight
import com.angcyo.uiview.kotlin.textWidth
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import pl.droidsonroids.gif.GifDrawableBuilder
import java.io.File

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持文件/网络图片加载, 支持动态检查gif, 支持显示gif提示, 支持动画, 支持直接加载视频文件
 * 创建人员：Robi
 * 创建时间：2017/07/04 11:11
 * 修改人员：Robi
 * 修改时间：2017/07/04 11:11
 * 修改备注：
 * Version: 1.0.0
 */
open class GlideImageView(context: Context, attributeSet: AttributeSet? = null) : RImageView(context, attributeSet) {

    protected var defaultPlaceholderDrawable: Drawable? = null
    protected var defaultPlaceholderDrawableRes = R.drawable.base_image_placeholder_shape

    companion object {
        var DEBUG_SHOW = false
        /**文件名需要包含扩展名*/
        fun gifDrawable(context: Context, assertName: String) = GifDrawableBuilder().from(context.assets.open(assertName)).build()
    }

    /**是否要检查Url的图片类型为Gif, 可以用来显示Gif指示图*/
    var checkGif = false

    /**强制url使用asGif的方式加载, 需要 checkGif=true*/
    var showAsGifImage = false

    /**占位资源*/
    var placeholderRes: Int = defaultPlaceholderDrawableRes
        set(value) {
            field = value
            placeholderDrawable = if (value == -1) {
                null
            } else {
                ContextCompat.getDrawable(context, value)
            }
        }

    /**最终设置使用的占位资源*/
    var placeholderDrawable: Drawable? = null

    var override = true

    /**跳过内存缓存*/
    var skipMemoryCache = true

    /**动画样式*/
    var animType = AnimType.DEFAULT

    var bitmapTransform: Transformation<Bitmap>? = null

    var noPlaceholderDrawable = false

    /**需要加载的图片地址, 优先判断是否是本地File*/
    open var url: String? = null
        set(value) {
            field = value
            startLoadUrl()
        }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.GlideImageView)
        defaultPlaceholderDrawable = typedArray.getDrawable(R.styleable.GlideImageView_r_placeholder_drawable)
        noPlaceholderDrawable = typedArray.getBoolean(R.styleable.GlideImageView_r_no_placeholder_drawable, noPlaceholderDrawable)

        if (defaultPlaceholderDrawable != null) {
            placeholderDrawable = defaultPlaceholderDrawable
        } else if (drawable is BitmapDrawable) {
            defaultPlaceholderDrawable = drawable
            placeholderDrawable = drawable
        }

        if (noPlaceholderDrawable) {
            defaultPlaceholderDrawable = null
            placeholderDrawable = null
        } else {
            if (placeholderDrawable != null) {
                setImageDrawable(placeholderDrawable)
            }
        }

        val loadUrl = typedArray.getString(R.styleable.GlideImageView_r_url)
        if (!loadUrl.isNullOrEmpty()) {
            url = loadUrl
        }

        typedArray.recycle()
    }

    /**重置,并清理界面数据*/
    fun clear() {
        reset()
        setImageDrawable(null)
        if (!noPlaceholderDrawable && defaultPlaceholderDrawable != null) {
            placeholderDrawable = defaultPlaceholderDrawable
            setImageDrawable(placeholderDrawable)
        }
        url = ""
    }

    /**重置所有属性, 方便在RecyclerView中使用*/
    fun reset() {
        //loadSuccessUrl = "", 不需要设置,设置了会抖动
        checkGif = false
        showAsGifImage = false
        mShowGifTip = false
        override = true
        skipMemoryCache = true

        if (noPlaceholderDrawable) {
            placeholderDrawable = null
            placeholderRes = -1
        } else {
            if (defaultPlaceholderDrawable != null) {
                placeholderDrawable = defaultPlaceholderDrawable
            } else {
                placeholderRes = defaultPlaceholderDrawableRes
            }
        }

        animType = AnimType.DEFAULT
        bitmapTransform = null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //L.d("onSizeChanged $oldw $oldh -> $w $h")
        startLoadUrl()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
    }

    final override fun setImageDrawable(drawable: Drawable?) {
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

        if (TextUtils.isEmpty(url) && !TextUtils.isEmpty(loadSuccessUrl)) {
            setImageDrawable(placeholderDrawable)
        }

        if (url == null) {
            setTagUrl("")
        } else if (url.isNullOrEmpty()) {
            setTagUrl("")
            cancelRequest()
            //setImageResource(placeholderRes)
        } else {
            setTagUrl(url!!)
            if (loadSuccessUrl.isNotEmpty() && TextUtils.equals(url, loadSuccessUrl)) {
                L.d("startLoadUrl 重复加载url -> $url")
            } else if (measuredHeight == 0 ||
                    measuredWidth == 0) {
            } else {
                L.d("startLoadUrl 加载url -> $url \n $loadSuccessUrl")

                val file = File(url)
                if (file.exists()) {
                    loadWithFile(file)
                } else {
                    loadWidthUrl(url!!)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    protected open fun defaultConfig(isGif: Boolean): RequestOptions {
        val requestOptions = if (noPlaceholderDrawable || placeholderDrawable == null) {
            RequestOptions()
        } else {
            RequestOptions.placeholderOf(placeholderDrawable)
                    .error(placeholderDrawable)
        }
        requestOptions.skipMemoryCache(skipMemoryCache)

        /**DiskCacheStrategy.SOURCE：缓存原始数据，
         * DiskCacheStrategy.RESULT：缓存变换(如缩放、裁剪等)后的资源数据，
         * DiskCacheStrategy.NONE：什么都不缓存
         * ，DiskCacheStrategy.ALL：缓存SOURC和RESULT。
         * 默认采用DiskCacheStrategy.RESULT策略，
         * 对于download only操作要使用DiskCacheStrategy.SOURCE。*/
        if (isGif) {
            requestOptions.diskCacheStrategy(DiskCacheStrategy.DATA).priority(Priority.HIGH)
        } else {
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.NORMAL)
        }

        if (bitmapTransform != null) {
            requestOptions.transform(bitmapTransform!!)
        } else {
            requestOptions.dontTransform()
        }

        when (scaleType) {
            ScaleType.CENTER_CROP -> requestOptions.centerCrop()
            ScaleType.CENTER_INSIDE -> requestOptions.centerInside()
            ScaleType.CENTER -> requestOptions.centerInside()
            ScaleType.FIT_CENTER -> requestOptions.fitCenter()
        }

        when (animType) {
            AnimType.NONE, AnimType.TRANSITION -> requestOptions.dontAnimate()
            AnimType.CROSS_FADE -> {
                //requestOptions //默认就是这个动画
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

    @SuppressLint("CheckResult")
    private fun intoConfigFile(request: RequestBuilder<File>, requestOptions: RequestOptions) {
        initListener(request)

        intoConfig {
            if (override) {
                requestOptions.override(measuredWidth, measuredHeight)
            }
            request.apply(requestOptions)
            if (animType == AnimType.TRANSITION) {
                request.into(object : SimpleTarget<File>() {
                    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                        if (placeholderDrawable != null) {
                            setImageDrawable(placeholderDrawable!!, BitmapDrawable(resources, resource.absolutePath))
                        }
                    }
                })
            } else {
                request.into(this)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun intoConfigGifDrawable(request: RequestBuilder<GifDrawable>, requestOptions: RequestOptions) {
        initListener(request)

        intoConfig {
            if (override) {
                requestOptions.override(measuredWidth, measuredHeight)
            }
            request.apply(requestOptions)
            if (animType == AnimType.TRANSITION) {
                request.into(object : SimpleTarget<GifDrawable>() {
                    override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
                        if (placeholderDrawable != null) {
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

    @SuppressLint("CheckResult")
    private fun intoConfigDrawable(request: RequestBuilder<Drawable>, requestOptions: RequestOptions) {
        initListener(request)

        intoConfig {
            if (override) {
                requestOptions.override(measuredWidth, measuredHeight)
            }
            request.apply(requestOptions)
            if (animType == AnimType.TRANSITION) {
                request.into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        if (placeholderDrawable != null) {
                            setImageDrawable(placeholderDrawable!!, resource)
                        }
                    }
                })
            } else {
                request.into(this)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun intoConfigBitmap(request: RequestBuilder<Bitmap>, requestOptions: RequestOptions) {
        initListener(request)

        intoConfig {
            if (override) {
                requestOptions.override(measuredWidth, measuredHeight)
            }
            request.apply(requestOptions)
            if (animType == AnimType.TRANSITION) {
                request.into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if (placeholderDrawable != null) {
                            setImageDrawable(placeholderDrawable!!, BitmapDrawable(resources, resource))
                        }
                    }
                })
            } else {
                request.into(this)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun <T> initListener(request: RequestBuilder<T>) {
        request.listener(object : RequestListener<T> {
            override fun onResourceReady(resource: T?, model: Any?, target: Target<T>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                L.d("onResourceReady -> \n$model\n$url")
                loadSuccessUrl = url!!
                return false
            }

            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<T>?, isFirstResource: Boolean): Boolean {
                L.d("onLoadFailed -> $url")
                loadSuccessUrl = ""
                return false
            }
        })
    }

    /**当前View加载x成功的url, 下次再次加载相同url, 不刷新界面*/
    var loadSuccessUrl: String = ""
        set(value) {
            field = value
            setTag(R.id.tag_success_url, field)
        }
        get() {
            val tag = getTag(R.id.tag_success_url)
            return if (tag == null) {
                ""
            } else {
                tag as String
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
            Glide.with(this).clear(this)
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

    @SuppressLint("CheckResult")
    private fun showGifFile(file: File) {
        if (context is Activity) {
            if ((context as Activity).isDestroyed) {
                return
            }
        }

        val request = Glide.with(context)
                .downloadOnly()
                .load(file)

        initListener(request)

        intoConfig {
            request.apply(defaultConfig(true))
            request.into(object : SimpleTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    resource.let {
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
        if (context is Activity) {
            if ((context as Activity).isDestroyed) {
                return
            }
        }

        val request = Glide.with(context)
                .asBitmap()
                .load(file)

        intoConfigBitmap(request, defaultConfig(false))
    }

    private fun showFile(file: File) {
        if (context is Activity) {
            if ((context as Activity).isDestroyed) {
                return
            }
        }

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

        if (context is Activity) {
            if ((context as Activity).isDestroyed) {
                return
            }
        }

        val request = Glide.with(context)
                .load(url)

        intoConfigDrawable(request, defaultConfig(false))
    }

    private fun loadJpegUrl(url: String) {
        if (!canLoad(url)) {
            return
        }

        if (context is Activity) {
            if ((context as Activity).isDestroyed) {
                return
            }
        }

        val request = Glide.with(context)
                .asBitmap()
                .load(url)

        intoConfigBitmap(request, defaultConfig(false))
    }

    @SuppressLint("CheckResult")
    private fun loadGifUrl(url: String) {
        if (!canLoad(url)) {
            return
        }

        if (context is Activity) {
            if ((context as Activity).isDestroyed) {
                return
            }
        }

        val request = Glide.with(context)
                .downloadOnly()
                .load(url)

        initListener(request)

        intoConfig {
            request.apply(defaultConfig(true))
            request.into(object : SimpleTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    resource.let {
                        try {
                            //L.i("call: 加载Gif 本地地址 -> ${resource.absolutePath}")
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

    val debugPaint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.strokeJoin = Paint.Join.ROUND
        p.strokeCap = Paint.Cap.ROUND
        p.style = Paint.Style.STROKE
        p.color = Color.WHITE
        p.textSize = 9 * density
        p
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if (DEBUG_SHOW) {
            val urlText = "url:$url"
            var startPosition = 0
            val urlTextHeight = textHeight(debugPaint)

            var startTop = urlTextHeight
            val oneWidth = textWidth(debugPaint, urlText.substring(0, 1))
            for (i in 0..urlText.length) {
                val subSequence = urlText.subSequence(startPosition, i).toString()
                if (oneWidth * subSequence.length > measuredWidth - oneWidth || i == urlText.length) {
                    canvas?.drawText(subSequence, 0f, startTop, debugPaint)
                    startTop += urlTextHeight
                    startPosition = i
                }
            }

            val sizeText = "w:$measuredWidth h:$measuredHeight"
            canvas?.drawText(sizeText, 0f, startTop, debugPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Glide.get(context).clearMemory()
        try {
            if (drawable is BitmapDrawable) {
                //(drawable as BitmapDrawable).bitmap.recycle()
            } else if (drawable is pl.droidsonroids.gif.GifDrawable) {
                //(drawable as pl.droidsonroids.gif.GifDrawable).recycle()//2017-11-24 列表复用, 回收会崩溃
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDrawError() {
        super.onDrawError()
        loadSuccessUrl = ""
    }
}

