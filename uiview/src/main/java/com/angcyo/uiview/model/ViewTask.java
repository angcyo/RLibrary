package com.angcyo.uiview.model;

import com.angcyo.uiview.container.UIParam;
import com.angcyo.uiview.view.IView;

import java.util.UUID;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：启动IView, 关闭IView等 统一用Task管理.
 * 创建人员：Robi
 * 创建时间：2018/03/02 16:49
 * 修改人员：Robi
 * 修改时间：2018/03/02 16:49
 * 修改备注：
 * Version: 1.0.0
 */
public class ViewTask {
    public static final int TASK_TYPE_START = 0b1;//启动
    public static final int TASK_TYPE_FINISH = TASK_TYPE_START << 1;//关闭
    public static final int TASK_TYPE_SHOW = TASK_TYPE_FINISH << 1;//显示
    public static final int TASK_TYPE_HIDE = TASK_TYPE_SHOW << 1;//隐藏

    public int taskType = 0;//启动的任务类型

    public IView iView;//需要操作的IView
    public UIParam param;//参数

    public String uuid = "";
    public long createTime;

    public ViewTask(int taskType, IView iView, UIParam param) {
        this.taskType = taskType;
        this.iView = iView;
        this.param = param;
        uuid = UUID.randomUUID().toString();
        createTime = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object obj) {
        //return TextUtils.equals(uuid, ((ViewTask) obj).uuid);
        if (iView == null || ((ViewTask) obj).iView == null) {
            return false;
        }
        return iView == ((ViewTask) obj).iView;
    }
}
