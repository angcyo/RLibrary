package com.lzy.imagepicker.iview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.angcyo.library.widget.DragPhotoView;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.container.ILayout;
import com.angcyo.uiview.container.UIParam;
import com.angcyo.uiview.model.AnimParam;
import com.angcyo.uiview.resources.AnimUtil;
import com.angcyo.uiview.resources.RAnimListener;
import com.angcyo.uiview.resources.ResUtil;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.utils.RUtils;
import com.angcyo.uiview.utils.ScreenUtil;
import com.angcyo.uiview.utils.UI;
import com.angcyo.uiview.view.UIIViewImpl;
import com.angcyo.uiview.viewgroup.RRelativeLayout;
import com.angcyo.uiview.widget.viewpager.TextIndicator;
import com.lzy.imagepicker.R;
import com.lzy.imagepicker.adapter.ImagePageAdapter;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.view.ViewPagerFixed;

import java.util.ArrayList;

import static com.angcyo.uiview.resources.AnimUtil.createClipEnterAnim;
import static com.angcyo.uiview.resources.AnimUtil.createClipExitAnim;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/01/04 15:22
 * 修改人员：Robi
 * 修改时间：2017/01/04 15:22
 * 修改备注：
 * Version: 1.0.0
 */
public class UIImagePagerIView extends UIIViewImpl {

    //动画开始 坐标x,y 和宽高
    protected int mStartX, mStartY, mStartW, mStartH;
    protected RRelativeLayout mMRootLayout;
    IndicatorStyle mIndicatorStyle = IndicatorStyle.CIRCLE;
    ImagePageAdapter.PhotoViewLongClickListener photoViewLongClickListener = null;
    private ArrayList<ImageItem> mImageItems;
    private ViewPagerFixed mMViewPager;
    private PinchCircleIndicator mMCircleIndicator;
    private boolean isToFinish;
    private int startPosition = 0;
    private ValueAnimator mValueAnimator;
    private int mLastTranColor = Color.BLACK;
    private IViewConfigCallback mIViewConfigCallback = new IViewConfigCallback() {
    };
    private ImagePageAdapter mImagePageAdapter;
    private TextIndicator mTextIndicator;

    protected UIImagePagerIView(ArrayList<ImageItem> imageItems, int startPosition) {
        mImageItems = imageItems;
        this.startPosition = Math.min(startPosition, imageItems.size() - 1);
    }

    public static void initStartParam(UIImagePagerIView uiView, final View view) {
        int[] rt = new int[2];
        view.getLocationOnScreen(rt);

        float w = view.getMeasuredWidth();
        float h = view.getMeasuredHeight();

        float x = rt[0] + w / 2;
        float y = rt[1] + h / 2;

        uiView.mStartX = rt[0];
        uiView.mStartY = rt[1];
        uiView.mStartW = (int) w;
        uiView.mStartH = (int) h;
    }


    public static UIImagePagerIView start(ILayout iLayout, final View view, ArrayList<ImageItem> imageItems, int startPosition) {
        final UIImagePagerIView imagePagerUIView = new UIImagePagerIView(imageItems, startPosition);
        if (iLayout == null) {
            return imagePagerUIView;
        }
        initStartParam(imagePagerUIView, view);
        iLayout.startIView(imagePagerUIView, new UIParam(true));

        return imagePagerUIView;
    }

    public UIImagePagerIView setIViewConfigCallback(IViewConfigCallback IViewConfigCallback) {
        mIViewConfigCallback = IViewConfigCallback;
        mIViewConfigCallback.mImagePagerUIView = this;
        return this;
    }

    public UIImagePagerIView setIndicatorStyle(IndicatorStyle indicatorStyle) {
        mIndicatorStyle = indicatorStyle;
        return this;
    }

    @Override
    public void onViewCreate(View rootView) {
        super.onViewCreate(rootView);
        mImagePageAdapter = new ImagePageAdapter(mActivity, mImageItems);
        mMViewPager.setAdapter(mImagePageAdapter);
        mImagePageAdapter.setOnExitListener(new DragPhotoView.OnExitListener() {
            @Override
            public void onMoveTo(DragPhotoView view, float w, float h, float translateX, float translateY) {
                showLastViewPattern();
                onMoveExitCancelTo(view, w, h, translateX, translateY);
            }

            @Override
            public void onExit(DragPhotoView view, float translateX, float translateY, float w, float h) {
                //animToFinish();
                animToMin(true);
            }

            @Override
            public void onMoveExitCancel(DragPhotoView view) {
                if (!isToFinish) {
                    hideLastViewPattern();
                }
            }

            @Override
            public void onMoveExitCancelTo(DragPhotoView view, float w, float h, float translateX, float translateY) {
                int alpha = 255 - (int) (255 * (translateY * 2 / h));
                alpha = Math.max(0, alpha);
                alpha = Math.min(255, alpha);
                mLastTranColor = SkinHelper.getTranColor(Color.BLACK, alpha);
                mMRootLayout.setBackgroundColor(mLastTranColor);
            }
        });
        mImagePageAdapter.setPhotoViewClickListener(new ImagePageAdapter.PhotoViewClickListener() {
            @Override
            public void OnPhotoTapListener(View view, float v, float v1) {
                //animToFinish();
                animToMin(false);
            }
        });
        setPhotoViewLongClickListener(photoViewLongClickListener);

        mMCircleIndicator.setViewPager(mMViewPager);
        mTextIndicator.setupViewPager(mMViewPager);
        mMViewPager.setCurrentItem(startPosition);

        hideIndicator();
    }

    @Override
    public boolean onBackPressed() {
        //animToFinish();
        if (RApplication.isLowDevice) {
            return true;
        }
        animToMin(false);
        finishIView(this, new UIParam(true, true, false));
        return false;
    }

    @Override
    public void onViewLoad() {
        super.onViewLoad();
        //startAnimation();
        if (!RApplication.isLowDevice) {
            animToMax();
        } else {
            mMRootLayout.setBackgroundColor(Color.BLACK);
            showIndicator();
        }
        //startAnimation2();
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    @Override
    public void onViewUnload() {
        super.onViewUnload();
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
    }

    @Override
    protected View inflateBaseView(FrameLayout container, LayoutInflater inflater) {
        mMRootLayout = new RRelativeLayout(mActivity);
        mMViewPager = new ViewPagerFixed(mActivity);
        mMCircleIndicator = new PinchCircleIndicator(mActivity);
        mTextIndicator = new TextIndicator(mActivity);

        RelativeLayout.LayoutParams indicatorParams = new RelativeLayout.LayoutParams(-2, -2);
        indicatorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        indicatorParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        indicatorParams.setMargins(0, 0, 0, (int) ResUtil.dpToPx(mActivity.getResources(), 50));

        RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(-2, -2);
        ivParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ivParams.setMargins(0, (int) ResUtil.dpToPx(mActivity.getResources(), 50), (int) ResUtil.dpToPx(mActivity.getResources(), 50), 0);

        mMRootLayout.addView(mMViewPager, new ViewGroup.LayoutParams(-1, -1));
        mMRootLayout.addView(mMCircleIndicator, indicatorParams);//点的索引指示器, 在底部居中

        indicatorParams = new RelativeLayout.LayoutParams(-2, -2);
        indicatorParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        indicatorParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        int marginTop = getDimensionPixelOffset(R.dimen.base_xhdpi);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            marginTop += getDimensionPixelOffset(R.dimen.status_bar_height);
        }
        indicatorParams.setMargins(0, marginTop, 0, 0);
        mTextIndicator.setTextColor(Color.WHITE);
        mTextIndicator.setAutoHide(false);
        mMRootLayout.addView(mTextIndicator, indicatorParams);//文本的索引指示器, 在顶部居中

        mMRootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animToFinish();
                animToMin(false);
            }
        });
        container.addView(mMRootLayout, new ViewGroup.LayoutParams(-1, -1));

        mIViewConfigCallback.onInflateBaseView(mMRootLayout, inflater);
        return container;
    }

    private void animToFinish() {
        if (isToFinish) {
            return;
        }
        showLastViewPattern();

        isToFinish = true;
        hideIndicator();
        mMCircleIndicator.setAlpha(0);
        AnimUtil.startArgb(mMRootLayout, mLastTranColor, Color.TRANSPARENT, UIIViewImpl.DEFAULT_ANIM_TIME);
        ViewCompat.animate(mMViewPager).alpha(0).scaleX(0.2f).scaleY(0.2f)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(UIIViewImpl.DEFAULT_ANIM_TIME)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        isToFinish = false;
                        finishIView(UIImagePagerIView.this, false);
                    }
                }).start();
    }

    private void showLastViewPattern() {
        try {
            getILayout().getViewPatternAtLast(1).mView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
        }
    }

    private void hideLastViewPattern() {
//        try {
//            getILayout().getViewPatternAtLast(1).mView.setVisibility(View.GONE);
//        } catch (Exception e) {
//        }
    }

    @Deprecated
    private void startAnimation() {
        hideIndicator();
        mValueAnimator = AnimUtil.startArgb(mMRootLayout, Color.TRANSPARENT, Color.BLACK, UIIViewImpl.DEFAULT_ANIM_TIME);
        final int screenWidth = ResUtil.getScreenWidth(mActivity);
        final int screenHeight = ResUtil.getScreenHeight(mActivity);
        mMViewPager.setX(mStartX + mStartW / 2 - screenWidth / 2);
        mMViewPager.setY(mStartY + mStartH / 2 - screenHeight / 2);
        mMViewPager.setScaleX((mStartW + 0f) / screenWidth);
        mMViewPager.setScaleY((mStartH + 0f) / screenHeight);
        final ViewPropertyAnimator viewPropertyAnimator = mMViewPager.animate().x(0).y(0)
                .scaleX(1).scaleY(1)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(UIIViewImpl.DEFAULT_ANIM_TIME);
        mMViewPager.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mImageItems.size() > 1) {
                    showIndicator();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mMViewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPropertyAnimator.start();
            }
        });
    }

    private void startAnimation2() {
        mMCircleIndicator.setVisibility(View.GONE);
        mValueAnimator = AnimUtil.startArgb(mMRootLayout, Color.TRANSPARENT, Color.BLACK, UIIViewImpl.DEFAULT_ANIM_TIME);
        final int screenWidth = ResUtil.getScreenWidth(mActivity);
        final int screenHeight = ResUtil.getScreenHeight(mActivity);

        ViewCompat.setX(mMViewPager, mStartX);
        ViewCompat.setY(mMViewPager, mStartY);

        UI.setView(mMViewPager, mStartW, mStartH);

        ViewCompat.animate(mMViewPager)
                .setDuration(1000)
                .x(screenWidth / 2 - mStartW / 2)
                .y(screenHeight / 2 - mStartH / 2)
                .scaleX(screenWidth * 1.f / mStartW)
                .scaleY(screenHeight * 1.f / mStartH)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (mImageItems.size() > 1) {
                            mMCircleIndicator.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .start();
    }

    @Override
    public Animation loadStartAnimation(AnimParam animParam) {
        return createClipEnterAnim(1f);
    }

    @Override
    public Animation loadFinishAnimation(AnimParam animParam) {
        return createClipExitAnim(1f);
    }

    @Override
    public Animation loadOtherEnterAnimation(AnimParam animParam) {
        return createClipEnterAnim(1f);
    }

    @Override
    public Animation loadOtherExitAnimation(AnimParam animParam) {
        return createClipExitAnim(1f);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mIViewConfigCallback.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onViewShow(Bundle bundle) {
        super.onViewShow(bundle);
        mIViewConfigCallback.onViewShow(bundle);
    }

    public ImagePageAdapter getImagePageAdapter() {
        return mImagePageAdapter;
    }

    private void showIndicator() {
        hideIndicator();
        if (mImageItems.size() <= 0) {
            return;
        } else if (mImageItems.size() > 10) {
            mIndicatorStyle = IndicatorStyle.TEXT;
        }
        switch (mIndicatorStyle) {
            case CIRCLE:
                mMCircleIndicator.setVisibility(View.VISIBLE);
                break;
            case TEXT:
                mTextIndicator.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void hideIndicator() {
        mMCircleIndicator.setVisibility(View.INVISIBLE);
        mTextIndicator.setVisibility(View.INVISIBLE);
    }

    public void setPhotoViewLongClickListener(ImagePageAdapter.PhotoViewLongClickListener listener) {
        photoViewLongClickListener = listener;
        if (mImagePageAdapter != null) {
            mImagePageAdapter.setPhotoViewLongClickListener(photoViewLongClickListener);
        }
    }

    /**
     * 退出动画
     */
    private void animToMin(boolean isFromMove /*是否是下拉返回, 关闭界面*/) {
        if (isToFinish) {
            return;
        }
        isToFinish = true;
        hideIndicator();
        mMCircleIndicator.setAlpha(0);

        Rect viewLocation = mImageItems.get(mMViewPager.getCurrentItem()).mViewLocation;

        //最终要移动到的位置, 如果为空采用默认动画 2018-3-1
        if (isFromMove) {
            viewLocation = AnimUtil.ensureRect(viewLocation);
        }

        View currentImageView = mImagePageAdapter.getCurrentImageView();
        View targetView = null;//mImagePageAdapter.getCurrentImageView();//mMViewPager;
        if (targetView == null) {
            targetView = mMViewPager;
        }

        int animWidth = targetView.getMeasuredWidth();
        int animHeight = targetView.getMeasuredHeight();

//        if (currentImageView instanceof DragPhotoView) {
//            int imageHeight = ((DragPhotoView) currentImageView).getCurrentImageHeight();
//            int imageWidth = ((DragPhotoView) currentImageView).getCurrentImageWidth();
//
//            if (imageWidth < animWidth) {
//                viewLocation.inset((animWidth - imageWidth) / 2, 0);
//            }
//
//            if (imageHeight < animHeight) {
//                viewLocation.inset(0, (animHeight - imageHeight) / 2);
//            }
//        }
//        AnimUtil.startToMinAnim(viewLocation,
//                targetView, new Point(ScreenUtil.screenWidth / 2, ScreenUtil.screenHeight / 2),
//                new Point(viewLocation.centerX(), viewLocation.centerY()),
//                animWidth, animHeight,
//                new RAnimListener() {
//                    @Override
//                    public void onAnimationProgress(Animator animation, float progress) {
//                        super.onAnimationProgress(animation, progress);
//                        mMRootLayout.setBackgroundColor(AnimUtil.evaluateColor(progress, mLastTranColor, Color.TRANSPARENT));
////                        mMViewPager.setAlpha(0.6f + 1 - progress);
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        super.onAnimationEnd(animation);
//                        isToFinish = false;
//                        finishIView(ImagePagerUIView.this, false);
//                    }
//                });

        if (currentImageView instanceof DragPhotoView) {
            int imageHeight = ((DragPhotoView) currentImageView).getCurrentImageHeight();
            int imageWidth = ((DragPhotoView) currentImageView).getCurrentImageWidth();

            animWidth = Math.min(animWidth, imageWidth);
            animHeight = Math.min(animHeight, imageHeight);
        }

        float endScaleX = viewLocation.width() * 1f / animWidth;
        float endScaleY = viewLocation.height() * 1f / animHeight;

        if (RUtils.isRectEmpty(viewLocation) || endScaleY > 0.7) {
            AnimUtil.scaleAlphaToMax(targetView, new Runnable() {
                @Override
                public void run() {
                    finishIView(UIImagePagerIView.this, false);
                }
            });
            AnimUtil.startArgb(mMRootLayout, mLastTranColor, Color.TRANSPARENT, DEFAULT_FINISH_ANIM_TIME);
        } else {
            AnimUtil.startToMinAnim(
                    targetView, new Point(ScreenUtil.screenWidth / 2, ScreenUtil.screenHeight / 2),
                    new Point(viewLocation.centerX(), viewLocation.centerY()),
                    endScaleX, endScaleY,
                    new RAnimListener() {
                        @Override
                        public void onAnimationProgress(Animator animation, float progress) {
                            super.onAnimationProgress(animation, progress);
                            mMRootLayout.setBackgroundColor(AnimUtil.evaluateColor(progress, mLastTranColor, Color.TRANSPARENT));
                            mMViewPager.setAlpha(0.6f + 1 - progress);
                        }

                        @Override
                        public void onAnimationFinish(Animator animation, boolean cancel) {
                            super.onAnimationFinish(animation, cancel);
                            isToFinish = false;
                            finishIView(UIImagePagerIView.this, false);
                        }
                    });
        }
    }

    /**
     * 进入动画
     */
    private void animToMax() {
        hideIndicator();
        Rect viewLocation = mImageItems.get(startPosition).mViewLocation;
        viewLocation = AnimUtil.ensureRect(viewLocation);

        if (mMViewPager.getMeasuredWidth() == 0 || mMViewPager.getMeasuredHeight() == 0) {
            final Rect finalViewLocation = viewLocation;
            mMViewPager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mMViewPager.getViewTreeObserver().removeOnPreDrawListener(this);
                    startAnimInner(finalViewLocation);
                    return false;
                }
            });
        } else {
            startAnimInner(viewLocation);
        }
    }

    private void startAnimInner(Rect viewLocation) {
//        mMViewPager.setBackgroundColor(Color.RED);

        AnimUtil.startToMaxAnim(viewLocation,
                mMViewPager,
                new Point(viewLocation.centerX(), viewLocation.centerY()),
                new Point(ScreenUtil.screenWidth / 2, ScreenUtil.screenHeight / 2),
                mMViewPager.getMeasuredWidth(),
                mMViewPager.getMeasuredHeight(), 60, new RAnimListener() {
                    @Override
                    public void onAnimationProgress(Animator animation, float progress) {
                        super.onAnimationProgress(animation, progress);
                        mMRootLayout.setBackgroundColor(AnimUtil.evaluateColor(progress, Color.TRANSPARENT, Color.BLACK));
                    }

                    @Override
                    public void onAnimationFinish(Animator animation, boolean cancel) {
                        super.onAnimationFinish(animation, cancel);
//                        mPreviewImageView.setVisibility(View.GONE);
//                        mMViewPager.setVisibility(View.VISIBLE);
                        showIndicator();
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                    }

                    @Override
                    public void onDelayBeforeStart(Animator animation) {
                        super.onDelayBeforeStart(animation);
//                        mPreviewImageView.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public boolean needTransitionExitAnim() {
        return true;
    }

    @Override
    public boolean needTransitionStartAnim() {
        return true;
    }

    /**
     * 指示器的样式
     */
    public enum IndicatorStyle {
        NONE, TEXT, CIRCLE
    }

    /**
     * 定制界面的Config
     */
    public static abstract class IViewConfigCallback {
        public UIImagePagerIView mImagePagerUIView;

        public void onInflateBaseView(final RelativeLayout containRootLayout, final LayoutInflater inflater) {

        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {

        }

        public void onViewShow(Bundle bundle) {

        }
    }

}
