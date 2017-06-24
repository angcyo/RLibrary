package com.angcyo.uiview.kotlin

import android.view.View

/**
 * Kotlin View的扩展
 * Created by angcyo on 2017-06-03.
 */

@Suppress("UNCHECKED_CAST")
public fun <V : View> View.v(id: Int): V {
    return this.findViewById<V>(id)
}

public val View.scaledDensity: Float get() {
    return resources.displayMetrics.scaledDensity
}
public val View.density: Float get() {
    return resources.displayMetrics.density
}
