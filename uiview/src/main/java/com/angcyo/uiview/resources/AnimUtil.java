package com.angcyo.uiview.resources;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.utils.ScreenUtil;
import com.angcyo.uiview.view.UIIViewImpl;
import com.angcyo.uiview.viewgroup.ClipLayout;

import static com.angcyo.uiview.view.UIIViewImpl.DEFAULT_DIALOG_FINISH_ANIM_TIME;
import static com.angcyo.uiview.view.UIIViewImpl.setDefaultConfig;

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
     * 放大并且慢慢透明的 退出动画
     */
    public static Animation scaleAlphaToMax(final View targetView, final Runnable onAnimationEnd) {
        ScaleAnimation animation = new ScaleAnimation(1f, 2f, 1f, 2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.8f, 0);
        setDefaultConfig(animation, true);
        setDefaultConfig(alphaAnimation, true);

        final AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(animation);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (targetView.getMeasuredWidth() == 0 || targetView.getMeasuredHeight() == 0) {
            targetView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    targetView.startAnimation(animationSet);
                    targetView.getViewTreeObserver().removeOnPreDrawListener(this);
                    return false;
                }
            });
        } else {
            targetView.startAnimation(animationSet);
        }
        return animationSet;
    }

    /**
     * 视图从一个中心点坐标, 平移放大到结束点坐标
     */
    public static ValueAnimator startToMaxAnim(Rect startRect, final View targetView,
                                               Point from, Point to,
                                               int maxWidth, int maxHeight, final long startDelay,
                                               Animator.AnimatorListener listener) {
        startRect = ensureRect(startRect);

        int statusBarHeight = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            statusBarHeight = RApplication.getApp().getResources().getDimensionPixelOffset(R.dimen.status_bar_height);
        }

        final Point startPoint = new Point(from.x, from.y - statusBarHeight);
        final Point endPoint = new Point(to.x, to.y - statusBarHeight);

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

    public static ValueAnimator startToMaxAnim(Rect startRect, final View targetView,
                                               Point from, Point to,
                                               final ClipLayout clipLayout,
                                               final long startDelay,
                                               Animator.AnimatorListener listener) {
        final Rect finalStartRect = ensureRect(startRect);

        int statusBarHeight = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            statusBarHeight = RApplication.getApp().getResources().getDimensionPixelOffset(R.dimen.status_bar_height);
        }

        final Point startPoint = new Point(from.x, from.y - statusBarHeight);
        final Point endPoint = new Point(to.x, to.y - statusBarHeight);

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addListener(listener);

        final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float[] toMaxWH = clipLayout.getToMaxWH(finalStartRect.width(), finalStartRect.height(), value);

                clipLayout.clipRectTo(toMaxWH[0], toMaxWH[1]);

                updateMaxValue(targetView, startPoint, endPoint, value);

                notifyAnimProgress(animation, animation.getAnimatedFraction());
            }
        };

        animator.addUpdateListener(updateListener);

        animator.setDuration(UIIViewImpl.DEFAULT_FINISH_ANIM_TIME);
        if (startDelay > 0) {
            animator.setStartDelay(startDelay);
        }
        animator.setInterpolator(new DecelerateInterpolator());

        final Runnable defaultRunnable = new Runnable() {
            @Override
            public void run() {
                float[] toMaxWH = clipLayout.getToMaxWH(finalStartRect.width(), finalStartRect.height(), 0);
                clipLayout.clipRectTo(toMaxWH[0], toMaxWH[1]);
                updateMaxValue(targetView, startPoint, endPoint, 0);
            }
        };

        if (targetView.getMeasuredWidth() == 0 || targetView.getMeasuredHeight() == 0) {
            targetView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (startDelay > 0) {
                        defaultRunnable.run();
                    }
                    notifyAnimDelayStart(animator);
                    animator.start();
                    targetView.getViewTreeObserver().removeOnPreDrawListener(this);
                    return false;
                }
            });
        } else {
            if (startDelay > 0) {
                //默认状态
                defaultRunnable.run();
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

    private static void updateMaxValue(final View targetView,
                                       final Point startPoint,
                                       final Point endPoint,
                                       float fraction) {
        ViewCompat.setX(targetView, (startPoint.x + (endPoint.x - startPoint.x) * fraction) - targetView.getMeasuredWidth() / 2);
        ViewCompat.setY(targetView, (startPoint.y + (endPoint.y - startPoint.y) * fraction) - targetView.getMeasuredHeight() / 2);
    }

    public static ValueAnimator startToMinAnim(Rect endRect, final View targetView,
                                               Point from, Point to,
                                               int maxWidth, int maxHeight,
                                               Animator.AnimatorListener listener) {
        endRect = ensureRect(endRect);

        int statusBarHeight = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            statusBarHeight = RApplication.getApp().getResources().getDimensionPixelOffset(R.dimen.status_bar_height);
        }

        final Point startPoint = new Point(from.x, from.y - statusBarHeight);
        final Point endPoint = new Point(to.x, to.y - statusBarHeight);

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

    public static ValueAnimator startToMinAnim(Rect endRect, final View targetView,
                                               Point from, Point to,
                                               final ClipLayout clipLayout,
                                               Animator.AnimatorListener listener) {
        final Rect finalEndRect = ensureRect(endRect);

        int statusBarHeight = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            statusBarHeight = RApplication.getApp().getResources().getDimensionPixelOffset(R.dimen.status_bar_height);
        }

        final Point startPoint = new Point(from.x, from.y - statusBarHeight);
        final Point endPoint = new Point(to.x, to.y - statusBarHeight);

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addListener(listener);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                float[] fromMaxWH = clipLayout.getFromMaxWH(finalEndRect.width(), finalEndRect.height(), value);
                clipLayout.clipRectTo(fromMaxWH[0], fromMaxWH[1]);

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

    public static ValueAnimator startToMinAnim(final View targetView,
                                               Point from, Point to,
                                               final float endScaleX, final float endScaleY,
                                               Animator.AnimatorListener listener) {
        int statusBarHeight = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            statusBarHeight = RApplication.getApp().getResources().getDimensionPixelOffset(R.dimen.status_bar_height);
        }

        final Point startPoint = new Point(from.x, from.y - statusBarHeight);
        final Point endPoint = new Point(to.x, to.y - statusBarHeight);

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

    /**
     * 抖动 放大缩小
     */
    public static void scaleBounceView(View view) {
        ViewCompat.setScaleX(view, 0.5f);
        ViewCompat.setScaleY(view, 0.5f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(new BounceInterpolator())
                .setDuration(300)
                .start();
    }

    /**
     * 从0.5回弹放大效果
     * 可以指定放大的倍数
     */
    public static void scaleBounceView(View view, float x, float y) {
        ViewCompat.setScaleX(view, 0.5f);
        ViewCompat.setScaleY(view, 0.5f);
        view.animate()
                .scaleX(x)
                .scaleY(y)
                .setInterpolator(new BounceInterpolator())
                .setDuration(300)
                .start();
    }

    /**
     * 从0.5放大...
     */
    public static void scaleOvershootView(View view) {
        ViewCompat.setScaleX(view, 0.5f);
        ViewCompat.setScaleY(view, 0.5f);
        view.animate()
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new OvershootInterpolator(2))
                .setDuration(300)
                .start();
    }

    public static Animation scaleOvershootStartAnimation() {
        ScaleAnimation animation = new ScaleAnimation(0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        setDefaultConfig(animation, false);
        setDefaultConfig(alphaAnimation, false);

        animation.setInterpolator(new OvershootInterpolator(2));//先变化到最大值, 还要大, 然后回到最大值.

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(animation);
        return animationSet;
    }


    public static Animation scaleOvershootExitAnimation() {
        ScaleAnimation animation = new ScaleAnimation(1f, 0f, 1f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        setDefaultConfig(animation, false);
        setDefaultConfig(alphaAnimation, false);

        animation.setInterpolator(new AnticipateInterpolator(2));//先变大, 后变小

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(animation);
        return animationSet;
    }

    /**
     * 启动时的动画  从底部平移到顶部
     */
    public static Animation translateAlphaStartAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        setDefaultConfig(translateAnimation, false);
        setDefaultConfig(alphaAnimation, false);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        return animationSet;
    }

    /**
     * 结束时的动画 平移到底部
     */
    public static Animation translateAlphaFinishAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        setDefaultConfig(translateAnimation, true);
        setDefaultConfig(alphaAnimation, true);

        translateAnimation.setDuration(DEFAULT_DIALOG_FINISH_ANIM_TIME);
        alphaAnimation.setDuration(DEFAULT_DIALOG_FINISH_ANIM_TIME);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        return animationSet;
    }

    /**
     * 启动时的动画  从底部平移到顶部
     */
    public static Animation translateStartAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f);
        setDefaultConfig(translateAnimation, false);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(translateAnimation);
        return animationSet;
    }

    /**
     * 结束时的动画 平移到底部
     */
    public static Animation translateFinishAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
        setDefaultConfig(translateAnimation, true);

        translateAnimation.setDuration(DEFAULT_DIALOG_FINISH_ANIM_TIME);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(translateAnimation);
        return animationSet;
    }

    public static Animation createClipEnterAnim(float fromAlpha) {
        AlphaAnimation animation = new AlphaAnimation(fromAlpha, 1f);
        setDefaultConfig(animation, false);
        return animation;
    }

    public static Animation createClipExitAnim(float toAlpha) {
        AlphaAnimation animation = new AlphaAnimation(1f, toAlpha);
        setDefaultConfig(animation, true);
        return animation;
    }

    public static Animation createAlphaEnterAnim(float fromAlpha) {
        return createClipEnterAnim(fromAlpha);
    }

    public static Animation createAlphaExitAnim(float toAlpha) {
        return createClipExitAnim(toAlpha);
    }

    public static Animation createOtherEnterNoAnim() {
        AlphaAnimation animation = new AlphaAnimation(0.9f, 1f);
        setDefaultConfig(animation, false);
        return animation;
    }

    public static Animation createOtherExitNoAnim() {
        AlphaAnimation animation = new AlphaAnimation(1f, 0.9f);
        setDefaultConfig(animation, true);
        return animation;
    }

    /**
     * 围绕自身中心点, 360旋转, 的动画
     */
    public static Animation rotateAnimation() {
        return rotateAnimation(false);
    }

    public static Animation rotateAnimation(boolean circle /*循环动画*/) {
        RotateAnimation animation = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(300);

        if (circle) {
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.RESTART);
            animation.setDuration(2000);
        }
        return animation;
    }

    /**
     * 上下移动的循环动画
     */
    public static Animation translateYYAnimation(float toYValue) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, toYValue);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setDuration(300);
        return animation;
    }
}
