package com.angcyo.fragment.ui;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;

import com.angcyo.uiview.resources.AnimUtil;
import com.angcyo.uiview.view.IViewAnimationType;

import static com.angcyo.uiview.view.IViewAnimationType.SCALE_TO_MAX_AND_END_OVERSHOOT;
import static com.angcyo.uiview.view.IViewAnimationType.SCALE_TO_MAX_AND_TO_MAX_END_OVERSHOOT;
import static com.angcyo.uiview.view.IViewAnimationType.SCALE_TO_MAX_OVERSHOOT;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/07/16 17:51
 * 修改人员：Robi
 * 修改时间：2018/07/16 17:51
 * 修改备注：
 * Version: 1.0.0
 */
public class RFragment extends Fragment implements IFragment {

    protected boolean isSwipeBack = false;

    /*激活动画, 请不要使用setCustomAnimations */
    protected boolean enableAnim = true;
    protected boolean isHiddened = false;
    protected Activity mActivity;

    protected IViewAnimationType mAnimationType = IViewAnimationType.TRANSLATE_HORIZONTAL;

    private boolean mIsRightJumpLeft = false;

    /*保持生命周期的顺序调用*/

    public static void setDefaultConfig(Animation animation, boolean isFinish) {
        if (isFinish) {
            animation.setDuration(300);
            animation.setInterpolator(new AccelerateInterpolator());
            animation.setFillAfter(false);
        } else {
            animation.setDuration(300);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setFillAfter(false);
        }
        animation.setFillBefore(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mAnimationType = IViewAnimationType.valueOf(savedInstanceState.getString("mAnimationType", IViewAnimationType.TRANSLATE_HORIZONTAL.name()));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getFragmentLayoutId(), container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        if (isSwipeBack || !enableAnim) {
            return null;
        }
        /* transit = 0
         * enter  = true
         * nextAnim = 0
         */
        return super.onCreateAnimator(transit, enter, nextAnim);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {

        /* transit = 0
         * enter  = false
         * nextAnim = 0
         */

        if (isSwipeBack || !enableAnim) {
            isHiddened = false;
            return null;
        }

        if (nextAnim == 0) {
            //默认动画
            if (enter) {
                //进入动画, 隐藏后进去 或者 创建后进入
                if (isHiddened) {
                    //从隐藏状态显示
                    isHiddened = false;
                    return loadOtherEnterAnimation();
                } else {
                    return loadStartAnimation();
                }

                //return AnimUtil.translateStartAnimation();
            } else {
                //隐藏 或者 销毁
                if (isRemoving()) {
                    //自己销毁的动画
                    return loadFinishAnimation();
                } else {
                    //其他Fragment Hide
                    return loadOtherExitAnimation();
                }
                //return AnimUtil.translateFinishAnimation();
            }
        } else if (nextAnim > 0) {
            return AnimationUtils.loadAnimation(mActivity, nextAnim);
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isSwipeBack = false;
        if (hidden) {
            isHiddened = true;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mAnimationType", mAnimationType.name());
    }

    public Animation loadStartAnimation() {
        switch (mAnimationType) {
            case NONE:
                return null;
            case ALPHA:
                return AnimUtil.createAlphaEnterAnim(0.8f);
            case TRANSLATE_VERTICAL:
                return AnimUtil.translateStartAnimation();
            case SCALE_TO_MAX:
            case SCALE_TO_MAX_AND_END:
            case SCALE_TO_MAX_OVERSHOOT:
            case SCALE_TO_MAX_AND_END_OVERSHOOT:
            case SCALE_TO_MAX_AND_TO_MAX_END_OVERSHOOT:
                Animation animation = AnimUtil.scaleMaxAlphaStartAnimation(0.7f);
                if (mAnimationType == SCALE_TO_MAX_OVERSHOOT ||
                        mAnimationType == SCALE_TO_MAX_AND_END_OVERSHOOT ||
                        mAnimationType == SCALE_TO_MAX_AND_TO_MAX_END_OVERSHOOT) {
                    animation.setInterpolator(new OvershootInterpolator());
                }
                return animation;
            case TRANSLATE_HORIZONTAL:
            default:
                return defaultLoadStartAnimation();
        }
    }

    public Animation loadFinishAnimation() {
        switch (mAnimationType) {
            case NONE:
                return null;
            case ALPHA:
                return AnimUtil.createAlphaExitAnim(0.2f);
            case TRANSLATE_VERTICAL:
                return AnimUtil.translateFinishAnimation();
            case SCALE_TO_MAX_AND_END:
            case SCALE_TO_MAX_AND_END_OVERSHOOT:
                Animation animation = AnimUtil.scaleMaxAlphaFinishAnimation(0.7f);
                if (mAnimationType == SCALE_TO_MAX_AND_END_OVERSHOOT) {
                    animation.setInterpolator(new OvershootInterpolator());
                }
                return animation;
            case SCALE_TO_MAX_AND_TO_MAX_END_OVERSHOOT:
                Animation animation2 = AnimUtil.scaleMaxAlphaFinishAnimation(1.2f);
                animation2.setInterpolator(new AnticipateInterpolator());
                return animation2;
            case SCALE_TO_MAX:
            case SCALE_TO_MAX_OVERSHOOT:
            case TRANSLATE_HORIZONTAL:
            default:
                return defaultLoadFinishAnimation();
        }
    }

    public Animation loadOtherEnterAnimation() {
        switch (mAnimationType) {
            case NONE:
                return null;
            case ALPHA:
                return AnimUtil.createAlphaEnterAnim(0.8f);
            case TRANSLATE_VERTICAL:
                return AnimUtil.createAlphaEnterAnim(0.8f);
            case SCALE_TO_MAX_AND_END:
            case SCALE_TO_MAX_AND_END_OVERSHOOT:
            case SCALE_TO_MAX_AND_TO_MAX_END_OVERSHOOT:
                return AnimUtil.createOtherEnterNoAnim();
            case SCALE_TO_MAX:
            case SCALE_TO_MAX_OVERSHOOT:
            case TRANSLATE_HORIZONTAL:
            default:
                return defaultLoadOtherEnterAnimation();
        }
    }

    public Animation loadOtherExitAnimation() {
        switch (mAnimationType) {
            case NONE:
                return null;
            case ALPHA:
                return AnimUtil.createAlphaExitAnim(0.8f);
            case TRANSLATE_VERTICAL:
                return AnimUtil.createAlphaExitAnim(0.8f);
            case SCALE_TO_MAX_AND_END:
            case SCALE_TO_MAX_AND_END_OVERSHOOT:
            case SCALE_TO_MAX_AND_TO_MAX_END_OVERSHOOT:
            case SCALE_TO_MAX:
            case SCALE_TO_MAX_OVERSHOOT:
                return AnimUtil.createOtherExitNoAnim();
            case TRANSLATE_HORIZONTAL:
            default:
                return defaultLoadOtherExitAnimation();
        }
    }

    public Animation defaultLoadOtherExitAnimation() {
        TranslateAnimation translateAnimation;
        if (mIsRightJumpLeft) {
            translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        } else {
            translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        }
        setDefaultConfig(translateAnimation, true);
        return translateAnimation;
    }

    public Animation defaultLoadOtherEnterAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        setDefaultConfig(translateAnimation, false);
        return translateAnimation;
    }

    protected Animation defaultLoadStartAnimation() {
        TranslateAnimation translateAnimation;
        if (mIsRightJumpLeft) {
            translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -0.99f, Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        } else {
            translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.99f, Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        }
        setDefaultConfig(translateAnimation, false);
        return translateAnimation;
    }

    protected Animation defaultLoadFinishAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        setDefaultConfig(translateAnimation, true);
        return translateAnimation;
    }

    /*------------------------自定义 可重载的方法---------------------------*/

    protected int getFragmentLayoutId() {
        return -1;
    }

    public void setAnimationType(IViewAnimationType animationType) {
        mAnimationType = animationType;
    }

    @Override
    public void setSwipeBack(boolean swipeBack) {
        isSwipeBack = swipeBack;
        //L.e("call: setSwipeBack([swipeBack])-> " + swipeBack);
    }

    @Override
    public void setIsRightJumpLeft(boolean isRightJumpLeft) {
        mIsRightJumpLeft = isRightJumpLeft;
        //L.e("call: setIsRightJumpLeft([isRightJumpLeft])-> " + isRightJumpLeft);
    }
}
