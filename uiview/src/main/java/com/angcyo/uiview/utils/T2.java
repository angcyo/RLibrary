package com.angcyo.uiview.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.skin.SkinHelper;

import java.lang.reflect.Field;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/01/14 09:53
 * 修改人员：Robi
 * 修改时间：2017/01/14 09:53
 * 修改备注：
 * Version: 1.0.0
 */
public class T2 {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_OK = 1;
    public static final int TYPE_INFO = 2;
    public static final int TYPE_ERROR = 3;
    public static boolean SHOW_TOAST_BG_COLOR = true;
    private static Toast toast;

    public static void show(Context context, CharSequence charSequence, int type) {
        show(context, charSequence, type, Gravity.CENTER);
    }

    public static void show(Context context, CharSequence charSequence, int type, int gravity) {
        if (!RUtils.isMainThread()) {
            L.e("试图在子线程显示Toast.");
            return;
        }

        View layout;
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
            initToast(toast);
            layout = LayoutInflater.from(context).inflate(R.layout.base_toast, null);
            ((TextView) layout.findViewById(R.id.base_toast_text_view)).setText(charSequence);
            toast.setView(layout);
            toast.setGravity(Gravity.TOP, 0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toast.getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        } else {
            layout = toast.getView();
        }

        TextView titleView = find(layout, R.id.base_toast_text_view);
        ImageView imageView = find(layout, R.id.base_toast_image_view);
        LinearLayout contentLayout = find(layout, R.id.base_toast_content_layout);
        View rootLayout = find(layout, R.id.base_toast_root_layout);

        contentLayout.setGravity(gravity);

        if (titleView != null) {
            titleView.setText(charSequence);
        }
        if (imageView != null) {
            if (type == TYPE_NONE) {
                if (SHOW_TOAST_BG_COLOR) {
                    rootLayout.setBackgroundResource(R.drawable.base_toast_shape);
                }
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.VISIBLE);

                switch (type % 4) {
                    case TYPE_OK:
                        imageView.setImageResource(R.drawable.base_ok);

                        if (SHOW_TOAST_BG_COLOR) {
                            rootLayout.setBackgroundResource(R.drawable.base_toast_shape_i);
                        }
                        break;
                    case TYPE_INFO:
                        rootLayout.setBackgroundResource(R.drawable.base_toast_shape_i);
                        imageView.setImageResource(R.drawable.base_info);
                        break;
                    case TYPE_ERROR:
                        imageView.setImageResource(R.drawable.base_failed_red);
                        rootLayout.setBackgroundResource(R.drawable.base_toast_shape_w);
                        break;
                }

                if (SHOW_TOAST_BG_COLOR) {
                    titleView.setTextColor(Color.WHITE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imageView.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                    }
                } else {
                    titleView.setTextColor(context.getResources().getColor(R.color.base_text_color_dark));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imageView.setImageTintList(ColorStateList.valueOf(SkinHelper.getSkin().getThemeSubColor()));
                    }
                }
            }
        }
        toast.show();
    }

    private static <T> T find(View view, int id) {
        return (T) view.findViewById(id);
    }

    private static void initToast(Toast toast) {
        try {
            Field mTN = toast.getClass().getDeclaredField("mTN");
            mTN.setAccessible(true);
            Object mTNObj = mTN.get(toast);

            Field mParams = mTNObj.getClass().getDeclaredField("mParams");
            mParams.setAccessible(true);
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams.get(mTNObj);
            params.width = -1;
            params.height = -2;// (int) dpToPx(context, T_HEIGHT);
//            params.gravity = Gravity.TOP;
            params.windowAnimations = R.style.BaseToastAnimation;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
