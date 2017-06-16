package com.angcyo.uiview.base;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.angcyo.library.utils.L;
import com.angcyo.rcode.IDecodeCallback;
import com.angcyo.rcode.QRCodeDecoder;
import com.angcyo.rcode.zxing.camera.CameraManager;
import com.angcyo.rcode.zxing.control.AmbientLightManager;
import com.angcyo.rcode.zxing.control.BeepManager;
import com.angcyo.rcode.zxing.decode.CaptureActivityHandler;
import com.angcyo.rcode.zxing.decode.InactivityTimer;
import com.angcyo.rcode.zxing.view.ViewfinderView;
import com.angcyo.uiview.R;
import com.angcyo.uiview.model.TitleBarPattern;
import com.angcyo.uiview.resources.AnimUtil;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.utils.T_;
import com.angcyo.uiview.widget.RImageCheckView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import rx.functions.Action1;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：二维码扫描界面
 * 创建人员：Robi
 * 创建时间：2017/04/18 14:48
 * 修改人员：Robi
 * 修改时间：2017/04/18 14:48
 * 修改备注：
 * Version: 1.0.0
 */
public class UIScanView extends UIContentView implements SurfaceHolder.Callback, IDecodeCallback {
    private static final long VIBRATE_DURATION = 50;
    protected BeepManager beepManager;
    protected Action1<String> mResultAction;
    ViewfinderView mViewfinderView;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private AmbientLightManager ambientLightManager;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private Collection<BarcodeFormat> decodeFormats;
    private boolean vibrate;
    private String characterSet;
    private Map<DecodeHintType, ?> decodeHints;
    private Result savedResultToShow;
    private SurfaceView mSurfaceView;


    public UIScanView() {
    }

    public UIScanView(Action1<String> resultAction) {
        mResultAction = resultAction;
    }

    @Override
    protected TitleBarPattern getTitleBar() {
        return super.getTitleBar()
                .setTitleString("")
                .setFloating(true)
                .setShowBackImageView(true)
                .setTitleBarBGColor(Color.TRANSPARENT);
    }

//    @Override
//    public int getDefaultBackgroundColor() {
//        return getColor(R.color.default_base_black);
//    }

    @Override
    protected void inflateContentLayout(RelativeLayout baseContentLayout, LayoutInflater inflater) {
        inflate(R.layout.base_view_rscan_layout);
    }

    @Override
    protected void initOnShowContentLayout() {
        super.initOnShowContentLayout();
        mViewfinderView = mViewHolder.v(R.id.base_viewfinder_view);
        mSurfaceView = mViewHolder.v(R.id.base_preview_view);

        RImageCheckView imageCheckView = mViewHolder.v(R.id.base_light_switch_view);
        imageCheckView.setOnCheckedChangeListener(new RImageCheckView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RImageCheckView buttonView, boolean isChecked) {
                openFlashlight(isChecked);
            }
        });

        mViewHolder.click(R.id.base_photo_selector_view, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectorPhotoClick();
            }
        });

        cameraManager = new CameraManager(mActivity);
        mViewfinderView.setCameraManager(cameraManager);
        mViewfinderView.setCenterColor(SkinHelper.getSkin().getThemeSubColor());

        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        //surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(mActivity);
        ambientLightManager = new AmbientLightManager(mActivity);
        beepManager = new BeepManager(mActivity);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            return;
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats,
                        decodeHints, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            displayFrameworkBugMessageAndExit();
        }
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler,
                        IDecodeCallback.decode_succeeded,
                        savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        //T_.error("抱歉，相机出现问题，您可能需要重启设备");
        T_.error("请检查相机权限.");
    }

    @Override
    public void onViewShow(Bundle bundle) {
        super.onViewShow(bundle);
        keepScreenOn(true);

        final SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (hasSurface) {
            new RxPermissions(mActivity)
                    .request(Manifest.permission.CAMERA)
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            if (aBoolean) {
                                AnimUtil.startArgb(mSurfaceView, Color.BLACK, Color.TRANSPARENT, 200);
                                initCamera(surfaceHolder);
                            } else {
                                displayFrameworkBugMessageAndExit();
                                finishIView();
                            }
                        }
                    });
        }

        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);

        inactivityTimer.onResume();
        vibrate = true;
        decodeFormats = null;
        characterSet = null;
    }

    @Override
    public void onViewHide() {
        super.onViewHide();
        keepScreenOn(false);
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    @Override
    public void onViewUnload() {
        super.onViewUnload();
        inactivityTimer.shutdown();
        mViewfinderView.recycleLineDrawable();
    }

    /**
     * 保持屏幕常亮
     */
    private void keepScreenOn(boolean keep) {
        Window window = mActivity.getWindow();
        if (keep) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 震动一下, 需要权限VIBRATE
     */
    private void playVibrate() {
        Vibrator vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATE_DURATION);
    }

    /**
     * 打开闪光灯
     */
    protected void openFlashlight(boolean open) {
        cameraManager.setTorch(open);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            //initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public ViewfinderView getViewfinderView() {
        return mViewfinderView;
    }

    @Override
    public void drawViewfinder() {
        if (mViewfinderView == null) {
            return;
        }
        mViewfinderView.drawViewfinder();
    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();

        //如果需要多次扫码, 请发送事件
        //Message message = Message.obtain(handler, IDecodeCallback.decode_failed);
        //handler.sendMessage(message);

        String result = rawResult.getText();
        L.e("二维码: " + result);

        onHandleDecode(result);
    }

    /**
     * 重新扫描
     */
    protected void scanAgain() {
        if (handler == null ||
                getIViewShowState() != IViewShowState.STATE_VIEW_SHOW) {
            return;
        }
        Message message = Message.obtain(handler, IDecodeCallback.decode_failed);
        handler.sendMessage(message);
    }

    /**
     * 重写此方法, 自定义扫描之后的事件
     */
    protected void onHandleDecode(String result) {
        if (TextUtils.isEmpty(result)) {
            result = "unknown";
        }
        //playVibrate();
        beepManager.playBeepSoundAndVibrate();
        //T_.show(result);
        finishIView();
        if (mResultAction != null) {
            mResultAction.call(result);
        }
    }

    /**
     * 打开图片二维码
     */
    protected void onSelectorPhotoClick() {
        //ImagePickerHelper.startImagePicker(mActivity, false, true, false, false, 0);
    }

    /**
     * 扫描图片二维码
     */
    public void scanPicture(String picturePath) {
        onHandleDecode(QRCodeDecoder.syncDecodeQRCode(picturePath));
    }
}
