package com.angcyo.uiview.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.skin.SkinHelper;

/**
 * Created by angcyo on 2017-04-21.
 */

public class SkinLineView extends View {
    public SkinLineView(Context context) {
        super(context);
    }

    public SkinLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setBackgroundColor(SkinHelper.getSkin().getThemeSubColor());
    }
}
