package com.angcyo.uiview.kotlin

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

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
    var touchView: View? = null
    val rect = Rect()
//    val intArray = intArrayOf(0, 0)
    for (i in childCount - 1 downTo 0) {
        val childAt = getChildAt(i)

//        L.e("call: ------------------ -> ")
//        childAt.getLocationInWindow(intArray)
//        L.e("call: findView -> ${intArray[0]} ${intArray[1]}")
//        childAt.getLocationOnScreen(intArray)
//        L.e("call: findView -> ${intArray[0]} ${intArray[1]}")
        childAt.getGlobalVisibleRect(rect)
//        L.e("${this}: findView ->$i $rect")
//        L.e("call: ------------------end -> ")

        if (childAt is ViewGroup && childAt.childCount > 0) {
            touchView = childAt.findView(touchRawX, touchRawY)
            if (touchView != null) {
                break
            }
        } else {
            if (rect.contains(touchRawX.toInt(), touchRawY.toInt())) {
                touchView = childAt
                break
            }
        }
    }
    return touchView
}