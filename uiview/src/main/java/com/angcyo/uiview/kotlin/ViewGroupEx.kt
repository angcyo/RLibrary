package com.angcyo.uiview.kotlin

import android.app.Activity
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.EditText
import com.angcyo.uiview.widget.RSoftInputLayout

/**
 * Kotlin ViewGroup的扩展
 * Created by angcyo on 2017-07-26.
 */
/**
 * 计算child在parent中的位置坐标, 请确保child在parent中.
 * */
public fun ViewGroup.getLocationInParent(child: View, location: Rect) {
    var x = 0
    var y = 0

    var view = child
    while (view.parent != this) {
        x += view.left
        y += view.top
        view = view.parent as View
    }

    x += view.left
    y += view.top

    location.set(x, y, x + child.measuredWidth, y + child.measuredHeight)
}

/**返回当软键盘弹出时, 布局向上偏移了多少距离*/
public fun View.getLayoutOffsetTopWidthSoftInput(): Int {
    val rect = Rect()
    var offsetTop = 0

    try {
        val activity = this.context as Activity
        val softInputMode = activity.window.attributes.softInputMode
        if (softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
            val keyboardHeight = RSoftInputLayout.getSoftKeyboardHeight(this)

            /**在ADJUST_PAN模式下, 键盘弹出时, 坐标需要进行偏移*/
            if (keyboardHeight > 0) {
                //return targetView
                val findFocus = this.findFocus()
                if (findFocus is EditText) {
                    findFocus.getWindowVisibleDisplayFrame(rect)
                    offsetTop = findFocus.bottom - rect.bottom
                }
            }
        }

    } catch (e: Exception) {
    }
    return offsetTop
}


/**获取touch坐标对应的RecyclerView, 如果没有则null*/
public fun ViewGroup.getTouchOnRecyclerView(touchRawX: Float, touchRawY: Float): RecyclerView? {
    return findRecyclerView(this, touchRawX, touchRawY, getLayoutOffsetTopWidthSoftInput())
}

/**
 * 根据touch坐标, 返回touch的View
 */
public fun ViewGroup.findView(event: MotionEvent): View? {
    return findView(this, event.rawX, event.rawY, getLayoutOffsetTopWidthSoftInput())
}

public fun ViewGroup.findView(touchRawX: Float, touchRawY: Float): View? {
    return findView(this, touchRawX, touchRawY, getLayoutOffsetTopWidthSoftInput())
}

public fun ViewGroup.findView(targetView: View /*判断需要结果的View*/, touchRawX: Float, touchRawY: Float, offsetTop: Int = 0): View? {
    /**键盘的高度*/
    var touchView: View? = targetView
    val rect = Rect()

    for (i in childCount - 1 downTo 0) {
        val childAt = getChildAt(i)

        if (childAt.visibility != View.VISIBLE) {
            continue
        }

//        childAt.getWindowVisibleDisplayFrame(rect)
//        L.e("${this}:1 ->$i $rect")
        childAt.getGlobalVisibleRect(rect)
//        L.e("${this}:2 ->$i $rect")
//        L.e("call: ------------------end -> ")
        rect.offset(0, -offsetTop)

        fun check(view: View): View? {
            if (view.visibility == View.VISIBLE &&
                    view.measuredHeight != 0 &&
                    view.measuredWidth != 0 &&
                    (view.left != view.right) &&
                    (view.top != view.bottom) &&
                    rect.contains(touchRawX.toInt(), touchRawY.toInt())) {
                return view
            }
            return null
        }

        if (childAt is ViewGroup && childAt.childCount > 0) {
            val resultView = childAt.findView(targetView, touchRawX, touchRawY, offsetTop)
            if (resultView != null && resultView != targetView) {
                touchView = resultView
                break
            } else {
                val check = check(childAt)
                if (check != null) {
                    touchView = childAt
                    break
                }
            }
        } else {
            val check = check(childAt)
            if (check != null) {
                touchView = childAt
                break
            }
        }
    }
    return touchView
}

public fun ViewGroup.findRecyclerView(targetView: View /*判断需要结果的View*/, touchRawX: Float, touchRawY: Float, offsetTop: Int = 0): RecyclerView? {
    /**键盘的高度*/
    var touchView: RecyclerView? = null
    val rect = Rect()

    for (i in childCount - 1 downTo 0) {
        val childAt = getChildAt(i)

        if (childAt.visibility != View.VISIBLE) {
            continue
        }
        childAt.getGlobalVisibleRect(rect)
        rect.offset(0, -offsetTop)

        fun check(view: View): View? {
            if (view.visibility == View.VISIBLE &&
                    view.measuredHeight != 0 &&
                    view.measuredWidth != 0 &&
                    (view.left != view.right) &&
                    (view.top != view.bottom) &&
                    rect.contains(touchRawX.toInt(), touchRawY.toInt())) {
                return view
            }
            return null
        }

        if (childAt is RecyclerView) {
            val check = check(childAt)
            if (check != null) {
                touchView = childAt
                break
            }
        } else if (childAt is ViewGroup && childAt.childCount > 0) {
            val resultView = childAt.findRecyclerView(targetView, touchRawX, touchRawY, offsetTop)
            if (resultView != null) {
                touchView = resultView
                break
            }
        }
    }
    return touchView
}

/**将子View的数量, 重置到指定的数量*/
public fun ViewGroup.resetChildCount(newSize: Int, onAddView: () -> View) {
    val oldSize = childCount
    val count = newSize - oldSize
    if (count > 0) {
        //需要补充子View
        for (i in 0 until count) {
            addView(onAddView.invoke())
        }
    } else if (count < 0) {
        //需要移除子View
        for (i in 0 until count.abs()) {
            removeViewAt(oldSize - 1 - i)
        }
    }
}

/**动态添加View, 并初始化 (做了性能优化)*/
public fun <T> ViewGroup.addView(datas: List<T>, onAddViewCallback: OnAddViewCallback<T>) {
    this.resetChildCount(datas.size, {
        val layoutId = onAddViewCallback.getLayoutId()
        if (layoutId > 0) {
            LayoutInflater.from(context).inflate(layoutId, this, false)
        } else onAddViewCallback.getView()!!
    })

    for (i in datas.indices) {
        onAddViewCallback.onInitView(getChildAt(i), datas[i], i)
    }
}

abstract class OnAddViewCallback<T> {
    open fun getLayoutId(): Int = -1

    open fun getView(): View? = null

    open fun onInitView(view: View, data: T, index: Int) {

    }
}