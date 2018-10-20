package com.angcyo.uiview.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

import com.angcyo.uiview.draw.RDrawText;
import com.angcyo.uiview.rsen.RefreshLayout;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/20
 */
public class TitleDrawTextView extends DrawTextView {

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

    int mCurrentOffset;

    int textSize;

    public TitleDrawTextView(Context context) {
        super(context);
    }

    public TitleDrawTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        drawText = new RTitleDrawText(this, attrs);
        textSize = drawText.getTextSize();
        setMinimumHeight((int) ((drawText.measureDrawHeight() + getPaddingTop() + getPaddingBottom()) * 0.7f));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {

            if (mOnOffsetChangedListener == null) {
                mOnOffsetChangedListener = new OffsetUpdateListener();
            }

            ((AppBarLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedListener);
        }
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
        OffsetUpdateListener() {
        }

        @Override
        public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
            mCurrentOffset = verticalOffset;
//            L.e(mCurrentOffset + "");
            /*标题栏文本放大缩小效果*/
            drawText.setTextSize((int) (textSize * (1 - Math.abs(verticalOffset) * 1f / getMeasuredHeight())));

            try {
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getParent().getParent();
                for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
                    View childAt = coordinatorLayout.getChildAt(i);
                    if (childAt instanceof RefreshLayout) {
                        childAt.setEnabled(mCurrentOffset >= 0);
                        break;
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }

        }
    }

    private class RTitleDrawText extends RDrawText {

        public RTitleDrawText(View view, AttributeSet attr) {
            super(view, attr);
        }

        @Override
        protected int getTextDrawY() {
            return RDrawText.textDrawCenterY(mBasePaint, 0, getBottom() - mCurrentOffset);
        }
    }
}
