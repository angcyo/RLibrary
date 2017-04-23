package com.angcyo.uiview.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by angcyo on 2017-04-23.
 */

public class ClipHelper {

    private static final long ANIM_TIME = 500;
    ValueAnimator mClipEnterValueAnimator, mClipExitValueAnimator;
    /**
     * 开始的坐标, 和半径
     */
    float clipStartX = 0f, clipStartY = 0f, clipStartRadius = 100f;
    Path clipPath = new Path();
    boolean enableClip = false;
    OnEndListener mEndListener;
    boolean isClipEnd = true;
    private View mTargetView;

    public ClipHelper(View targetView) {
        mTargetView = targetView;
    }

    /**
     * 必须调用此方法, 才会有效果
     */
    public void draw(Canvas canvas) {
        if (enableClip) {
            canvas.clipPath(clipPath);
        }
    }

    /**
     * 开始进入clip, 请在targetView, 可以正常拿到宽高的时候, 调用.
     *
     * @param view 自动计算view位置, 开始clip动画
     */
    public void startEnterClip(View view, OnEndListener listener) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);

        int height = view.getMeasuredHeight();
        int width = view.getMeasuredWidth();

        int r = Math.max(width, height) / 2;
        int x = rect.left + width / 2;
        int y = rect.top + height / 2;

        startEnterClip(x, y, r, listener);
    }

    public void startEnterClip(float startX, float startY, float startR, OnEndListener listener) {
        mEndListener = listener;

        if (!isClipEnd) {
            return;
        }
        isClipEnd = false;

        clipStartRadius = startR;
        clipStartX = startX;
        clipStartY = startY;

        initEnterAnimator();
        mClipEnterValueAnimator.start();
    }

    /**
     * 开始退出clip
     */
    public void startExitClip(OnEndListener listener) {
        mEndListener = listener;

        if (!isClipEnd) {
            return;
        }
        isClipEnd = false;
        initExitAnimator();
        mClipExitValueAnimator.start();
    }

    /**
     * 是否激活clip
     */
    public void setEnableClip(boolean enableClip) {
        this.enableClip = enableClip;
    }

    /**
     * 判断clip是否结束
     */
    public boolean isClipEnd() {
        return isClipEnd;
    }

    //勾股定理
    private float c(float a, float b) {
        return (float) Math.sqrt(a * a + b * b);
    }

    private void updateClipPath(float radius) {
        clipPath.reset();
        clipPath.addCircle(clipStartX, clipStartY, radius, Path.Direction.CW);
        mTargetView.postInvalidate();
    }

    private float calcEndRadius() {
        int viewWidth = mTargetView.getMeasuredWidth();
        int viewHeight = mTargetView.getMeasuredHeight();
        //开始点与左上角的距离
        float rLT = c(Math.abs(clipStartX), Math.abs(clipStartY));
        float rRT = c(Math.abs(viewWidth - clipStartX), Math.abs(clipStartY));
        float rLB = c(Math.abs(clipStartX), Math.abs(viewHeight - clipStartY));
        float rRB = c(Math.abs(viewWidth - clipStartX), Math.abs(viewHeight - clipStartY));

        return Math.max(Math.max(Math.max(rLT, rRT), rLB), rRB);
    }

    private void initEnterAnimator() {
        final float endRadius = calcEndRadius();
//        if (mClipEnterValueAnimator == null) {
        mClipEnterValueAnimator = ObjectAnimator.ofFloat(clipStartRadius, endRadius);
        mClipEnterValueAnimator.setInterpolator(new AccelerateInterpolator());
//            mClipEnterValueAnimator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        mClipEnterValueAnimator.setDuration(ANIM_TIME);
        mClipEnterValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float progress = (float) valueAnimator.getAnimatedValue();
//                    Log.d("angcyo", "progress-->" + progress);
                updateClipPath(clipStartRadius + progress);
            }
        });
        mClipEnterValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isClipEnd = true;
                if (mEndListener != null) {
                    mEndListener.onEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
//        }
    }

    private void initExitAnimator() {
        final float endRadius = calcEndRadius();

//        if (mClipExitValueAnimator == null) {
        mClipExitValueAnimator = ObjectAnimator.ofFloat(endRadius, clipStartRadius);
//            mClipExitValueAnimator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        mClipExitValueAnimator.setDuration(ANIM_TIME);
        mClipExitValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float progress = (float) valueAnimator.getAnimatedValue();
                updateClipPath(progress);
            }
        });
        mClipExitValueAnimator.setInterpolator(new AccelerateInterpolator());
        mClipExitValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isClipEnd = true;
                if (mEndListener != null) {
                    mEndListener.onEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
//        }
    }


    public interface OnEndListener {
        void onEnd();
    }
}
