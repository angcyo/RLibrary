package com.angcyo.uiview.iview

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIExItemUIView
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.kotlin.clickIt
import com.angcyo.uiview.kotlin.nowTime
import com.angcyo.uiview.kotlin.onDoubleTap
import com.angcyo.uiview.kotlin.onEmptyText
import com.angcyo.uiview.recycler.RRecyclerView
import com.angcyo.uiview.resources.AnimUtil
import com.angcyo.uiview.widget.ExEditText
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

    override fun needLoadData(): Boolean {
        return true
    }

    override fun onUILoadEmpty() {
        //super.onUILoadEmpty()
    }

    override fun onUILoadData(page: Int, extend: String?) {
        //super.onUILoadData(page, extend)
    }

    override fun initRecyclerView(recyclerView: RRecyclerView?, baseContentLayout: ContentLayout?) {
        super.initRecyclerView(recyclerView, baseContentLayout)
        recyclerView?.let {
            it.setOnFastTouchListener {
                //L.e("call: onFastClick -> ")
                onFastClick()
            }
            it.setOnTouchScrollListener(object : RRecyclerView.OnTouchScrollListener() {
                override fun onTouchScroll(recyclerView: RRecyclerView,
                                           downX: Float, downY: Float,
                                           eventX: Float, eventY: Float,
                                           dx: Int, dy: Int) {
                    super.onTouchScroll(recyclerView, downX, downY, eventX, eventY, dx, dy)
                    //L.e("call: onTouchScroll -> $downY $eventY : $dx $dy")
                    this@UIChatIView.onTouchScroll(recyclerView, downX, downY, eventX, eventY, dx, dy)
                }

                override fun onFastScrollToTop(recyclerView: RRecyclerView) {
                    super.onFastScrollToTop(recyclerView)
                    //L.e("call: onFastScrollToTop -> ")
                    this@UIChatIView.onFastScrollToTop(recyclerView)
                }
            })
        }
    }

    /**
     * 快速手指向上滑动, 用来在聊天界面显示键盘
     */
    open fun onFastScrollToTop(recyclerView: RRecyclerView) {
        if (recyclerView.isLastItemVisible) {
            showSoftInput(chatInputView)
        }
    }

    /**RecyclerView 滑动回调*/
    open fun onTouchScroll(recyclerView: RRecyclerView,
                           downX: Float, downY: Float,
                           eventX: Float, eventY: Float,
                           dx: Int, dy: Int) {
        if (eventY - downY > 5) {
            onBackPressed()
        }
    }

    /**在  RecyclerView 区域快速点击*/
    open fun onFastClick() {
        onBackPressed()
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
        if (isEmojiShow || isKeyboardShow) {
            scrollToLastBottom()
        }
    }

    /**滚动到底部*/
    open fun scrollToLastBottom(anim: Boolean = false) {
        mRecyclerView?.scrollToLastBottom(anim)
    }

    /**请根据需求, 填充相应的表情布局*/
    open fun initEmojiLayout(chatEmojiRootFrameLayout: FrameLayout, inflater: LayoutInflater) {

    }


    lateinit var chatInputView: ExEditText
    lateinit var chatSendButton: View
    open fun getInputLayoutId(): Int = R.layout.base_chat_input_layout

    /**请根据需求, 填充相应的输入框布局*/
    open fun initInputLayout(chatInputControlFrameLayout: FrameLayout, inflater: LayoutInflater) {
        inflater.inflate(getInputLayoutId(), chatInputControlFrameLayout).apply {
            chatInputView = findViewById(R.id.base_input_view)
            chatSendButton = findViewById(R.id.base_send_button)

            //文本框监听
            chatInputView.onEmptyText {
                if (it) {
                    if (chatSendButton.visibility == View.INVISIBLE) {
                        return@onEmptyText
                    }
                    chatSendButton.isEnabled = false
                    AnimUtil.scaleToMin(chatSendButton) {
                        chatSendButton.visibility = View.INVISIBLE
                    }
                } else {
                    if (chatSendButton.visibility == View.VISIBLE) {
                        return@onEmptyText
                    }
                    chatSendButton.isEnabled = true
                    chatSendButton.visibility = View.VISIBLE
                    AnimUtil.scaleToMax(chatSendButton)
                }
            }

            //双击发送
            chatInputView.onDoubleTap {
                onSendButtonClick(chatInputView.string())
            }

            //发送按钮监听
            chatSendButton.clickIt {
                onSendButtonClick(chatInputView.string())
            }
        }
    }

    open fun onSendButtonClick(inputText: String) {
        //清空输入
        chatInputView.setInputText("")
    }

    protected var lastAddMessageTime = 0L
    open fun addMessageToLast(dataBean: DataType) {
        mExBaseAdapter.addLastItem(dataBean)
        val nowTime = nowTime()
        if (nowTime - lastAddMessageTime > 300) {
            scrollToLastBottom(true)
        } else {
            scrollToLastBottom(false)
        }
        lastAddMessageTime = nowTime
    }
}