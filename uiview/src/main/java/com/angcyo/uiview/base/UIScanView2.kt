package com.angcyo.uiview.base

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.angcyo.library.utils.L
import com.angcyo.library.utils.L.TAG
import com.angcyo.uiview.R
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.container.UIParam
import com.angcyo.uiview.dialog.UILoading
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.net.RException
import com.angcyo.uiview.net.RFunc
import com.angcyo.uiview.net.RSubscriber
import com.angcyo.uiview.net.Rx
import com.angcyo.uiview.resources.AnimUtil
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.T_
import com.angcyo.uiview.view.IView
import com.angcyo.uiview.widget.RImageCheckView
import com.angcyo.zxingzbar.*
import com.angcyo.zxingzbar.camera.CameraManager
import com.angcyo.zxingzbar.encode.QRCodeEncoder.HINTS_DECODE
import com.google.zxing.*
import com.google.zxing.client.android.DecodeHandlerJni
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.tbruyelle.rxpermissions.RxPermissions
import net.sourceforge.zbar.Image
import net.sourceforge.zbar.ImageScanner
import java.io.IOException

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：使用zxingzbar 扫描库 https://github.com/XieZhiFa/ZxingZbar
 * 创建人员：Robi
 * 创建时间：2018/02/27 13:42
 * 修改人员：Robi
 * 修改时间：2018/02/27 13:42
 * 修改备注：
 * Version: 1.0.0
 */
open class UIScanView2 : UIContentView(), IActivity, SurfaceHolder.Callback {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        hasSurface = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!")
        }
        if (!hasSurface) {
            hasSurface = true

            if (iViewShowState == IView.IViewShowState.STATE_VIEW_SHOW) {
                initCamera(holder)
            }
        }
    }

    private val VIBRATE_DURATION: Long = 50

    override fun getCameraManager(): CameraManager {
        return cameraManager
    }

    override fun getHandler(): Handler? {
        return handler
    }

    override fun getActivity(): Activity {
        return mActivity
    }

    override fun getViewfinderView(): ViewfinderView {
        return viewfinderView
    }

    override fun setResult(resultCode: Int, data: Intent?) {

    }

    override fun finish() {

    }

    override fun getPackageManager(): PackageManager {
        return mActivity.packageManager
    }

    override fun startActivity(intent: Intent?) {

    }

    override fun drawViewfinder() {
        viewfinderView.drawViewfinder()
    }

    var onScanResult: ((String) -> Unit)? = null

    /**处理扫码返回的结果*/
    override fun handleDecode(data: String?) {
        L.e("call: handleDecode ->$data")
        var result = ""
        if (TextUtils.isEmpty(data)) {
            //T_.ok("unknown")
            result = ""
        } else {
            //playVibrate()
            result = data!!
            //T_.ok(data)
        }
        beepManager.playBeepSoundAndVibrate()
        onHandleDecode(result)
    }

    open fun onHandleDecode(data: String) {
        finishIView {
            onScanResult?.invoke(data)
        }
        //重复扫描请调用此方法
        //scanAgain()
    }

    /**打开失败*/
    open fun onDisplayError() {
        T_.error("请开启摄像头权限.")
        finishIView()
    }

    /**重新扫描*/
    open fun scanAgain() {
        restartPreviewAfterDelay(1000L)
    }

    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar()
                .setTitleString("")
                .setFloating(true)
                .setShowBackImageView(true)
                .setTitleBarBGColor(Color.TRANSPARENT)
    }

    override fun inflateContentLayout(baseContentLayout: ContentLayout, inflater: LayoutInflater) {
        inflate(R.layout.base_view_rscan_layout2)
    }

    private lateinit var cameraManager: CameraManager
    private var handler: CaptureActivityHandler? = null
    private lateinit var viewfinderView: ViewfinderView
    private lateinit var surfaceView: SurfaceView

    private var lastResult: Result? = null
    private var savedResultToShow: Result? = null

    //需要解码的格式
    private var decodeFormats: Collection<BarcodeFormat>? = null
    private lateinit var ambientLightManager: AmbientLightManager
    private lateinit var beepManager: BeepManager
    private lateinit var inactivityTimer: InactivityTimer
    private var decodeHints: Map<DecodeHintType, *>? = null

    private var hasSurface: Boolean = false

    override fun onViewCreate(rootView: View?, param: UIParam?) {
        super.onViewCreate(rootView, param)

        hasSurface = false
        inactivityTimer = InactivityTimer(mActivity)
        beepManager = BeepManager(mActivity)
        ambientLightManager = AmbientLightManager(mActivity)

        cameraManager = CameraManager(mActivity.applicationContext)
    }

    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()

        val imageCheckView = mViewHolder.v<RImageCheckView>(R.id.base_light_switch_view)

        //打开闪光灯
        imageCheckView.setOnCheckedChangeListener { buttonView, isChecked -> openFlashlight(isChecked) }

        //从相册选择
        mViewHolder.click(R.id.base_photo_selector_view) { onSelectorPhotoClick() }

        surfaceView = v(R.id.base_preview_view)
        viewfinderView = v(R.id.base_viewfinder_view)
        viewfinderView.laserColor = (SkinHelper.getSkin().themeSubColor)

        viewfinderView.setCameraManager(cameraManager)

        val surfaceHolder = surfaceView.holder
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder)
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this)
        }
    }

    /**
     * 震动一下, 需要权限VIBRATE
     */
    protected open fun playVibrate() {
        val vibrator = mActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VIBRATE_DURATION)
    }

    /**
     * 打开闪光灯
     */
    protected open fun openFlashlight(open: Boolean) {
        cameraManager.setTorch(open)
    }

    /**
     * 打开图片二维码
     */
    protected open fun onSelectorPhotoClick() {
        //ImagePickerHelper.startImagePicker(mActivity, false, true, false, false, 0)
    }

    override fun onViewShow(bundle: Bundle?, fromClz: Class<*>?) {
        super.onViewShow(bundle, fromClz)
        keepScreenOn(true)

        handler = null
        lastResult = null

        beepManager.updatePrefs()
        ambientLightManager.start(cameraManager)

        inactivityTimer.onResume()

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            //initCamera(surfaceView.holder)
            initCamera(surfaceView.holder)
        }
    }

    override fun onViewHide() {
        super.onViewHide()
        keepScreenOn(false)

        if (handler != null) {
            handler?.quitSynchronously()
            handler = null
        }
        inactivityTimer.onPause()
        ambientLightManager.stop()
        beepManager.close()
        cameraManager.closeDriver()
        //historyManager = null; // Keep for onActivityResult
    }

    override fun onViewUnload() {
        super.onViewUnload()
    }

    override fun onViewUnloadDelay() {
        super.onViewUnloadDelay()
        surfaceView.holder.removeCallback(this)
        inactivityTimer.shutdown()
    }

    private var characterSet: String? = null

    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        fun doIt() {
            if (surfaceHolder == null) {
                throw IllegalStateException("No SurfaceHolder provided")
            }
            if (cameraManager.isOpen) {
                Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?")
                return
            }
            try {
                cameraManager.openDriver(surfaceHolder)
                // Creating the handler starts the preview, which can also throw a RuntimeException.
                if (handler == null) {
                    handler = CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager)
                }
                decodeOrStoreSavedBitmap(null, null)
            } catch (ioe: IOException) {
                Log.w(TAG, ioe)
                displayFrameworkBugMessageAndExit()
            } catch (e: RuntimeException) {
                // Barcode Scanner has seen crashes in the wild of this variety:
                // java.?lang.?RuntimeException: Fail to connect to camera service
                Log.w(TAG, "Unexpected error initializing camera", e)
                displayFrameworkBugMessageAndExit()
            }
        }

        RxPermissions(mActivity)
                .request(Manifest.permission.CAMERA)
                .subscribe { aBoolean ->
                    if (aBoolean!!) {
                        if (surfaceView.background is ColorDrawable &&
                                (surfaceView.background as ColorDrawable).color == 0) {

                        } else {
                            AnimUtil.startArgb(surfaceView, Color.BLACK, Color.TRANSPARENT, 200)
                        }
                        doIt()
                    } else {
                        displayFrameworkBugMessageAndExit()
                        finishIView()
                    }
                }
    }

    private fun decodeOrStoreSavedBitmap(bitmap: Bitmap?, result: Result?) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result
        } else {
            if (result != null) {
                savedResultToShow = result
            }
            if (savedResultToShow != null) {
                val message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow)
                handler?.sendMessage(message)
            }
            savedResultToShow = null
        }
    }

    fun restartPreviewAfterDelay(delayMS: Long) {
        if (handler != null) {
            handler?.sendEmptyMessageDelayed(R.id.restart_preview, delayMS)
        }
    }

    /**打开摄像头出错*/
    private fun displayFrameworkBugMessageAndExit() {
        onDisplayError()
    }

    fun scanPicture(picturePath: String) {
        UILoading.show2(mParentILayout).setLoadingTipText("正在识别...").setCanCancel(false)
        Rx.base(object : RFunc<String>() {
            override fun onFuncCall(): String {
                return scanPictureFun(mActivity, picturePath)
            }
        }, object : RSubscriber<String>() {
            override fun onSucceed(bean: String) {
                handleDecode(bean)
            }

            override fun onEnd(isError: Boolean, isNoNetwork: Boolean, e: RException?) {
                super.onEnd(isError, isNoNetwork, e)
                UILoading.hide()
            }
        })
    }

    fun scanPicture(scanBitmap: Bitmap) {
        UILoading.show2(mParentILayout).setLoadingTipText("正在识别...").setCanCancel(false)
        Rx.base(object : RFunc<String>() {
            override fun onFuncCall(): String {
                return scanPictureFun1(scanBitmap)
            }
        }, object : RSubscriber<String>() {
            override fun onSucceed(bean: String) {
                handleDecode(bean)
            }

            override fun onEnd(isError: Boolean, isNoNetwork: Boolean, e: RException?) {
                super.onEnd(isError, isNoNetwork, e)
                UILoading.hide()
            }
        })
    }


    companion object {

        /**第二层 扫描*/
        private fun scanPictureFun2(scanBitmap: Bitmap): String {
            val source = PlanarYUVLuminanceSource(
                    rgb2YUV(scanBitmap),
                    scanBitmap.width,
                    scanBitmap.height,
                    0, 0,
                    scanBitmap.width,
                    scanBitmap.height,
                    false)

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            var result: Result?
            try {
                result = reader.decode(binaryBitmap, HINTS_DECODE)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    result = reader.decode(BinaryBitmap(GlobalHistogramBinarizer(source)), HINTS_DECODE)
                    return result.text ?: ""
                } catch (e: Exception) {
                    return scanPictureFun3(scanBitmap)
                }
            }
            return result.text ?: ""
        }

        /**第三层 扫描方法, 使用 ZBar*/
        private fun scanPictureFun3(scanBitmap: Bitmap): String {
            val barcode = Image(scanBitmap.width, scanBitmap.height, "Y800")
//            Debug.logTimeStart("rgb2YUV")
//            barcode.data = rgb2YUV(scanBitmap) //BmpUtil.generateBitstream(scanBitmap, Bitmap.CompressFormat.JPEG, 100)
//            Debug.logTimeEnd("rgb2YUV")

            val width = scanBitmap.width
            val height = scanBitmap.height
            val pixels = IntArray(width * height)
            scanBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
//            Debug.logTimeStart("rgb2YUV_2")
            barcode.data = DecodeHandlerJni.rgb2yuv(pixels, width, height)
//            Debug.logTimeEnd("rgb2YUV_2")

            val mImageScanner = ImageScanner()
            val result = mImageScanner.scanImage(barcode)
            var resultQRcode: String? = null
            if (result != 0) {
                val symSet = mImageScanner.results
                for (sym in symSet)
                    resultQRcode = sym.data
            }
            return resultQRcode ?: ""
        }

        /**第一层 扫描*/
        fun scanPictureFun1(bitmap: Bitmap?): String {
            var result: Result? = null
            var source: RGBLuminanceSource? = null
            try {
                val width = bitmap!!.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                source = RGBLuminanceSource(width, height, pixels)
                result = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)), HINTS_DECODE)
                return result!!.text
            } catch (e: Exception) {
                e.printStackTrace()
                if (source != null) {
                    try {
                        result = MultiFormatReader().decode(BinaryBitmap(GlobalHistogramBinarizer(source)), HINTS_DECODE)
                        return result!!.text
                    } catch (e2: Throwable) {
                        e2.printStackTrace()
                        return scanPictureFun2(bitmap!!)
                    }
                }
                return ""
            }
        }

        fun scanPictureFun(context: Context, bitmapPath: String): String {
            return scanPictureFun1(createBitmap(context, bitmapPath))
        }

        /**
         * 根据图片路径创建一个合适大小的位图
         *
         * @param filePath
         * @return
         */
        fun createBitmap(context: Context, filePath: String): Bitmap {
            val dm = context.resources.displayMetrics
            val displayWidth = dm.widthPixels

            val opts = BitmapFactory.Options()            //实例位图设置
            opts.inJustDecodeBounds = true                                        //表示不全将图片加载到内存,而是读取图片的信息2
            BitmapFactory.decodeFile(filePath, opts)                            //首先读取图片信息,并不加载图片到内存,防止内存泄露
            val imageWidth = opts.outWidth                                        //获取图片的宽
            val imageHeight = opts.outHeight                                    //获取图片的高

            var scale = 1
            if (imageWidth > displayWidth || imageHeight > displayWidth) {
                val scaleX = imageWidth / displayWidth                             //计算图片,宽度的缩放率
                val scaleY = imageHeight / displayWidth                            //计算图片,高度的缩放率

                if ((scaleX > scaleY) and (scaleY >= 1)) {                                //如果图片的宽比高大,则采用宽的比率
                    scale = scaleX
                }

                if ((scaleY > scaleX) and (scaleX >= 1)) {                                //如果图片的高比宽大,则采用高的比率
                    scale = scaleY
                }
            }

            opts.inJustDecodeBounds = false                                    //表示加载图片到内存
            opts.inSampleSize = scale                                            //设置Bitmap的采样率

            return BitmapFactory.decodeFile(filePath, opts)
        }

        fun rgb2YUV(bitmap: Bitmap): ByteArray {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val len = width * height
            val yuv = ByteArray(len * 3 / 2)
            var y: Int
            var u: Int
            var v: Int
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val rgb = pixels[i * width + j] and 0x00FFFFFF

                    val r = rgb and 0xFF
                    val g = rgb shr 8 and 0xFF
                    val b = rgb shr 16 and 0xFF

                    y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                    u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                    v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128

                    y = if (y < 16) 16 else if (y > 255) 255 else y
                    u = if (u < 0) 0 else if (u > 255) 255 else u
                    v = if (v < 0) 0 else if (v > 255) 255 else v

                    yuv[i * width + j] = y.toByte()
                    //                yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
                    //                yuv[len + (i >> 1) * width + (j & ~1) + 1] = (byte) v;
                }
            }
            return yuv
        }
    }

}