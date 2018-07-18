package com.angcyo.fragment.ui;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/07/18 08:37
 * 修改人员：Robi
 * 修改时间：2018/07/18 08:37
 * 修改备注：
 * Version: 1.0.0
 */
public interface IFragment {
    /*滑动返回时, 保存标志位, 不执行动画*/
    void setSwipeBack(boolean swipeBack);

    /*主界面Tab, 界面切换的方向*/
    void setIsRightJumpLeft(boolean isRightJumpLeft);
}
