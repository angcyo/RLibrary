package com.angcyo.uiview.utils;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：Glide 简易封装
 * 创建人员：Robi
 * 创建时间：2017/06/27 14:19
 * 修改人员：Robi
 * 修改时间：2017/06/27 14:19
 * 修改备注：
 * Version: 1.0.0
 */
public class G {
    public static void showImage(Activity activity, ImageView imageView, String image,
                                 Drawable placeholder, boolean anim) {
        DrawableTypeRequest<String> request = Glide.with(activity)
                .load(image);
        request.diskCacheStrategy(DiskCacheStrategy.ALL);
        if (placeholder != null) {
            request.placeholder(placeholder);
        }
        if (!anim) {
            request.dontAnimate();
        }
        request.into(imageView);
    }
}
