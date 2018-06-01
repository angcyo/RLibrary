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
import com.angcyo.uiview.kotlin.ExKt;
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
    protected View anchorView;
    protected OnInitWindow mOnInitWindow;
    /**
     * 显示三角指示图标
     */
    protected boolean showTriangle = true;
    protected int layoutId = -1;
    protected int offsetY = 0;

    //检查是否在屏幕上/下部分
    protected boolean checkVertical = true;

    //检查是否在屏幕左/右部分
    protected boolean checkHorizontal = true;

    //在锚点的上方 or 下方
    protected int anchorGravity = Gravity.NO_GRAVITY;

    protected UIWindow(View anchorView) {
        this.anchorView = anchorView;
    }

    public static UIWindow build(View anchorView) {
        UIWindow uiWindow = new UIWindow(anchorView);
        return uiWindow;
    }

    public UIWindow show(ILayout iLayout) {
        iLayout.startIView(this);
        return this;
    }

    public UIWindow show(ILayout iLayout, int yoff) {
        offsetY = yoff;
        iLayout.startIView(this);
        return this;
    }

    public UIWindow layout(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
        return this;
    }

    public UIWindow setOffsetY(int offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public UIWindow setLayout(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
        return this;
    }

    public UIWindow onInitWindow(OnInitWindow initWindow) {
        mOnInitWindow = initWindow;
        return this;
    }

    /**
     * 是否显示三角形
     */
    public UIWindow setShowTriangle(boolean showTriangle) {
        this.showTriangle = showTriangle;
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
    final protected View inflateDialogView(FrameLayout dialogRootLayout, LayoutInflater inflater) {
        LinearLayout containLayout = new LinearLayout(mActivity);
        containLayout.setOrientation(LinearLayout.VERTICAL);

        if (canCanceledOnOutside()) {
            containLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishDialog();
                }
            });
        }

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

        if (checkHorizontal) {
            if (screenLocation[0] + anchorWidth / 2 < displayFrame.width() / 2) {
                //左半屏
                triangleParams.gravity = Gravity.START;
                triangleParams.leftMargin = screenLocation[0] + anchorWidth / 2 - triangleView.getMeasuredWidth() / 2;
            } else {
                //右半屏
                triangleParams.gravity = Gravity.END;
                triangleParams.rightMargin = displayFrame.right - screenLocation[0] - anchorWidth / 2 - triangleView.getMeasuredWidth() / 2;
            }
        } else {
            //左半屏, 默认在左边
            triangleParams.gravity = Gravity.START;
            triangleParams.leftMargin = screenLocation[0] + anchorWidth / 2 - triangleView.getMeasuredWidth() / 2;
        }

        if (checkVertical) {
            if (screenLocation[1] + anchorHeight / 2 < displayFrame.height() / 2) {
                //在屏幕上半部
                params.topMargin = screenLocation[1] + anchorHeight + offsetY;
                if (showTriangle) {
                    containLayout.addView(triangleView, triangleParams);
                }
                inflateWindowContent(containLayout);
                gravity = Gravity.TOP;
            } else {
                //在屏幕下半部
                inflateWindowContent(containLayout);
                if (showTriangle) {
                    triangleView.setRotation(180);
                    containLayout.addView(triangleView, triangleParams);
                }
                params.bottomMargin = displayFrame.bottom - screenLocation[1] + offsetY;
                params.gravity = Gravity.BOTTOM;
                gravity = Gravity.BOTTOM;
            }
        } else {
            //默认在屏幕上半部
            if (ExKt.have(anchorGravity, Gravity.TOP)) {
                //在锚点的上方
                inflateWindowContent(containLayout);
                if (showTriangle) {
                    triangleView.setRotation(180);
                    containLayout.addView(triangleView, triangleParams);
                }
                params.bottomMargin = displayFrame.bottom - screenLocation[1] + offsetY;
                params.gravity = Gravity.BOTTOM;
                gravity = Gravity.BOTTOM;
            } else {
                params.topMargin = screenLocation[1] + anchorHeight + offsetY;
                if (showTriangle) {
                    containLayout.addView(triangleView, triangleParams);
                }
                inflateWindowContent(containLayout);
                gravity = Gravity.TOP;
            }
        }

        dialogRootLayout.addView(containLayout, params);
        resetDialogGravity();

        if (mOnInitWindow != null) {
            mOnInitWindow.onInitWindow(this, new RBaseViewHolder(containLayout));
        }
        return containLayout;
    }

    protected void inflateWindowContent(LinearLayout containLayout) {
        LayoutInflater.from(mActivity).inflate(layoutId, containLayout);
    }

    public interface OnInitWindow {
        void onInitWindow(UIWindow window, RBaseViewHolder viewHolder);
    }
}
