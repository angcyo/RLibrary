package com.angcyo.uiview.kotlin

import java.text.SimpleDateFormat

/**
 * Created by angcyo on ：2018/04/12 13:36
 * 修改备注：
 * Version: 1.0.0
 */

/**返回毫秒对应的天数*/
fun Long.toDay(): Int {
    return (this / (24 * 60 * 60 * 1000)).toInt()
}

fun Int.toDay(): Int {
    return this.toLong().toDay()
}

/**将字符串换算成毫秒*/
fun String.toMillis(pattern: String = "yyyyMMdd"): Long {
    val format: SimpleDateFormat = SimpleDateFormat.getDateInstance() as SimpleDateFormat
    format.applyPattern(pattern)
    var time = 0L
    try {
        time = format.parse(this).time
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return time
}