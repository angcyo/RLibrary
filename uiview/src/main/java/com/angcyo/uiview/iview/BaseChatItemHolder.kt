package com.angcyo.uiview.iview

import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.angcyo.uiview.R
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.IExBaseDataType
import com.angcyo.uiview.recycler.adapter.RExItemHolder
import com.angcyo.uiview.viewgroup.FillLayout
import com.angcyo.uiview.viewgroup.RConstraintLayout
import com.angcyo.uiview.viewgroup.RLinearLayout
import com.angcyo.uiview.widget.GlideImageView

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
open class BaseChatItemHolder : RExItemHolder<IExBaseDataType>() {
    //最根的布局
    lateinit var chatItemRootLayout: RLinearLayout
    //消息顶部的时间
    lateinit var chatTimeTipView: TextView
    //头像, 名称, 内容包裹布局
    lateinit var chatItemWrapLayout: RConstraintLayout
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

    override fun onBindItemDataView(holder: RBaseViewHolder, posInData: Int, dataBean: IExBaseDataType?) {
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
        onBindChatItemView(holder, posInData, dataBean)
    }

    open fun initBaseLayout(holder: RBaseViewHolder, posInData: Int, dataBean: IExBaseDataType?) {
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
                chatAvatarControlLayout.layoutParams = ConstraintLayout.LayoutParams((50 * density()).toInt(), (50 * density()).toInt()).apply {
                    leftToLeft = PARENT_ID
                }
                chatNameControlLayout.visibility = View.VISIBLE
                chatNameControlLayout.layoutParams = ConstraintLayout.LayoutParams(-2, -2).apply {
                    leftToRight = R.id.base_chat_avatar_control_layout
                    topToTop = R.id.base_chat_avatar_control_layout
                }
                chatContentControlLayout.reverseLayout = false
                chatContentControlLayout.layoutParams = ConstraintLayout.LayoutParams(0, -2).apply {
                    rightToRight = PARENT_ID
                    leftToRight = R.id.base_chat_avatar_control_layout
                    topToBottom = R.id.base_chat_name_control_layout
                }
            } else {
                //发送的消息
                chatAvatarControlLayout.layoutParams = ConstraintLayout.LayoutParams((50 * density()).toInt(), (50 * density()).toInt()).apply {
                    rightToRight = PARENT_ID
                }
                chatNameControlLayout.visibility = View.GONE //自动发送的消息, 不显示名称
                chatNameControlLayout.layoutParams = ConstraintLayout.LayoutParams(-2, -2).apply {
                    rightToLeft = R.id.base_chat_avatar_control_layout
                    topToTop = R.id.base_chat_avatar_control_layout
                }
                chatContentControlLayout.reverseLayout = true
                chatContentControlLayout.layoutParams = ConstraintLayout.LayoutParams(0, -2).apply {
                    leftToLeft = PARENT_ID
                    rightToLeft = R.id.base_chat_avatar_control_layout
                    topToBottom = R.id.base_chat_name_control_layout
                }
            }
        }
    }

    open fun onBindChatItemView(holder: RBaseViewHolder, posInData: Int, dataBean: IExBaseDataType?) {

    }

    /**内容布局*/
    open fun getChatContentItemLayoutId(): Int {
        return R.layout.base_text_item_selector_layout
    }

    /**
     * 是否需要占满item  (在不需要显示标准的头像布局下使用)
     * */
    open fun isFullItemLayout(holder: RBaseViewHolder, posInData: Int, dataBean: IExBaseDataType?): Boolean {
        return false
    }

    /**
     * 是否是接收到的消息, 否则就是自己发送的消息
     * */
    open fun isReceiveItemLayout(holder: RBaseViewHolder, posInData: Int, dataBean: IExBaseDataType?): Boolean {
        return true
    }

}