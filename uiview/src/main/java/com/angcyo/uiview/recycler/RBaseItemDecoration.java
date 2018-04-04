package com.angcyo.uiview.recycler;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * Created by angcyo on 2016-10-16.
 */

public class RBaseItemDecoration extends RecyclerView.ItemDecoration {
    static int dividerColor = Color.parseColor("#E0E0E0");//分割线的颜色
    protected float mDividerSize;
    Drawable mDividerDrawableV;//垂直方向绘制的Drawable
    Drawable mDividerDrawableH;//水平方向绘制的Drawable
    /**
     * LinearLayoutManager中有效
     * VERTICAL 方向: padding 的是top 和 bottom
     * HORIZONTAL 方向: padding 的是left 和 right
     */
    int mMarginStart = 0, mMarginEnd = 0;

    /**
     * 是否绘制最后一个item的分割线
     */
    boolean drawLastVLine = false;//LinearLayoutManager.VERTICAL 时使用
    boolean drawLastHLine = false;//LinearLayoutManager.HORIZONTAL 时使用

    boolean drawFirstVLine = false;//
    boolean drawFirstHLine = false;//

    public RBaseItemDecoration() {
        this(1);
    }

    public RBaseItemDecoration(int dividerSize) {
        this(dividerSize, dividerColor);
    }

    public RBaseItemDecoration(Drawable drawable) {
        this(drawable, 1);
    }

    public RBaseItemDecoration(int dividerSize, int dividerColor) {
        mDividerSize = dividerSize;
        mDividerDrawableV = new ColorDrawable(dividerColor);
        mDividerDrawableH = mDividerDrawableV;
    }

    public RBaseItemDecoration(Drawable drawable, int dividerSize) {
        mDividerSize = dividerSize;
        mDividerDrawableV = drawable;
        mDividerDrawableH = drawable;
    }

    public static void setDividerColor(int dividerColor) {
        RBaseItemDecoration.dividerColor = dividerColor;
    }

    public RBaseItemDecoration setDrawLastLine(boolean drawLastLine) {
        this.drawLastVLine = drawLastLine;
        this.drawLastHLine = drawLastLine;
        return this;
    }

    public RBaseItemDecoration setDrawFirstLine(boolean drawFirstLine) {
        this.drawFirstVLine = drawFirstLine;
        this.drawFirstHLine = drawFirstLine;
        return this;
    }

    public RBaseItemDecoration setDrawLastVLine(boolean drawLastVLine) {
        this.drawLastVLine = drawLastVLine;
        return this;

    }

    public RBaseItemDecoration setDrawLastHLine(boolean drawLastHLine) {
        this.drawLastHLine = drawLastHLine;
        return this;
    }

    public RBaseItemDecoration setDrawFirstVLine(boolean drawFirstVLine) {
        this.drawFirstVLine = drawFirstVLine;
        return this;
    }

    public RBaseItemDecoration setDrawFirstHLine(boolean drawFirstHLine) {
        this.drawFirstHLine = drawFirstHLine;
        return this;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        final RecyclerView.LayoutManager manager = parent.getLayoutManager();

        int itemCount = manager.getItemCount();
        if (itemCount < 1) {
            //如果只有1个item, 直接返回;
            return;
        }
        if (manager instanceof GridLayoutManager) {
            //网格布局
            drawGrid(c, manager);
        } else if (manager instanceof StaggeredGridLayoutManager) {
            //暂不支持...
        } else {
            //线性布局
            final LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
            final int firstItem = layoutManager.findFirstVisibleItemPosition();
            for (int i = 0; i < layoutManager.getChildCount(); i++) {
                int adapterPosition = firstItem + i;

                final View view = layoutManager.findViewByPosition(adapterPosition);

                if (itemCount == 1 || adapterPosition == itemCount - 1) {
                    //第一个, 或者最后一个
                    if (!drawLastHLine && !drawLastVLine) {
                        continue;
                    }
                }

                if (view != null) {
                    if (layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                        //水平
                        if (adapterPosition == 0 && drawFirstHLine) {
                            drawDrawableVFirst(c, view);
                        }
                        drawDrawableV(c, view);
                    } else {
                        //垂直
                        if (adapterPosition == 0 && drawFirstVLine) {
                            drawDrawableHFirst(c, view);
                        }
                        drawDrawableH(c, view);
                    }
                }
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    //------------------------------------------公共方法---------------------------------

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();//布局管理器
        final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        final int viewLayoutPosition = layoutParams.getViewLayoutPosition();//布局时当前View的位置

        if (layoutManager.getItemCount() < 1) {
            //如果只有1个item, 直接返回;
            return;
        }

        if (layoutManager instanceof GridLayoutManager) {
            //请注意,这里的分割线 并不包括边框边上的分割线, 只处理不含边框的内部之间的分割线.
            offsetsOfGrid(outRect, ((GridLayoutManager) layoutManager), viewLayoutPosition);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            //暂时不支持...以后再写
        } else {
            //线性布局 就简单了
            LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) layoutManager);
            final int itemCount = layoutManager.getItemCount();
            if (linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                int leftSize = 0;
                int rightSize = 0;

                if (viewLayoutPosition == 0 && drawFirstHLine) {
                    leftSize = (int) mDividerSize;
                }

                //水平方向
                if (itemCount == 1 || viewLayoutPosition == itemCount - 1) {
                    //这里可以决定,第一个item的分割线
                    if (drawLastHLine) {
                        rightSize = (int) mDividerSize;//默认只有右边有分割线, 你也可以把左边的分割线添加出来}
                    } else {
                        //outRect.set(0, 0, 0, 0);//默认只有右边有分割线, 你也可以把左边的分割线添加出来}
                    }
                } else {
                    //中间的item,默认只有右边的分割线
                    rightSize = (int) mDividerSize;
                }

                outRect.set(leftSize, 0, rightSize, 0);
            } else {
                int topSize = 0;
                int bottomSize = 0;

                if (viewLayoutPosition == 0 && drawFirstVLine) {
                    topSize = (int) mDividerSize;
                }

                //垂直方向
                if (itemCount == 1 || viewLayoutPosition == itemCount - 1) {
                    //这里可以决定,第一个item的分割线
                    if (drawLastVLine) {
                        bottomSize = (int) mDividerSize; //默认只有右边有分割线, 你也可以把左边的分割线添加出来
                    } else {
                        //outRect.set(0, 0, 0, 0);
                    }
                } else {
                    //中间的item,默认只有右边的分割线
                    bottomSize = (int) mDividerSize;
                }

                outRect.set(0, topSize, 0, bottomSize);

            }
        }
    }

    public RBaseItemDecoration setDividerSize(float dividerSize) {
        mDividerSize = dividerSize;
        return this;
    }

    public RBaseItemDecoration setDividerDrawableV(Drawable dividerDrawableV) {
        mDividerDrawableV = dividerDrawableV;
        return this;
    }

    public RBaseItemDecoration setDividerDrawableH(Drawable dividerDrawableH) {
        mDividerDrawableH = dividerDrawableH;
        return this;
    }

    public RBaseItemDecoration setMarginStart(int marginStart) {
        mMarginStart = marginStart;
        return this;
    }

    public RBaseItemDecoration setMarginEnd(int marginEnd) {
        mMarginEnd = marginEnd;
        return this;
    }


    //------------------------------------------私有方法---------------------------------

    /**
     * GridLayoutManager 布局, 计算每个Item, 应该留出的空间(用来绘制分割线的空间)
     */
    private void offsetsOfGrid(Rect outRect, GridLayoutManager layoutManager, int viewLayoutPosition) {
        final int spanCount = layoutManager.getSpanCount();
        int itemCount = layoutManager.getItemCount();

        int right = 0, bottom = 0, left = 0, top = 0;
        if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
            //垂直方向
            bottom = (int) mDividerSize;
            right = (int) mDividerSize;

            if (viewLayoutPosition < spanCount) {
                if (drawFirstHLine) {
                    top = (int) mDividerSize;
                }
            }

            if (viewLayoutPosition % spanCount == 0) {
                if (drawFirstVLine) {
                    left = (int) mDividerSize;
                }
            }

            if (isLastOfGrid(itemCount, viewLayoutPosition, spanCount)/*判断是否是最后一排*/) {
                if (drawLastHLine) {
                } else {
                    bottom = 0;
                }
            }
            if (isEndOfGrid(itemCount, viewLayoutPosition, spanCount)/*判断是否是最靠右的一排*/) {
                if (drawLastVLine) {
                } else {
                    right = 0;
                }
            }
        } else {
            //水平方向
            bottom = (int) mDividerSize;
            right = (int) mDividerSize;

            if (viewLayoutPosition < spanCount) {
                if (drawFirstVLine) {
                    left = (int) mDividerSize;
                }
            }

            if (viewLayoutPosition % spanCount == 0) {
                if (drawFirstHLine) {
                    top = (int) mDividerSize;
                }
            }

            if (isLastOfGrid(itemCount, viewLayoutPosition, spanCount)/*判断是否是最后一排*/) {
                if (drawLastVLine) {
                } else {
                    right = 0;
                }
            }
            if (isEndOfGrid(itemCount, viewLayoutPosition, spanCount)/*判断是否是最靠底部的一排*/) {
                if (drawLastHLine) {
                } else {
                    bottom = 0;
                }
            }
        }

        outRect.set(left, top, right, bottom);
    }

    /**
     * 判断 viewLayoutPosition 是否是一排的结束位置 (垂直水平通用)
     */
    private boolean isEndOfGrid(int itemCount, int viewLayoutPosition, int spanCount) {
        return viewLayoutPosition % spanCount == spanCount - 1 || itemCount - 1 == viewLayoutPosition;
    }

    /**
     * 判断 viewLayoutPosition 所在的位置,是否是最后一排(垂直水平通用)
     */
    private boolean isLastOfGrid(int itemCount, int viewLayoutPosition, int spanCount) {
        boolean result = false;
        final double ceil = Math.ceil(((float) itemCount) / spanCount);
        if (viewLayoutPosition >= ceil * spanCount - spanCount) {
            result = true;
        }
        return result;
    }

    protected void drawGrid(Canvas c, RecyclerView.LayoutManager manager) {
        final GridLayoutManager layoutManager = (GridLayoutManager) manager;
        final int spanCount = layoutManager.getSpanCount();
        int itemCount = layoutManager.getItemCount();

        final int firstItem = layoutManager.findFirstVisibleItemPosition();
        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            final View view = layoutManager.findViewByPosition(firstItem + i);
            if (view != null) {
                final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
                final int viewLayoutPosition = layoutParams.getViewLayoutPosition();//布局时当前View的位置

                if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {

                    if (viewLayoutPosition < spanCount) {
                        if (drawFirstHLine) {
                            drawDrawableHFirst(c, view);
                        }
                    }

                    if (viewLayoutPosition % spanCount == 0) {
                        if (drawFirstVLine) {
                            drawDrawableVFirst(c, view);
                        }
                    }

                    //垂直方向
                    if (isLastOfGrid(itemCount, viewLayoutPosition, spanCount)/*判断是否是最后一排*/) {
                        if (viewLayoutPosition == itemCount - 1 /*最后一个*/) {
                            if (drawLastVLine) {
                                drawDrawableV(c, view);
                            }
                            if (drawLastHLine) {
                                drawDrawableH(c, view);
                            }
                        } else {
                            if (drawLastHLine) {
                                drawDrawableH(c, view);
                            }
                            drawDrawableV(c, view);
                        }
                    } else {
                        if (isEndOfGrid(itemCount, viewLayoutPosition, spanCount)/*判断是否是最靠右的一排*/) {
                            drawDrawableH(c, view);

                            if (drawLastVLine) {
                                drawDrawableV(c, view);
                            }
                        } else {
                            drawDrawableH(c, view);
                            drawDrawableV(c, view);
                        }
                    }
                } else {
                    if (viewLayoutPosition < spanCount) {
                        if (drawFirstVLine) {
                            drawDrawableVFirst(c, view);
                        }
                    }

                    if (viewLayoutPosition % spanCount == 0) {
                        if (drawFirstHLine) {
                            drawDrawableHFirst(c, view);
                        }
                    }

                    //水平方向
                    if (isLastOfGrid(itemCount, viewLayoutPosition, spanCount)) {
                        if (viewLayoutPosition == itemCount - 1 /*最后一个*/) {
                            if (drawLastVLine) {
                                drawDrawableV(c, view);
                            }
                            if (drawLastHLine) {
                                drawDrawableH(c, view);
                            }
                        } else {
                            if (drawLastVLine) {
                                drawDrawableV(c, view);
                            }
                            drawDrawableH(c, view);
                        }
                    } else {
                        if (isEndOfGrid(itemCount, viewLayoutPosition, spanCount)) {
                            drawDrawableV(c, view);

                            if (drawLastHLine) {
                                drawDrawableH(c, view);
                            }
                        } else {
                            drawDrawableH(c, view);
                            drawDrawableV(c, view);
                        }
                    }
                }
            }
        }
    }

    /**
     * 绘制view对应垂直方向的分割线
     */
    protected void drawDrawableV(Canvas c, View view) {
        final RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
        mDividerDrawableV.setBounds(
                view.getRight() + p.rightMargin,
                view.getTop() + mMarginStart,
                (int) (view.getRight() + p.rightMargin + mDividerSize),
                (int) (view.getBottom() - mMarginEnd + mDividerSize));
        drawDrawable(c, mDividerDrawableV);
    }

    /**
     * 绘制view对应水平方向的分割线
     */
    protected void drawDrawableH(Canvas c, View view) {
        final RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
        mDividerDrawableH.setBounds(
                view.getLeft() + mMarginStart,
                view.getBottom() + p.bottomMargin,
                view.getRight() - mMarginEnd,
                (int) (view.getBottom() + p.bottomMargin + mDividerSize));
        drawDrawable(c, mDividerDrawableH);
    }

    private void drawDrawableHFirst(Canvas c, View view) {
        //final RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
        mDividerDrawableH.setBounds(
                (int) (view.getLeft() + mMarginStart - mDividerSize),
                (int) (view.getTop() - mDividerSize),
                (int) (view.getRight() - mMarginEnd + mDividerSize),
                view.getTop());
        drawDrawable(c, mDividerDrawableH);
    }

    private void drawDrawableVFirst(Canvas c, View view) {
        //final RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
        mDividerDrawableV.setBounds(
                (int) (view.getLeft() - mDividerSize),
                view.getTop() + mMarginStart,
                view.getLeft(),
                (int) (view.getBottom() - mMarginEnd + mDividerSize));
        drawDrawable(c, mDividerDrawableV);
    }

    protected void drawDrawable(Canvas c, Drawable drawable) {
        drawable.draw(c);
    }
}
