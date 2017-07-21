package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：自带点击效果的ImageView
 * 创建人员：Robi
 * 创建时间：2017/03/10 11:45
 * 修改人员：Robi
 * 修改时间：2017/03/10 11:45
 * 修改备注：
 * Version: 1.0.0
 */
public class RImageView extends AppCompatImageView {

    /**
     * 播放按钮图片
     */
    Drawable mPlayDrawable;
    private boolean isAttachedToWindow;
    private boolean mShowMask;//显示click时的蒙层

    /**
     * 当调用{@link android.widget.ImageView#setImageDrawable(Drawable)} 时, 是否显示过渡动画
     */
    private boolean showDrawableAnim = true;
    private boolean mShowGifTip;
    private Drawable mGifTipDrawable;
    private float mDensity;

    public RImageView(Context context) {
        this(context, null);
    }

    public RImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 居中剪切图片
     */
    public static Bitmap centerCrop(Resources res, Bitmap bitmap, int width, int height) {
        if (bitmap == null) {
            return null;
        }

        if (width == 0 || height == 0) {
            return bitmap;
        }

        Bitmap result = Bitmap.createBitmap(width, height, bitmap.getConfig() != null
                ? bitmap.getConfig() : Bitmap.Config.ARGB_8888);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1 && result != null) {
            result.setHasAlpha(bitmap.hasAlpha());
        }

        Matrix matrix = new Matrix();

        float scale;
        float dx = 0, dy = 0;

        final int dwidth = bitmap.getWidth();
        final int dheight = bitmap.getHeight();

        final int vwidth = width;
        final int vheight = height;

        if (dwidth * vheight > vwidth * dheight) {
            scale = (float) vheight / (float) dheight;
            dx = (vwidth - dwidth * scale) * 0.5f;
        } else {
            scale = (float) vwidth / (float) dwidth;
            dy = (vheight - dheight * scale) * 0.5f;
        }

        matrix.setScale(scale, scale);
        matrix.postTranslate(Math.round(dx), Math.round(dy));

//        Canvas canvas = new Canvas(result);
//        canvas.concat(matrix);
//        BitmapDrawable bitmapDrawable = new BitmapDrawable(res, bitmap);
//        bitmapDrawable.setBounds(0, 0, dwidth, dheight);
//        bitmapDrawable.draw(canvas);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, matrix, paint);

        return result;
    }

    /**
     * 复制 imageView的Drawable
     */
    public static Drawable copyDrawable(final ImageView imageView) {
        if (imageView == null) {
            return null;
        }
        return copyDrawable(imageView.getDrawable());
    }

    public static Drawable copyDrawable(Drawable drawable) {
        Drawable result = null;
        if (drawable != null) {
            if (drawable instanceof TransitionDrawable) {
                TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
                drawable = transitionDrawable.getDrawable(transitionDrawable.getNumberOfLayers() - 1);
            }

            if (drawable == null) {
                return null;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Rect bounds = drawable.getBounds();
                int width = bounds.width();
                int height = bounds.height();

                if (width == 0 || height == 0) {
                    return drawable;
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.draw(canvas);
                result = new BitmapDrawable(RApplication.getApp().getResources(), bitmap);
                result.setBounds(bounds);
            } else {
                Drawable.ConstantState constantState = drawable.mutate().getConstantState();
                if (constantState != null) {
                    result = constantState.newDrawable();
                }
            }
        }
        return result;
    }

    private void initView() {
        mDensity = getResources().getDisplayMetrics().density;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isClickable()) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setColor();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                clearColor();
                break;
        }

        super.onTouchEvent(event);
        return true;
    }

    public void setColor(@ColorInt int color) {
        setColor(getDrawable(), color);
    }

    private void setColor(Drawable drawable, @ColorInt int color) {
        if (drawable != null) {
            if (showMask(drawable)) {
//                LayerDrawable layerDrawable = (LayerDrawable) drawable;
//                int numberOfLayers = layerDrawable.getNumberOfLayers();
////                if (numberOfLayers > 0) {
////                    setColor((layerDrawable).getDrawable(numberOfLayers - 1), color);
////                }
//                for (int i = 0; i < numberOfLayers; i++) {
//                    setColor((layerDrawable).getDrawable(i), color);
//                }

                mShowMask = true;
                postInvalidate();
//                layerDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            } else {
                drawable.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    private boolean showMask(Drawable drawable) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || drawable instanceof LayerDrawable;
    }

    private void clearColor(Drawable drawable) {
        if (drawable != null) {
            if (showMask(drawable)) {
//                LayerDrawable layerDrawable = (LayerDrawable) drawable;
//                int numberOfLayers = layerDrawable.getNumberOfLayers();
//                if (numberOfLayers > 0) {
//                    clearColor((layerDrawable).getDrawable(numberOfLayers - 1));
//                }
                mShowMask = false;
                postInvalidate();
//                layerDrawable.clearColorFilter();
            } else {
                drawable.mutate().clearColorFilter();
            }
        }
    }

    /**
     * 设置混合颜色
     */
    public void setColor() {
        setColor(Color.GRAY);
    }

    public void clearColor() {
        clearColor(getDrawable());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearColor();
        //setImageBitmap(null);
        isAttachedToWindow = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPlayDrawable != null) {
            int height = getMeasuredHeight() / 2;
            int width = getMeasuredWidth() / 2;
            int w = mPlayDrawable.getIntrinsicWidth() / 2;
            int h = mPlayDrawable.getIntrinsicHeight() / 2;
            mPlayDrawable.setBounds(width - w, height - h, width + w, height + h);
            mPlayDrawable.draw(canvas);
        }
        if (mShowGifTip) {
            ensureGifTipDrawable();
            canvas.save();
            int offset = (int) (2 * mDensity);
            canvas.translate(getMeasuredWidth() - mGifTipDrawable.getIntrinsicWidth() - offset,
                    getMeasuredHeight() - mGifTipDrawable.getIntrinsicHeight() - offset);
            mGifTipDrawable.draw(canvas);
            canvas.restore();
        }
        if (mShowMask) {
            canvas.drawColor(Color.parseColor("#80000000"));
        }
    }

    private void ensureGifTipDrawable() {
        if (mGifTipDrawable == null) {
            mGifTipDrawable = ContextCompat.getDrawable(getContext(), R.drawable.base_ico_gif);

            mGifTipDrawable.setBounds(0, 0,
                    mGifTipDrawable.getIntrinsicWidth(), mGifTipDrawable.getIntrinsicHeight());
        }
    }

//    @Override
//    public void setImageResource(@DrawableRes int resId) {
//        super.setImageResource(resId);
//    }
//
//    @Override
//    public void setImageBitmap(Bitmap bm) {
//        super.setImageBitmap(bm);
//    }
//
//    @Override
//    public void setImageBitmap(@Nullable Drawable drawable) {
//        if (showDrawableAnim) {
//            Drawable drawable1 = getDrawable();
//            final TransitionDrawable td = new TransitionDrawable(
//                    new Drawable[]{drawable1 == null ? new ColorDrawable(Color.WHITE) : drawable1,
//                            drawable});
//            super.setImageBitmap(td);
//            td.startTransition(300);
//        } else {
//            super.setImageBitmap(drawable);
//        }
//    }

    public void setPlayDrawable(Drawable playDrawable) {
        mPlayDrawable = playDrawable;
        if (isAttachedToWindow) {
            postInvalidate();
        }
    }

    public void setPlayDrawable(@DrawableRes int res) {
        if (res == -1) {
            setPlayDrawable(null);
        } else {
            setPlayDrawable(ContextCompat.getDrawable(getContext(), res));
        }
    }

    public void setShowGifTip(boolean showGifTip) {
        mShowGifTip = showGifTip;
        if (isAttachedToWindow) {
            postInvalidate();
        }
    }

    /**
     * 使用过渡的方式显示Drawable
     */
    public void setImageDrawable(@Nullable Drawable fromDrawable, @Nullable Drawable toDrawable) {
        final TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                fromDrawable, toDrawable});
        super.setImageDrawable(td);
        td.startTransition(300);
    }

    public void setImageBitmap(@Nullable final Drawable fromDrawable, @Nullable final Bitmap toBitmap) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                        fromDrawable, new BitmapDrawable(getResources(),
                        getScaleType() == ScaleType.CENTER_CROP ?
                                centerCrop(getResources(), toBitmap, width, height) :
                                toBitmap)});
                RImageView.super.setImageDrawable(td);
                td.startTransition(300);
            }
        };

        if (width == 0 || height == 0) {
            post(runnable);
        } else {
            runnable.run();
        }
    }

    public void setImageBitmapNoCrop(@Nullable final Drawable fromDrawable, @Nullable final Bitmap toBitmap) {
        final TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                fromDrawable, new BitmapDrawable(getResources(), toBitmap)});
        RImageView.super.setImageDrawable(td);
        td.startTransition(300);
    }

    public Drawable copyDrawable() {
        return copyDrawable(this);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
    }
}
