package com.angcyo.uiview.model;

import com.angcyo.uiview.view.IView;

/**
 * Created by angcyo on 2018-03-03.
 */

public class AnimParam {
    /**
     * 请求原始动画
     */
    public boolean needBaseAnim = false;

    /**
     * 需要执行动画的目标IView
     */
    public IView targetIView;
}
