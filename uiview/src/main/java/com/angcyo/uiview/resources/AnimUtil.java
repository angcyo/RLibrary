package com.angcyo.uiview.resources;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import com.angcyo.uiview.utils.ScreenUtil;
import com.angcyo.uiview.view.UIIViewImpl;

/**
 * Created by angcyo on 2016-10-02 20:54.
 */
public class AnimUtil {

    /**
     * 计算2个颜色之间的颜色值
     */
    public static int evaluateColor(float fraction /*0-1*/, int startColor, int endColor) {
        int startInt = startColor;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = endColor;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (startA + (int) (fraction * (endA - startA))) << 24 |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));
    }

    /**
     * 默认颜色渐变动画
     */
    public static ValueAnimator startArgb(final View targetView, int startColor, int endColor) {
        return startArgb(targetView, startColor, endColor, 700);
    }

    public static ValueAnimator startArgb(final View targetView, int startColor, int endColor, long duration) {
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimator.setInterpolator(new LinearInterpolator());
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) animation.getAnimatedValue();//之后就可以得到动画的颜色了.
                targetView.setBackgroundColor(color);//设置一下, 就可以看到效果..
            }
        });
        colorAnimator.setDuration(duration);
        colorAnimator.start();
        return colorAnimator;
    }

    /**
     * 属性值动画
     */
    public static ValueAnimator startValue(int from, int to, long duration, ValueAnimator.AnimatorUpdateListener listener) {
        final ValueAnimator valueAnimator = ObjectAnimator.ofInt(from, to);
        valueAnimator.setDuration(duration).addUpdateListener(listener);
        valueAnimator.start();
        return valueAnimator;
    }

    /**
     * 应用一个布局动画
     */
    public static void applyLayoutAnimation(final ViewGroup viewGroup) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1f,
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f);
        translateAnimation.setDuration(300);
        applyLayoutAnimation(viewGroup, translateAnimation);
    }

    /**
     * 应用一个布局动画
     */
    public static void applyLayoutAnimation(final ViewGroup viewGroup, final Animation animation) {
        if (animation == null) {
            viewGroup.setLayoutAnimation(null);
            return;
        }
        final LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animation);
        viewGroup.setLayoutAnimation(layoutAnimationController);
    }

    public static Rect ensureRect(Rect rect) {
        if (rect == null || rect.isEmpty()) {
            int dp = (int) (100 * ScreenUtil.density);
            return new Rect(ScreenUtil.screenWidth / 2 - dp / 2, ScreenUtil.screenHeight / 2 - dp / 2,
                    ScreenUtil.screenWidth / 2 + dp / 2, ScreenUtil.screenHeight / 2 + dp / 2);
        }
        return rect;
    }

    /**
     * 视图从一个中心点坐标, 平移放大到结束点坐标
     */
    public static ValueAnimator startToMaxAnim(Rect startRect, final View targetView,
                                               Point from, Point to,
                                               int maxWidth, int maxHeight, final long startDelay,
                                               Animator.AnimatorListener listener) {
        startRect = ensureRect(startRect);

        final Point startPoint = from;
        final Point endPoint = to;

        final int targetWidth = Math.min(ScreenUtil.screenWidth, maxWidth);
        final int targetHeight = Math.min(ScreenUtil.screenHeight, maxHeight);

        final float startScaleX = startRect.width() * 1f / targetWidth;
        final float startScaleY = startRect.height() * 1f / targetHeight;

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addListener(listener);

        final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                updateMaxValue(targetView, startScaleX, startScaleY, startPoint, endPoint, value);

                notifyAnimProgress(animation, animation.getAnimatedFraction());
            }
        };

        animator.addUpdateListener(updateListener);

        animator.setDuration(UIIViewImpl.DEFAULT_FINISH_ANIM_TIME);
        if (startDelay > 0) {
            animator.setStartDelay(startDelay);
        }
        animator.setInterpolator(new DecelerateInterpolator());

        if (targetView.getMeasuredWidth() == 0 || targetView.getMeasuredHeight() == 0) {
            targetView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (startDelay > 0) {
                        updateMaxValue(targetView, startScaleX, startScaleY, startPoint, endPoint, 0);
                    }
                    notifyAnimDelayStart(animator);
                    animator.start();
                    targetView.getViewTreeObserver().removeOnPreDrawListener(this);
                    return false;
                }
            });
        } else {
            if (startDelay > 0) {
                updateMaxValue(targetView, startScaleX, startScaleY, startPoint, endPoint, 0);
            }
            notifyAnimDelayStart(animator);
            animator.start();
        }

        return animator;
    }

    private static void updateMaxValue(final View targetView,
                                       final float startScaleX, final float startScaleY,
                                       final Point startPoint,
                                       final Point endPoint,
                                       float fraction) {
        ViewCompat.setScaleX(targetView, startScaleX + (1 - startScaleX) * fraction);
        ViewCompat.setScaleY(targetView, startScaleY + (1 - startScaleY) * fraction);

        ViewCompat.setX(targetView, (startPoint.x + (endPoint.x - startPoint.x) * fraction) - targetView.getMeasuredWidth() / 2);
        ViewCompat.setY(targetView, (startPoint.y + (endPoint.y - startPoint.y) * fraction) - targetView.getMeasuredHeight() / 2);
    }

    public static ValueAnimator startToMinAnim(Rect endRect, final View targetView,
                                               Point from, Point to,
                                               int maxWidth, int maxHeight,
                                               Animator.AnimatorListener listener) {
        endRect = ensureRect(endRect);

        final Point startPoint = from;
        final Point endPoint = to;

        final int targetWidth = Math.min(ScreenUtil.screenWidth, maxWidth);
        final int targetHeight = Math.min(ScreenUtil.screenHeight, maxHeight);

        final float endScaleX = endRect.width() * 1f / targetWidth;
        final float endScaleY = endRect.height() * 1f / targetHeight;

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addListener(listener);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                ViewCompat.setScaleX(targetView, 1 - (1 - endScaleX) * value);
                ViewCompat.setScaleY(targetView, 1 - (1 - endScaleY) * value);

                ViewCompat.setX(targetView, (startPoint.x + (endPoint.x - startPoint.x) * value) - targetView.getMeasuredWidth() / 2);
                ViewCompat.setY(targetView, (startPoint.y + (endPoint.y - startPoint.y) * value) - targetView.getMeasuredHeight() / 2);

                notifyAnimProgress(animation, animation.getAnimatedFraction());
            }
        });

        animator.setDuration(UIIViewImpl.DEFAULT_FINISH_ANIM_TIME);
        animator.setInterpolator(new DecelerateInterpolator());

        if (targetView.getMeasuredWidth() == 0 || targetView.getMeasuredHeight() == 0) {
            targetView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    notifyAnimDelayStart(animator);
                    animator.start();
                    targetView.getViewTreeObserver().removeOnPreDrawListener(this);
                    return false;
                }
            });
        } else {
            notifyAnimDelayStart(animator);
            animator.start();
        }
        return animator;
    }

    private static void notifyAnimProgress(ValueAnimator animator, float progress) {
        if (animator.getListeners() == null) {
            return;
        }
        for (Animator.AnimatorListener listener : animator.getListeners()) {
            if (listener instanceof RAnimListener) {
                ((RAnimListener) listener).onAnimationProgress(animator, progress);
            }
        }
    }

    private static void notifyAnimDelayStart(ValueAnimator animator) {
        if (animator.getListeners() == null) {
            return;
        }
        for (Animator.AnimatorListener listener : animator.getListeners()) {
            if (listener instanceof RAnimListener) {
                ((RAnimListener) listener).onDelayBeforeStart(animator);
            }
        }
    }

    private static void notifyAnimDelayEnd(ValueAnimator animator) {
        if (animator.getListeners() == null) {
            return;
        }
        for (Animator.AnimatorListener listener : animator.getListeners()) {
            if (listener instanceof RAnimListener) {
                ((RAnimListener) listener).onDelayAfterEnd(animator);
            }
        }
    }
}
