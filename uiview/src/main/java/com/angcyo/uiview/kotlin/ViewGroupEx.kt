package com.angcyo.uiview.kotlin

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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

/**
 * 根据touch坐标, 返回touch的View
 */
public fun ViewGroup.findView(touchRawX: Float, touchRawY: Float): View? {
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

    return findView(this, touchRawX, touchRawY, offsetTop)
}

public fun ViewGroup.findView(targetView: View, touchRawX: Float, touchRawY: Float, offsetTop: Int = 0): View? {
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

        if (childAt is ViewGroup && childAt.childCount > 0) {
            val resultView = childAt.findView(targetView, touchRawX, touchRawY, offsetTop)
            if (resultView != null && resultView != targetView) {
                touchView = resultView
                break
            }
        } else {
            if (childAt.measuredHeight != 0 &&
                    childAt.measuredWidth != 0 &&
                    (childAt.left != childAt.right && childAt.left != 0) &&
                    (childAt.top != childAt.bottom && childAt.top != 0) &&
                    rect.contains(touchRawX.toInt(), touchRawY.toInt())) {
                touchView = childAt
                break
            }
        }
    }
    return touchView
}