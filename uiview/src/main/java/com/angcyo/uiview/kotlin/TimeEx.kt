package com.angcyo.uiview.kotlin

import com.angcyo.uiview.utils.RUtils
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

/**当前时间和现在时间对比, 还剩多少天*/
fun Long.toNowDay(): Int {
    return (this - nowTime()).toDay()
}

/**返回毫秒对应的天数*/
fun Int.toDay(): Int {
    return this.toLong().toDay()
}

/**
 * 00:00的格式输出, 如果有小时: 01:00:00
 */
fun Long.toHHmmss(showMill: Boolean = false /*显示毫秒*/): String {
    val formatTime = RUtils.formatTime(this)
    return if (showMill) {
        "$formatTime.${this % 1000L}"
    } else {
        formatTime
    }
}

/**时间全格式输出*/
fun Long.fullTime(): String {
    return RUtils.yyyyMMdd("yyyy-MM-dd HH:mm:ss.SSS", this)
}

/**格式化时间输出*/
fun Long.toTime(pattern: String = "yyyy-MM-dd HH:mm"): String {
    return RUtils.yyyyMMdd(pattern, this)
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