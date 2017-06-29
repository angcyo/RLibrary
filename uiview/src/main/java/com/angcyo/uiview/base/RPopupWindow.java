package com.angcyo.uiview.base;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.PopupWindow;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/06/29 15:51
 * 修改人员：Robi
 * 修改时间：2017/06/29 15:51
 * 修改备注：
 * Version: 1.0.0
 */
public class RPopupWindow extends PopupWindow {

    public RPopupWindow(Context context) {
        super(context);

        setBackgroundDrawable(new ColorDrawable(Color.RED));
    }

    public static RPopupWindow build(Context context) {
        return new RPopupWindow(context);
    }
}
