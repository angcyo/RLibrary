package com.angcyo.uiview.view;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/03/12 13:42
 * 修改人员：Robi
 * 修改时间：2018/03/12 13:42
 * 修改备注：
 * Version: 1.0.0
 */
public enum IViewAnimationType {
    NONE /*无动画*/,
    ALPHA /*透明渐变动画*/,
    TRANSLATE_HORIZONTAL /*左右平移动画*/,
    TRANSLATE_VERTICAL /*垂直平移启动退出动画, 其他透明动画*/,
    SCALE_TO_MAX /*从0.8放大, 其他从1到0.5缩小*/,
    SCALE_TO_MAX_AND_END /*与SCALE_TO_MAX不同的是, 退出时, 也采用Scale动画*/
}
