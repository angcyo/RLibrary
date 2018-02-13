package com.angcyo.uiview.dialog

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIIDialogRxImpl
import com.angcyo.uiview.view.RClickListener
import com.angcyo.uiview.widget.RTextView
import com.angcyo.uiview.widget.SimpleProgressBar

/**
 * 版本升级对话框
 * Created by angcyo on 2017/09/22 0022.
 */
class UIVersionDialog : UIIDialogRxImpl() {

    /**版本提示标题*/
    var versionTitle = ""
        set(value) {
            field = value
            titleView?.text = field
        }

    /**版本提示内容*/
    var versionContent = ""
        set(value) {
            field = value.replace("\\\\\\\\n", "\n").replace("\\\\n", "\n").replace("\\n", "\n")
            contentView?.text = field
        }

    /**是否强制升级*/
    var isForceUpdate = false
        set(value) {
            field = value
            cancelView?.visibility = if (field) View.GONE else View.VISIBLE

            setCanCancel(!isForceUpdate)
        }

    /**下载进度显示*/
    var progress = 0
        set(value) {
            field = value
            if (field > 0) {
                progressBar?.visibility = View.VISIBLE
            }
            progressBar?.setProgress(field)
        }

    /**不明确的进度*/
    var incertitudeProgress = false
        set(value) {
            field = value
            progressBar?.setIncertitudeProgress(field)
        }

    /**更新按钮提示文本*/
    var okButtonText = "立即更新"
        set(value) {
            field = value
            okView?.text = field
        }

    /**更新按钮单击事件*/
    var okButtonListener: View.OnClickListener? = null
        set(value) {
            field = value
            if (field is RClickListener) {
                okView?.setOnClickListener(field)
            } else {
                okView?.setOnClickListener(object : RClickListener() {
                    override fun onRClick(view: View?) {
                        field?.onClick(view)
                    }
                })
            }
        }

    init {
        setGravity(Gravity.CENTER)
        setCanCanceledOnOutside(false)
    }

    override fun inflateDialogView(dialogRootLayout: FrameLayout, inflater: LayoutInflater): View {
        return inflate(R.layout.base_version_dialog_layout)
    }

    private var titleView: RTextView? = null
    private var contentView: RTextView? = null
    private var okView: RTextView? = null

    private var cancelView: View? = null
    private var progressBar: SimpleProgressBar? = null

    override fun initDialogContentView() {
        super.initDialogContentView()
        titleView = mViewHolder.v(R.id.base_version_title_view)
        contentView = mViewHolder.v(R.id.base_version_content_view)
        okView = mViewHolder.v(R.id.base_version_download_view)

        cancelView = mViewHolder.v(R.id.base_version_cancel_view)
        progressBar = mViewHolder.v(R.id.progress_bar)

        titleView?.text = versionTitle
        contentView?.text = versionContent

        isForceUpdate = isForceUpdate

        incertitudeProgress = incertitudeProgress
        progress = progress

        okButtonListener = okButtonListener

        okButtonText = okButtonText

        click(R.id.base_version_cancel_view) {
            finishDialog()
        }
    }

}