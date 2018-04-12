package com.angcyo.uiview.kotlin

import com.angcyo.uiview.utils.RUtils

/**
 * Created by angcyo on ：2017/09/12 10:04
 * 修改备注：
 * Version: 1.0.0
 */

/**保留小数点后几位*/
public fun Float.decimal(bitNum: Int = 2 /*小数点后几位*/, halfUp: Boolean = true /*四舍五入*/): String {
    return RUtils.decimal(this, bitNum, halfUp)
}