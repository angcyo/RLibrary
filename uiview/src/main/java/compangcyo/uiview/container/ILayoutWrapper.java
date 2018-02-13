package com.angcyo.uiview.container;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：当父ILayout嵌套ILayout的时候, 嵌套的ILayout需要用此View包裹, 才会测量
 * 创建人员：Robi
 * 创建时间：2017/06/30 18:30
 * 修改人员：Robi
 * 修改时间：2017/06/30 18:30
 * 修改备注：
 * Version: 1.0.0
 */
public class ILayoutWrapper extends RelativeLayout {
    public ILayoutWrapper(Context context) {
        super(context);
    }

    public ILayoutWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
