package com.angcyo.uiview.iview

import android.view.LayoutInflater
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIExItemUIView
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.widget.RSoftInputLayout

/**
 * 聊天界面的封装
 * Created by angcyo on 2018/03/24 09:28
 */

open abstract class UIChatIView<ItemType, DataType> : UIExItemUIView<ItemType, DataType>() {
    /**键盘标签切换布局*/
    lateinit var softInputLayout: RSoftInputLayout
    /**最顶级的根布局*/
    lateinit var chatRootFrameLayout: FrameLayout
    /**内容顶级布局, 可以用来在聊天列表上悬浮显示 界面*/
    lateinit var chatContentRootFrameLayout: FrameLayout
    /**Emoji根布局*/
    lateinit var chatEmojiRootFrameLayout: FrameLayout
    /**输入控件控制布局, 用来填充输入框*/
    lateinit var chatInputControlFrameLayout: FrameLayout

    override fun onBackPressed(): Boolean {
        return softInputLayout.requestBackPressed()
    }

    override fun isShowInViewPager(): Boolean {
        return false
    }

    override fun getRecyclerRootLayoutId(): Int {
        return R.layout.base_chat_recycler_view_layout
    }

    override fun getDefaultLayoutState(): LayoutState {
        return LayoutState.CONTENT
    }

    override fun getDefaultBackgroundColor(): Int {
        return getColor(R.color.base_chat_bg_color)
    }

    override fun onUILoadData(page: Int, extend: String?) {
        //super.onUILoadData(page, extend)
    }

    override fun afterInflateView(baseContentLayout: ContentLayout) {
        super.afterInflateView(baseContentLayout)
        softInputLayout = baseContentLayout.findViewById(R.id.base_soft_input_layout)
        chatRootFrameLayout = baseContentLayout.findViewById(R.id.base_chat_root_layout)
        chatContentRootFrameLayout = baseContentLayout.findViewById(R.id.base_content_root_control_layout)
        chatEmojiRootFrameLayout = baseContentLayout.findViewById(R.id.base_emoji_root_control_layout)
        chatInputControlFrameLayout = baseContentLayout.findViewById(R.id.base_input_control_layout)

        softInputLayout.addOnEmojiLayoutChangeListener { isEmojiShow, isKeyboardShow, height ->
            onEmojiLayoutChange(isEmojiShow, isKeyboardShow, height)
        }

        val layoutInflater = LayoutInflater.from(mActivity)
        initInputLayout(chatInputControlFrameLayout, layoutInflater)
        initEmojiLayout(chatEmojiRootFrameLayout, layoutInflater)
    }

    /**键盘弹出事件*/
    open fun onEmojiLayoutChange(isEmojiShow: Boolean, isKeyboardShow: Boolean, height: Int) {

    }

    /**请根据需求, 填充相应的表情布局*/
    open fun initEmojiLayout(chatEmojiRootFrameLayout: FrameLayout, inflater: LayoutInflater) {

    }

    /**请根据需求, 填充相应的输入框布局*/
    open fun initInputLayout(chatInputControlFrameLayout: FrameLayout, inflater: LayoutInflater) {

    }
}