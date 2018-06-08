package com.angcyo.uiview.iview

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.angcyo.uiview.BuildConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.IChatDataType
import com.angcyo.uiview.recycler.adapter.RExItemHolder
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.viewgroup.FillLayout
import com.angcyo.uiview.viewgroup.RLinearLayout
import com.angcyo.uiview.widget.GlideImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：聊天item基础处理类
 * 创建人员：Robi
 * 创建时间：2018/06/06 15:40
 * 修改人员：Robi
 * 修改时间：2018/06/06 15:40
 * 修改备注：
 * Version: 1.0.0
 */
open class BaseChatItemHolder : RExItemHolder<IChatDataType>() {

    companion object {
        val timeFormat = SimpleDateFormat("yyyy-MM-dd_HH:mm", Locale.CHINA)
    }

    //最根的布局
    lateinit var chatItemRootLayout: RLinearLayout
    //消息顶部的时间
    lateinit var chatTimeTipView: TextView
    //头像, 名称, 内容包裹布局
    lateinit var chatItemWrapLayout: RLinearLayout
    //用户头像控制
    lateinit var chatAvatarControlLayout: FrameLayout
    //用户头像
    lateinit var chatAvatarView: GlideImageView
    //名称控制布局
    lateinit var chatNameControlLayout: LinearLayout
    //名称
    lateinit var chatNameView: TextView
    //内容反转方向布局
    lateinit var chatContentControlLayout: FillLayout
    //内容包裹布局, 用来显示不同的item
    lateinit var chatContentWrapLayout: FrameLayout
    //消息发送状态
    lateinit var chatStateControlLayout: LinearLayout

    //全屏item显示包裹布局
    lateinit var chatFullItemWrapLayout: FrameLayout

    override fun onBindItemDataView(holder: RBaseViewHolder, posInData: Int, dataBean: IChatDataType?) {
        chatItemRootLayout = holder.v(R.id.base_chat_item_root_layout)
        chatTimeTipView = holder.v(R.id.base_chat_time_tip_view)

        chatItemWrapLayout = holder.v(R.id.base_chat_item_wrap_layout)

        chatAvatarControlLayout = holder.v(R.id.base_chat_avatar_control_layout)
        chatAvatarView = holder.v(R.id.base_chat_avatar_view)

        chatNameControlLayout = holder.v(R.id.base_chat_name_control_layout)
        chatNameView = holder.v(R.id.base_chat_name_view)

        chatContentControlLayout = holder.v(R.id.base_chat_content_control_layout)
        chatContentWrapLayout = holder.v(R.id.base_chat_content_wrap_layout) //标准内容包裹

        chatStateControlLayout = holder.v(R.id.base_chat_state_control_layout)
        chatFullItemWrapLayout = holder.v(R.id.base_chat_full_item_wrap_layout) //全屏内容包裹

        initBaseLayout(holder, posInData, dataBean)
        initShowChatTimeTip(holder, posInData, dataBean)
        onBindChatItemView(holder, posInData, dataBean)

        if (BuildConfig.DEBUG) {
            chatItemRootLayout.setDebugInfoString(this.javaClass.simpleName)
        }
    }

    open fun initBaseLayout(holder: RBaseViewHolder, posInData: Int, dataBean: IChatDataType?) {
        if (isFullItemLayout(holder, posInData, dataBean)) {
            //全屏item
            chatItemWrapLayout.visibility = View.GONE
            chatFullItemWrapLayout.visibility = View.VISIBLE

            if (chatFullItemWrapLayout.childCount == 0) {
                LayoutInflater.from(getActivity()).inflate(getChatContentItemLayoutId(), chatFullItemWrapLayout)
            }
        } else {
            chatItemWrapLayout.visibility = View.VISIBLE
            chatFullItemWrapLayout.visibility = View.GONE

            if (chatContentWrapLayout.childCount == 0) {
                LayoutInflater.from(getActivity()).inflate(getChatContentItemLayoutId(), chatContentWrapLayout)
            }

            if (isReceiveItemLayout(holder, posInData, dataBean)) {
                //收到的消息
                chatContentControlLayout.reverseLayout = false
                chatItemWrapLayout.isReverseLayout = false
                chatNameControlLayout.visibility = View.VISIBLE
            } else {
                //发送的消息
                chatNameControlLayout.visibility = View.GONE //自动发送的消息, 不显示名称
                chatContentControlLayout.reverseLayout = true
                chatItemWrapLayout.isReverseLayout = true
            }
        }
    }

    open fun onBindChatItemView(holder: RBaseViewHolder, posInData: Int, dataBean: IChatDataType?) {

    }

    /**是否需要显示tip时间*/
    open fun initShowChatTimeTip(holder: RBaseViewHolder, posInData: Int, dataBean: IChatDataType?) {
        if (dataBean == null || posInData < 0) {
            chatTimeTipView.visibility = View.GONE
            return
        }
        var showTime = false //是否需要显示消息时间
        val chatTime = dataBean.chatTime

        if (dataBean.ignoreChatTime()) {
            showTime = false
        } else {
            if (posInData == 0) {
                showTime = true
            } else {
                //判断之前是否都是忽略时间的消息
                var isAllIgnoreChatTime = true
                for (i in posInData - 1 downTo 0) {
                    //L.e("call: initShowChatTimeTip -> $posInData $i")

                    if (exItemAdapter!!.allDatas[i].ignoreChatTime()) {

                    } else {
                        isAllIgnoreChatTime = false
                        break
                    }
                }

                if (isAllIgnoreChatTime) {
                    showTime = true
                } else {

                    var prevData = exItemAdapter!!.allDatas[posInData - 1]
                    //找到之前不需要忽略消息的数据item
                    for (i in posInData - 1 downTo 0) {
                        val dataType = exItemAdapter!!.allDatas[i]
                        if (dataType.ignoreChatTime()) {

                        } else {
                            prevData = dataType
                            break
                        }
                    }

                    val prevTime = prevData.chatTime
                    if ((chatTime - prevTime).absoluteValue >= 60 * 1000) {
                        //和之前的时间相隔一分钟
                        showTime = true
                    } else {
                        if (timeFormat.format(Date(chatTime)) != timeFormat.format(Date(prevTime))) {
                            //虽然时间间隔可能小于1分钟, 但是时间分钟以上变化了, 也显示时间
                            //防止一直聊天, 不停止.导致时间一直不显示
                            showTime = true
                        }
                    }
                }
            }
        }

        if (showTime) {
            chatTimeTipView.visibility = View.VISIBLE
            chatTimeTipView.text = RUtils.getShotTimeString(chatTime)
        } else {
            chatTimeTipView.visibility = View.GONE
        }
    }

    /**内容布局*/
    open fun getChatContentItemLayoutId(): Int {
        return R.layout.base_chat_text_layout
    }

    /**
     * 是否需要占满item  (在不需要显示标准的头像布局下使用)
     * */
    open fun isFullItemLayout(holder: RBaseViewHolder, posInData: Int, dataBean: IChatDataType?): Boolean {
        return false
    }

    /**
     * 是否是接收到的消息, 否则就是自己发送的消息
     * */
    open fun isReceiveItemLayout(holder: RBaseViewHolder, posInData: Int, dataBean: IChatDataType?): Boolean {
        return true
    }

}