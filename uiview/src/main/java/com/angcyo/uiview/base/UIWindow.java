package com.angcyo.uiview.base;

import android.graphics.Rect;
import android.support.annotation.LayoutRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.angcyo.uiview.R;
import com.angcyo.uiview.container.ILayout;
import com.angcyo.uiview.recycler.RBaseViewHolder;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模拟 PopupWindow 的特性
 * 创建人员：Robi
 * 创建时间：2017/06/29 16:53
 * 修改人员：Robi
 * 修改时间：2017/06/29 16:53
 * 修改备注：
 * Version: 1.0.0
 */
public class UIWindow extends UIIDialogImpl {

    private final int[] mTmpDrawingLocation = new int[2];
    private final int[] mTmpScreenLocation = new int[2];

    /**
     * 锚点View
     */
    View anchorView;
    OnInitWindow mOnInitWindow;
    /**
     * 显示三角指示图标
     */
    private boolean showTriangle = true;
    private int layoutId = -1;
    private int offsetY = 0;

    private UIWindow(View anchorView) {
        this.anchorView = anchorView;
    }

    public static UIWindow build(View anchorView) {
        UIWindow uiWindow = new UIWindow(anchorView);
        return uiWindow;
    }

    public void show(ILayout iLayout) {
        show(iLayout, 0);
    }

    public void show(ILayout iLayout, int yoff) {
        offsetY = yoff;
        iLayout.startIView(this);
    }

    public UIWindow layout(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
        return this;
    }

    public UIWindow onInitWindow(OnInitWindow initWindow) {
        mOnInitWindow = initWindow;
        return this;
    }

    @Override
    public UIWindow setDimBehind(boolean dimBehind) {
        return (UIWindow) super.setDimBehind(dimBehind);
    }

    @Override
    public UIWindow setDimColor(int dimColor) {
        return (UIWindow) super.setDimColor(dimColor);
    }

    @Override
    public UIWindow setCanCanceledOnOutside(boolean canCanceledOnOutside) {
        return (UIWindow) super.setCanCanceledOnOutside(canCanceledOnOutside);
    }

    @Override
    protected View inflateDialogView(FrameLayout dialogRootLayout, LayoutInflater inflater) {
        LinearLayout containLayout = new LinearLayout(mActivity);
        containLayout.setOrientation(LinearLayout.VERTICAL);

        ImageView triangleView = new ImageView(mActivity);
        triangleView.setImageResource(R.drawable.base_trigon);
        if (showTriangle) {
            triangleView.measure(View.MeasureSpec.makeMeasureSpec(1 << 30 - 1, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(1 << 30 - 1, View.MeasureSpec.AT_MOST));
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -2);
        LinearLayout.LayoutParams triangleParams = new LinearLayout.LayoutParams(-2, -2);

//        final int[] drawingLocation = mTmpDrawingLocation;
//        anchorView.getLocationInWindow(drawingLocation);

        final Rect displayFrame = new Rect();
        anchorView.getWindowVisibleDisplayFrame(displayFrame);

        final int[] screenLocation = mTmpScreenLocation;
        anchorView.getLocationOnScreen(screenLocation);

        int anchorWidth = anchorView.getMeasuredWidth();
        int anchorHeight = anchorView.getMeasuredHeight();

        if (screenLocation[0] + anchorWidth / 2 < displayFrame.width() / 2) {
            //左半屏
            triangleParams.gravity = Gravity.START;
            triangleParams.leftMargin = screenLocation[0] + anchorWidth / 2 - triangleView.getMeasuredWidth() / 2;
        } else {
            //右半屏
            triangleParams.gravity = Gravity.END;
            triangleParams.rightMargin = displayFrame.right - screenLocation[0] - anchorWidth / 2 - triangleView.getMeasuredWidth() / 2;
        }

        if (screenLocation[1] + anchorHeight / 2 < displayFrame.height() / 2) {
            //在屏幕上半部
            params.topMargin = screenLocation[1] + anchorHeight + offsetY;
            if (showTriangle) {
                containLayout.addView(triangleView, triangleParams);
            }
            LayoutInflater.from(mActivity).inflate(layoutId, containLayout);
            gravity = Gravity.TOP;
        } else {
            //在屏幕下半部
            LayoutInflater.from(mActivity).inflate(layoutId, containLayout);
            if (showTriangle) {
                triangleView.setRotation(180);
                containLayout.addView(triangleView, triangleParams);
            }
            params.bottomMargin = displayFrame.bottom - screenLocation[1] + offsetY;
            params.gravity = Gravity.BOTTOM;
            gravity = Gravity.BOTTOM;
        }

        dialogRootLayout.addView(containLayout, params);
        resetDialogGravity();

        if (mOnInitWindow != null) {
            mOnInitWindow.onInitWindow(this, new RBaseViewHolder(containLayout));
        }
        return containLayout;
    }

    public interface OnInitWindow {
        void onInitWindow(UIWindow window, RBaseViewHolder viewHolder);
    }
}
