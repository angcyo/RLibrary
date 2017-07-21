package com.angcyo.uiview.resources

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/21 14:42
 * 修改人员：Robi
 * 修改时间：2017/07/21 14:42
 * 修改备注：
 * Version: 1.0.0
 */
abstract class RAnimListener : AnimatorListenerAdapter() {
    override fun onAnimationRepeat(animation: Animator?) {
        super.onAnimationRepeat(animation)
    }

    override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
    }

    override fun onAnimationCancel(animation: Animator?) {
        super.onAnimationCancel(animation)
    }

    override fun onAnimationPause(animation: Animator?) {
        super.onAnimationPause(animation)
    }

    override fun onAnimationStart(animation: Animator?) {
        super.onAnimationStart(animation)
    }

    override fun onAnimationResume(animation: Animator?) {
        super.onAnimationResume(animation)
    }

    open fun onAnimationProgress(animation: Animator?, progress: Float /*0-1f*/) {

    }

    /**当设置了动画setStartDelay时, 此方法会先于onAnimationStart调用*/
    open fun onDelayBeforeStart(animation: Animator?) {

    }

    /**当动画结束时, 延迟一定时间回调此方法*/
    open fun onDelayAfterEnd(animation: Animator?) {

    }
}