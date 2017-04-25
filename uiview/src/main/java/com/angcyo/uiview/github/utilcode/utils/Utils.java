package com.angcyo.uiview.github.utilcode.utils;

import android.content.Context;

import com.angcyo.uiview.RApplication;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 16/12/08
 *     desc  : Utils初始化相关
 * </pre>
 */
public class Utils {

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        return RApplication.getApp();
    }
}