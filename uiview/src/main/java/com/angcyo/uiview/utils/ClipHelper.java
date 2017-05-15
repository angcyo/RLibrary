package com.angcyo.uiview.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by angcyo on 2017-04-23.
 */

public class ClipHelper {

    public static final int DEFAULT_RADIUS = 100;
    public static int ANIM_TIME = 500;
    ValueAnimator mClipEnterValueAnimator, mClipExitValueAnimator;
    /**
     * 开始的坐标, 和半径
     */
    float clipStartX = 0f, clipStartY = 0f, clipStartRadius = DEFAULT_RADIUS;
    Path clipPath = new Path();
    boolean enableClip = false;
    OnEndListener mEndListener;
    boolean isClipEnd = true;
    private View mTargetView;

    public ClipHelper(View targetView) {
        mTargetView = targetView;
    }

    public static int[] init(View view) {
        if (view == null) {
            return new int[]{ScreenUtil.screenWidth / 2, ScreenUtil.screenHeight / 2, DEFAULT_RADIUS};
        }

        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);

        int height = view.getMeasuredHeight();
        int width = view.getMeasuredWidth();

        int r = Math.min(width, height);// / 2;
        int x = rect.left + width / 2;
        int y = rect.top + height / 2;

        return new int[]{x, y, r};
    }

    public static float calcEndRadius(int maxWidth, int maxHeight, float x, float y) {
        int viewWidth = maxWidth;
        int viewHeight = maxHeight;
        //开始点与左上角的距离
        float rLT = c(Math.abs(x), Math.abs(y));
        float rRT = c(Math.abs(viewWidth - x), Math.abs(y));
        float rLB = c(Math.abs(x), Math.abs(viewHeight - y));
        float rRB = c(Math.abs(viewWidth - x), Math.abs(viewHeight - y));

        return Math.max(Math.max(Math.max(rLT, rRT), rLB), rRB);
    }

    //勾股定理
    private static float c(float a, float b) {
        return (float) Math.sqrt(a * a + b * b);
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
        int x;
        int y;
        int r;
        if (view == null) {
            x = mTargetView.getMeasuredWidth() / 2;
            y = mTargetView.getMeasuredHeight() / 2;
            r = (int) clipStartRadius;
        } else {
            int[] init = init(view);
            x = init[0];
            y = init[1];
            r = init[2];
        }

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

    public void initXYR(float startX, float startY, float startR) {
        clipStartRadius = startR;
        clipStartX = startX;
        clipStartY = startY;
    }

    public void initXYR(View view) {
        int x;
        int y;
        int r;
        if (view == null) {
            x = mTargetView.getMeasuredWidth() / 2;
            y = mTargetView.getMeasuredHeight() / 2;
            r = (int) clipStartRadius;
        } else {
            int[] init = init(view);
            x = init[0];
            y = init[1];
            r = init[2];
        }
        initXYR(x, y, r);
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

    private void updateClipPath(float radius) {
        clipPath.reset();
        clipPath.addCircle(clipStartX, clipStartY, radius, Path.Direction.CW);
        mTargetView.postInvalidate();
    }

    private float calcEndRadius() {
        int viewWidth = mTargetView.getMeasuredWidth();
        int viewHeight = mTargetView.getMeasuredHeight();

        return calcEndRadius(viewWidth, viewHeight, clipStartX, clipStartY);
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
