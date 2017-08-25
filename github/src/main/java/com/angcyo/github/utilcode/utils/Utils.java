package com.angcyo.github.utilcode.utils;

import android.app.Application;
import android.content.Context;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 16/12/08
 *     desc  : Utils初始化相关
 * </pre>
 * https://github.com/Blankj/AndroidUtilCode
 */
public class Utils {

    static Application mApplication;

    public static void init(Application application) {
        mApplication = application;
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        return mApplication;
    }
}