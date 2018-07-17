package com.angcyo.fragment.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.angcyo.uiview.kotlin.ActivityExKt;
import com.angcyo.uiview.utils.ScreenUtil;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：Fragment 载体
 * 创建人员：Robi
 * 创建时间：2018/07/16 13:40
 * 修改人员：Robi
 * 修改时间：2018/07/16 13:40
 * 修改备注：
 * Version: 1.0.0
 */
public class UIFragmentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityExKt.enableLayoutFullScreen(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*当屏幕尺寸发生变化的时候, 重新读取屏幕宽高度*/
        ScreenUtil.init(getApplicationContext());
    }
}
