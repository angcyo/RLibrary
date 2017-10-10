package com.angcyo.uiview.utils;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：cjh
 * 创建时间：2017/10/10
 * 修改人员：cjh
 * 修改时间：2017/10/10
 * 修改备注：
 * Version: 1.0.0
 */
public abstract class SafeRunnable implements Runnable {
    @Override
    final public void run() {
        try {
            onRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void onRun();
}
