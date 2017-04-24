package com.angcyo.uiview.widget;

import android.content.Context;
import android.util.AttributeSet;

import java.util.regex.Pattern;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持显示@显示, 支持显示 带logo的网页链接, 支持显示表情
 * 创建人员：Robi
 * 创建时间：2017/04/24 15:48
 * 修改人员：Robi
 * 修改时间：2017/04/24 15:48
 * 修改备注：
 * Version: 1.0.0
 */
public class RExTextView extends RTextView {
    public RExTextView(Context context) {
        super(context);
    }

    public RExTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RExTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    protected void p() {
        String p = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.:+#]*[\\w\\-+#])?";
        p.matches();
        Pattern pattern = Pattern.compile()
    }
}
