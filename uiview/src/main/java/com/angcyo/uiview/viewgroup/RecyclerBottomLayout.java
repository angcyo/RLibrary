package com.angcyo.uiview.viewgroup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/09
 */
public class RecyclerBottomLayout extends FrameLayout {

    public RecyclerBottomLayout(@NonNull Context context) {
        super(context);
    }

    public RecyclerBottomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerBottomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final ViewParent parent = getParent();
        Log.w("angcyo", "layout:" + top + " " + bottom);
        boolean callSuper = true;
        if (parent instanceof RecyclerView) {

            final RecyclerView recyclerView = ((RecyclerView) parent);
            final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) getLayoutParams();
            int parentHeight = recyclerView.getMeasuredHeight();
            if (recyclerView.computeVerticalScrollOffset() == 0 //只处理第一屏
                    && top < parentHeight //布局有部分展示了
                    && bottom > top) {
                if ((bottom + layoutParams.bottomMargin) != parentHeight) {
                    //布局未全部展示
                    //当前布局在RecyclerView的第一屏(没有任何滚动的状态), 并且底部没有显示全.

                    int spaceHeight = parentHeight - top - layoutParams.bottomMargin;
                    if (spaceHeight - layoutParams.topMargin - layoutParams.bottomMargin > 40 * getResources().getDisplayMetrics().density) {
                        //剩余空间足够大, 同时也解决了动态隐藏导航栏带来的BUG
                        callSuper = false;

                        layoutParams.height = spaceHeight;
                        setLayoutParams(layoutParams);

                        post(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("angcyo", "重置高度:" + layoutParams.height);
                                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                                if (adapter != null) {
                                    adapter.notifyItemChanged(layoutParams.getViewAdapterPosition());
                                } else {
                                    requestLayout();
                                }
                            }
                        });
                    }

                }
            }
        }

        if (callSuper) {
            super.onLayout(changed, left, top, right, bottom);
        }
    }
}
