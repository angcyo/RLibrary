package com.lzy.imagepicker;

import android.text.TextUtils;
import android.widget.ImageView;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：图片涉黄控制
 * 创建人员：Robi
 * 创建时间：2017/05/24 15:27
 * 修改人员：Robi
 * 修改时间：2017/05/24 15:27
 * 修改备注：
 * Version: 1.0.0
 */
public class YImageControl {

    /**
     * 判断图片是否涉黄
     */
    public static boolean isYellowImage(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (url.contains("|porn")) {
            return true;
        }
        return false;
    }

    /**
     * 返回正确的url
     */
    public static String url(String url) {
        if (!TextUtils.isEmpty(url)) {
            return url.replaceAll("\\|porn", "");
        }
        return url;
    }

    public static void showYellowImageXiao(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.drawable.jinhuang_xiao);
    }
}
