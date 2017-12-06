package com.angcyo.uiview.utils;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/01/14 09:53
 * 修改人员：Robi
 * 修改时间：2017-12-1 17:54:12
 * 修改备注：
 * Version: 1.0.0
 */
public class Tip {
    private static Toast toast;

    public static void show(CharSequence tipText, int tipImageResId) {
        show(RApplication.getApp(), tipText, tipImageResId);
    }

    public static void tip(CharSequence tipText) {
        show(tipText, R.drawable.base_tip_ico);
    }

    public static void ok(CharSequence tipText) {
        show(tipText, R.drawable.base_tip_ok);
    }

    private static void show(Context context, CharSequence tipText, int tipImageResId) {
        View layout;
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
            initToast(toast);
            layout = LayoutInflater.from(context).inflate(R.layout.base_toast_tip, null);
            ((TextView) layout.findViewById(R.id.base_toast_text_view)).setText(tipText);
            toast.setView(layout);
            toast.setGravity(Gravity.CENTER, 0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toast.getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        } else {
            layout = toast.getView();
        }

        TextView titleView = find(layout, R.id.base_toast_text_view);
        ImageView imageView = find(layout, R.id.base_toast_image_view);

        if (titleView != null) {
            titleView.setText(tipText);
        }
        if (imageView != null) {
            imageView.setImageResource(tipImageResId);
        }
        toast.show();
    }

    private static <T> T find(View view, int id) {
        return (T) view.findViewById(id);
    }

    private static void initToast(Toast toast) {
//        try {
//            Field mTN = toast.getClass().getDeclaredField("mTN");
//            mTN.setAccessible(true);
//            Object mTNObj = mTN.get(toast);
//
//            Field mParams = mTNObj.getClass().getDeclaredField("mParams");
//            mParams.setAccessible(true);
//            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams.get(mTNObj);
//            params.width = -1;
//            params.height = -2;// (int) dpToPx(context, T_HEIGHT);
////            params.gravity = Gravity.TOP;
//            params.windowAnimations = R.style.BaseToastAnimation;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
