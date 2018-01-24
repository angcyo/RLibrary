package com.angcyo.uiview.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import com.angcyo.github.utilcode.utils.ClipboardUtils
import com.angcyo.library.utils.L
import com.angcyo.uiview.RApplication
import com.angcyo.uiview.utils.Tip

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * <pre>

<!-- 无障碍服务和权限 -->
<service
android:name=".main.RAccessibilityService"
android:enabled="true"
android:exported="true"
android:label="RSen微信辅助助手"
android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">

<intent-filter>
<action android:name="android.accessibilityservice.AccessibilityService"/>
</intent-filter>

<meta-data
android:name="android.accessibilityservice"
android:resource="@xml/base_accessibility_service"/>
</service>

 * </pre>
 * 创建人员：Robi
 * 创建时间：2018/01/24 13:51
 * 修改人员：Robi
 * 修改时间：2018/01/24 13:51
 * 修改备注：
 * Version: 1.0.0
 */
abstract class BaseAccessibilityService : AccessibilityService() {

    companion object {
        var isServiceConnected = false

        val TAG = "NodeInfo"

        /**打开辅助工具界面*/
        fun openAccessibilityActivity() {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                RApplication.getApp().startActivity(intent)
            } catch (e: Exception) {
                Tip.tip("打开失败\n${e.message}")
            }
        }

        /**
         * 获取 Service 是否启用状态
         *
         * @return
         */
        fun isServiceEnabled(): Boolean {
            val accessibilityManager: AccessibilityManager = RApplication.getApp().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val accessibilityServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            return accessibilityServices.any { it.id.contains(RApplication.getApp().packageName) } || isServiceConnected
        }

        /**调用node的点击事件*/
        fun clickNode(nodeInfo: AccessibilityNodeInfo) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        fun setNodeText(nodeInfo: AccessibilityNodeInfo, text: String) {
            val arguments = Bundle()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                ClipboardUtils.copyText(text)
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            } else {
                Tip.tip("设备不支持\n设置文本")
            }
        }

        /**向前滚动列表*/
        fun scrollForward(nodeInfo: AccessibilityNodeInfo) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }

        /**向后滚动列表*/
        fun scrollBackward(nodeInfo: AccessibilityNodeInfo) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }

        fun logNodeInfo(rootNodeInfo: AccessibilityNodeInfo) {
            val stringBuild = StringBuilder()
            Log.i(TAG, "╔═══════════════════════════════════════════════════════════════════════════════════════")
            debugNodeInfo(rootNodeInfo)
            Log.i(TAG, "╚═══════════════════════════════════════════════════════════════════════════════════════")
            //L.e("call: logNodeInfo -> $stringBuild")
        }

        fun debugNodeInfo(nodeInfo: AccessibilityNodeInfo, index: Int = 0 /*缩进控制*/, preIndex: String = "" /*child路径*/) {
            fun newLine(i: Int): String {
                val sb = StringBuilder()
                for (j in 0 until i) {
                    sb.append("    ")
                }
                return sb.toString()
            }

            val stringBuilder = StringBuilder("|")
            stringBuilder.append("${newLine(index)}")
            stringBuilder.append(nodeInfo.className)
//            stringBuilder.append(" idName:")
//            stringBuilder.append(nodeInfo.viewIdResourceName)
            if (TextUtils.equals(TextView::class.java.name, nodeInfo.className)) {
                stringBuilder.append("[")
                stringBuilder.append(nodeInfo.text)
                stringBuilder.append("]")
            }
            stringBuilder.append(" Count:")
            stringBuilder.append(nodeInfo.childCount)
            stringBuilder.append(" $preIndex")

            val rect = Rect()
            nodeInfo.getBoundsInScreen(rect)
            stringBuilder.append(" Bounds:")
            stringBuilder.append(rect)

            Log.i(TAG, "$stringBuilder")

            for (i in 0 until nodeInfo.childCount) {
                debugNodeInfo(nodeInfo.getChild(i), index + 1, "${if (preIndex.isEmpty()) preIndex else "${preIndex}_"}$i")
            }
        }
    }

    /**被中断了*/
    override fun onInterrupt() {
        L.e("call: onInterrupt -> ")
    }

    /**服务连接上*/
    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true

        L.e("call: onServiceConnected -> ")
    }

    override fun onDestroy() {
        super.onDestroy()
        L.e("call: onDestroy -> ")
    }

    /**服务断开*/
    override fun onUnbind(intent: Intent): Boolean {
        L.e("call: onUnbind -> $intent")
        isServiceConnected = false
        return super.onUnbind(intent)
    }

    /**核心方法, 收到事件*/
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            L.e("size:${windows.size} $windows $event")
        } else {
            L.e("call: onAccessibilityEvent -> $event")
        }
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                //当被监听的程序窗口状态变化时回调, 通常打开程序时会回调

                val view = View(applicationContext)
                view.layoutParams = ViewGroup.LayoutParams(100, 100)
                view.setBackgroundColor(Color.RED)
                //event.source.getChild(0).getChild(0).getChild(0).addChild(view)
                logNodeInfo(event.source)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                //当窗口上有内容发生变化的时候回调

            }
        }
    }


}