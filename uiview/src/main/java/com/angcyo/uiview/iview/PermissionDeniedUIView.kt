package com.angcyo.uiview.iview

import android.os.Bundle
import android.widget.TextView
import com.angcyo.uiview.R
import com.angcyo.uiview.RCrashHandler
import com.angcyo.uiview.base.Item
import com.angcyo.uiview.base.SingleItem
import com.angcyo.uiview.base.UIItemUIView
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.utils.UI

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：权限被禁止后的界面
 * 创建人员：Robi
 * 创建时间：2017/10/18 17:20
 * 修改人员：Robi
 * 修改时间：2017/10/18 17:20
 * 修改备注：
 * Version: 1.0.0
 */
class PermissionDeniedUIView(val permission: String) : UIItemUIView<SingleItem>() {
    override fun createItems(items: MutableList<SingleItem>) {
        items.add(object : SingleItem() {
            override fun onBindView(holder: RBaseViewHolder, posInData: Int, dataBean: Item?) {
                holder.click(R.id.base_button_view) {
                    RUtils.openAppDetailView(mActivity, mActivity.packageName)
                }

                val textView: TextView = holder.v(R.id.base_text_view)

                var singleText: String = permission
                var detailText: String = permission
                val split = permission.split(":")
                for (s in split) {
                    val s1 = permissions[s]
                    if (s1.isNullOrEmpty()) {

                    } else {
                        singleText = singleText.replace(s, s1!!, true)
                        detailText = detailText.replace(s, s + "\n" + s1!! + "\n", true)
                    }
                }
                textView.text = singleText.replace(":", "\n")
                holder.click(textView) {
                    UI.setViewHeight(holder.itemView, -2)
                    textView.text = detailText.replace(":", "\n")
                }

            }

            override fun getItemLayoutId(): Int {
                return R.layout.base_view_permission_denied
            }

        })
    }

    val permissions = mapOf(
            "android.permission.WRITE_EXTERNAL_STORAGE" to "写入外部存储",
            "android.permission.ACCESS_CHECKIN_PROPERTIES" to "访问登记属性",
            "android.permission.ACCESS_COARSE_LOCATION" to "获取错略位置",
            "android.permission.ACCESS_FINE_LOCATION" to "获取精确位置",
            "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" to "访问定位额",
            "android.permission.ACCESS_MOCK_LOCATION" to "获取模拟定位信息",
            "android.permission.ACCESS_NETWORK_STATE" to "获取网络状态",
            "android.permission.ACCESS_SURFACE_FLINGER" to "访问Surface Flinger",
            "android.permission.ACCESS_WIFI_STATE" to "获取WiFi状态",
            "android.permission.ACCOUNT_MANAGER" to "账户管理",
            "android.permission.AUTHENTICATE_ACCOUNTS" to "验证账户",
            "android.permission.BATTERY_STATS" to "电量统计",
            "android.permission.BIND_APPWIDGET" to "绑定小插件",
            "android.permission.BIND_DEVICE_ADMIN" to "绑定设备管理",
            "android.permission.BIND_INPUT_METHOD" to "绑定输入法",
            "android.permission.BIND_REMOTEVIEWS" to "绑定RemoteView",
            "android.permission.BIND_WALLPAPER" to "绑定壁纸",
            "android.permission.BLUETOOTH" to "使用蓝牙",
            "android.permission.BLUETOOTH_ADMIN" to "蓝牙管理",
            "android.permission.BRICK" to "变成砖头",
            "android.permission.BROADCAST_PACKAGE_REMOVED" to "应用删除时广播",
            "android.permission.BROADCAST_SMS" to "收到短信时广播",
            "android.permission.BROADCAST_STICKY" to "连续广播",
            "android.permission.BROADCAST_WAP_PUSH" to "WAP PUSH广播",
            "android.permission.CALL_PHONE" to "拨打电话",
            "android.permission.CALL_PRIVILEGED" to "通话权限",
            "android.permission.CAMERA" to "拍照权限",
            "android.permission.CHANGE_COMPONENT_ENABLED_STATE" to "改变组件状态",
            "android.permission.CHANGE_CONFIGURATION" to "改变配置",
            "android.permission.CHANGE_NETWORK_STATE" to "改变网络状态",
            "android.permission.CHANGE_WIFI_MULTICAST_STATE" to "改变WiFi多播状态",
            "android.permission.CHANGE_WIFI_STATE" to "改变WiFi状态",
            "android.permission.CLEAR_APP_CACHE" to "清除应用缓存",
            "android.permission.CLEAR_APP_USER_DATA" to "清除用户数据",
            "android.permission.CWJ_GROUP" to "底层访问权限",
            "android.permission.CELL_PHONE_MASTER_EX" to "手机优化大师扩展权限",
            "android.permission.CONTROL_LOCATION_UPDATES" to "控制定位更新",
            "android.permission.DELETE_CACHE_FILES" to "删除缓存文件",
            "android.permission.DELETE_PACKAGES" to "删除应用",
            "android.permission.DEVICE_POWER" to "电源管理",
            "android.permission.DIAGNOSTIC" to "应用诊断",
            "android.permission.DISABLE_KEYGUARD" to "禁用键盘锁",
            "android.permission.DUMP" to "转存系统信息",
            "android.permission.EXPAND_STATUS_BAR" to "状态栏控制",
            "android.permission.FACTORY_TEST" to "工厂测试模式",
            "android.permission.FLASHLIGHT" to "使用闪光灯",
            "android.permission.FORCE_BACK" to "强制后退",
            "android.permission.GET_ACCOUNTS" to "访问账户Gmail列表",
            "android.permission.GET_PACKAGE_SIZE" to "获取应用大小",
            "android.permission.GET_TASKS" to "获取任务信息",
            "android.permission.GLOBAL_SEARCH" to "允许全局搜索",
            "android.permission.HARDWARE_TEST" to "硬件测试",
            "android.permission.INJECT_EVENTS" to "注射事件",
            "android.permission.INSTALL_LOCATION_PROVIDER" to "安装定位提供",
            "android.permission.INSTALL_PACKAGES" to "安装应用程序",
            "android.permission.INTERNAL_SYSTEM_WINDOW" to "内部系统窗口",
            "android.permission.INTERNET" to "访问网络",
            "android.permission.KILL_BACKGROUND_PROCESSES" to "结束后台进程",
            "android.permission.MANAGE_ACCOUNTS" to "管理账户",
            "android.permission.MANAGE_APP_TOKENS" to "管理程序引用",
            "android.permission.MTWEAK_USER" to "高级权限",
            "android.permission.MTWEAK_FORUM" to "社区权限",
            "android.permission.MASTER_CLEAR" to "软格式化",
            "android.permission.MODIFY_AUDIO_SETTINGS" to "修改声音设置",
            "android.permission.MODIFY_PHONE_STATE" to "修改电话状态",
            "android.permission.MOUNT_FORMAT_FILESYSTEMS" to "格式化文件系统",
            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS" to "挂载文件系统",
            "android.permission.NFC" to "允许NFC通讯",
            "android.permission.PERSISTENT_ACTIVITY" to "永久Activity",
            "android.permission.PROCESS_OUTGOING_CALLS" to "处理拨出电话",
            "android.permission.READ_CALENDAR" to "读取日程提醒",
            "android.permission.READ_CONTACTS" to "读取联系人",
            "android.permission.READ_FRAME_BUFFER" to "屏幕截图",
            "android.permission.READ_INPUT_STATE" to "读取输入状态",
            "android.permission.READ_LOGS" to "读取系统日志",
            "android.permission.READ_PHONE_STATE" to "读取电话状态",
            "android.permission.READ_SMS" to "读取短信内容",
            "android.permission.READ_SYNC_SETTINGS" to "读取同步设置",
            "android.permission.READ_SYNC_STATS" to "读取同步状态",
            "android.permission.REBOOT" to "重启设备",
            "android.permission.RECEIVE_BOOT_COMPLETED" to "开机自动允许",
            "android.permission.RECEIVE_MMS" to "接收彩信",
            "android.permission.RECEIVE_SMS" to "接收短信",
            "android.permission.RECEIVE_WAP_PUSH" to "接收Wap Push",
            "android.permission.RECORD_AUDIO" to "录音",
            "android.permission.REORDER_TASKS" to "排序系统任务",
            "android.permission.RESTART_PACKAGES" to "结束系统任务",
            "android.permission.SEND_SMS" to "发送短信",
            "android.permission.SET_ACTIVITY_WATCHER" to "设置Activity观察其",
            "com.android.alarm.permission.SET_ALARM" to "设置闹铃提醒",
            "android.permission.SET_ALWAYS_FINISH" to "设置总是退出",
            "android.permission.SET_ANIMATION_SCALE" to "设置动画缩放",
            "android.permission.SET_DEBUG_APP" to "设置调试程序",
            "android.permission.SET_ORIENTATION" to "设置屏幕方向",
            "android.permission.SET_PREFERRED_APPLICATIONS" to "设置应用参数",
            "android.permission.SET_PROCESS_LIMIT" to "设置进程限制",
            "android.permission.SET_TIME" to "设置系统时间",
            "android.permission.SET_TIME_ZONE" to "设置系统时区",
            "android.permission.SET_WALLPAPER" to "设置桌面壁纸",
            "android.permission.SET_WALLPAPER_HINTS" to "设置壁纸建议",
            "android.permission.SIGNAL_PERSISTENT_PROCESSES" to "发送永久进程信号",
            "android.permission.STATUS_BAR" to "状态栏控制" to "允许程序打",
            "android.permission.SUBSCRIBED_FEEDS_READ" to "访问订阅内容",
            "android.permission.SUBSCRIBED_FEEDS_WRITE" to "写入订阅内容",
            "android.permission.SYSTEM_ALERT_WINDOW" to "显示系统窗口",
            "android.permission.UPDATE_DEVICE_STATS" to "更新设备状态",
            "android.permission.USE_CREDENTIALS" to "使用证书",
            "android.permission.USE_SIP" to "使用SIP视频",
            "android.permission.VIBRATE" to "使用振动",
            "android.permission.WAKE_LOCK" to "唤醒锁定",
            "android.permission.WRITE_APN_SETTINGS" to "写入GPRS接入点设置",
            "android.permission.WRITE_CALENDAR" to "写入日程提醒",
            "android.permission.WRITE_CONTACTS" to "写入联系人",
            "android.permission.WRITE_EXTERNAL_STORAGE" to "写入外部存储",
            "android.permission.WRITE_GSERVICES" to "写入Google地图数据",
            "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS" to "写入收藏夹和历史记录",
            "android.permission.WRITE_SECURE_SETTINGS" to "读写系统敏感设置",
            "android.permission.WRITE_SETTINGS" to "读写系统设置",
            "android.permission.WRITE_SMS" to "编写短信",
            "com.android.browser.permission.READ_HISTORY_BOOKMARKS" to "读取收藏夹和历史记录"
    )

    override fun getTitleBar(): TitleBarPattern? = null

    override fun getDefaultBackgroundColor(): Int {
        return super.getDefaultBackgroundColor()
    }

    override fun initOnShowContentLayout() {
        super.initOnShowContentLayout()
    }

    override fun onBackPressed(): Boolean {
        finishActivity()
        return false
        //return super.onBackPressed()
    }

    override fun onViewShowNotFirst(bundle: Bundle?) {
        super.onViewShowNotFirst(bundle)
        finishActivity()
        RCrashHandler.resetStartActivity(mActivity)
    }

    override fun canTryCaptureView(): Boolean {
        return false
    }

    override fun onViewShow(bundle: Bundle?) {
        super.onViewShow(bundle)
    }

    override fun onViewHide() {
        super.onViewHide()
    }

    override fun isLightStatusBar(): Boolean {
        return true
    }
}
