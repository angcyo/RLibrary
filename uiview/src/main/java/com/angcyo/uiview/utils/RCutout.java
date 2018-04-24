package com.angcyo.uiview.utils;

import android.os.Build;
import android.view.View;

import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.accessibility.permission.RomUtil;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：凹形屏工具类
 * 创建人员：Robi
 * 创建时间：2018/04/23 10:29
 * 修改人员：Robi
 * 修改时间：2018/04/23 10:29
 * 修改备注：
 * Version: 1.0.0
 */
public class RCutout {
    /**
     * 判断手机是否是凹形屏
     */
    public static boolean haveCutout(View view) {
        if (RomUtil.isOppo()) {
            return RApplication.getApp().getPackageManager()
                    .hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        }



        return Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1;
    }
}
