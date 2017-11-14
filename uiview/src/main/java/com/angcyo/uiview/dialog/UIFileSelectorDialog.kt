package com.angcyo.uiview.dialog

import android.graphics.Color
import android.text.TextUtils
import android.text.format.Formatter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import com.angcyo.github.utilcode.utils.FileUtils
import com.angcyo.library.okhttp.Ok
import com.angcyo.library.utils.L
import com.angcyo.uiview.R
import com.angcyo.uiview.Root
import com.angcyo.uiview.base.UIIDialogImpl
import com.angcyo.uiview.kotlin.minValue
import com.angcyo.uiview.net.RFunc
import com.angcyo.uiview.net.RSubscriber
import com.angcyo.uiview.net.Rx
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.RBaseAdapter
import com.angcyo.uiview.recycler.widget.IShowState
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.viewgroup.RLinearLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：文件选择对话框
 * 创建人员：Robi
 * 创建时间：2017/10/24 18:38
 * 修改人员：Robi
 * 修改时间：2017/10/24 18:38
 * 修改备注：
 * Version: 1.0.0
 */
open class UIFileSelectorDialog : UIIDialogImpl {
    init {
        setGravity(Gravity.BOTTOM)
    }

    /**是否显示隐藏文件*/
    var showHideFile = false

    private val storageDirectory = Root.externalStorageDirectory()

    private var targetPath: String = storageDirectory
        set(value) {

            if (value.isNotEmpty() && value.startsWith(storageDirectory)) {
                if (File(value).isDirectory) {
                    field = value
                }
            } else {
                field = storageDirectory
            }
        }

    private val simpleFormat by lazy {
        SimpleDateFormat("yyyy/MM/dd", Locale.CHINA)
    }

    /*选中的文件*/
    private var selectorFilePath: String = ""
    private var onFileSelector: ((File) -> Unit)? = null

    constructor(initPath: String = "", onFileSelector: ((File) -> Unit)? = null) {
        targetPath = initPath
        this.onFileSelector = onFileSelector
    }

    constructor(onFileSelector: ((File) -> Unit)? = null) {
        this.onFileSelector = onFileSelector
    }

    override fun inflateDialogView(dialogRootLayout: FrameLayout?, inflater: LayoutInflater?): View {
        return inflate(R.layout.base_dialog_file_selector)
    }

    override fun getOffsetScrollTop(): Int {
        return (resources.displayMetrics.heightPixels) / 2
    }

    override fun enableTouchBack(): Boolean {
        return true
    }

    private fun formatTime(time: Long): String = simpleFormat.format(time)

    private fun formatFileSize(size: Long): String = Formatter.formatFileSize(mActivity, size)

    private fun getFileList(path: String, onFileList: (List<FileItem>) -> Unit) {
        Rx.base(object : RFunc<List<FileItem>>() {
            override fun onFuncCall(): List<FileItem> {
                val file = File(path)
                return if (file.exists() && file.isDirectory && file.canRead()) {
                    val list = file.listFiles().asList()
                    Collections.sort(list) { o1, o2 ->
                        when {
                            (o1.isDirectory && o2.isDirectory) || (o1.isFile && o2.isFile) -> o1.name.toLowerCase().compareTo(o2.name.toLowerCase())
                            o2.isDirectory -> 1
                            o1.isDirectory -> -1
                            else -> o1.name.toLowerCase().compareTo(o2.name.toLowerCase())
                        }
                    }

                    val items = mutableListOf<FileItem>()
                    val fileList: List<File>

                    if (showHideFile) {
                        fileList = list
                    } else {
                        fileList = list.filter {
                            !it.isHidden
                        }
                    }
                    fileList.mapTo(items) { FileItem(it, Ok.ImageType.of(Ok.ImageTypeUtil.getImageType(it))) }
                    items
                } else {
                    emptyList()
                }
            }

        }, object : RSubscriber<List<FileItem>>() {
            override fun onSucceed(bean: List<FileItem>) {
                super.onSucceed(bean)
                onFileList.invoke(bean)
            }
        })
    }

    /**获取上一层路径*/
    private fun getPrePath(): String = targetPath.substring(0, targetPath.lastIndexOf("/"))

    private var scrollView: HorizontalScrollView? = null

    private var selectorItemView: RLinearLayout? = null

    override fun initDialogContentView() {
        super.initDialogContentView()
        mViewHolder.tv(R.id.current_file_path_view).text = targetPath
        mViewHolder.view(R.id.base_selector_button).isEnabled = false

        scrollView = mViewHolder.v(R.id.current_file_path_layout)

        /*上一个路径*/
        mViewHolder.click(R.id.current_file_path_layout) {
            resetPath(getPrePath())
        }
        //选择按钮
        mViewHolder.click(R.id.base_selector_button) {
            //T_.show(selectorFilePath)
            finishDialog {
                onFileSelector?.invoke(File(selectorFilePath))
            }
        }

        mViewHolder.reV(R.id.base_recycler_view).apply {
            adapter = object : RBaseAdapter<FileItem>(mActivity) {
                override fun getItemLayoutId(viewType: Int): Int = R.layout.base_dialog_file_selector_item

                override fun onBindView(holder: RBaseViewHolder, position: Int, item: FileItem) {
                    val bean = item.file
                    holder.tv(R.id.base_name_view).text = bean.name
                    holder.tv(R.id.base_time_view).text = formatTime(bean.lastModified())

                    //权限信息
                    holder.tv(R.id.base_auth_view).text = "${if (bean.isDirectory) "d" else "-"}${if (bean.canExecute()) "e" else "-"}${if (bean.canRead()) "r" else "-"}${if (bean.canWrite()) "w" else "-"}"

                    //文件/文件夹 提示信息
                    when {
                        item.imageType != Ok.ImageType.UNKNOWN -> {
                            holder.glideImgV(R.id.base_image_view).apply {
                                reset()
                                url = bean.absolutePath
                            }
                        }
                        bean.isDirectory -> {
                            holder.glideImgV(R.id.base_image_view).apply {
                                reset()
                                setImageResource(R.drawable.base_floder_32)
                            }
                            holder.tv(R.id.base_tip_view).text = "${bean.listFiles().size} 项"
                        }
                        bean.isFile -> {
                            holder.glideImgV(R.id.base_image_view).apply {
                                reset()
                                setImageResource(R.drawable.base_file_32)
                            }
                            holder.tv(R.id.base_tip_view).text = formatFileSize(bean.length())
                        }
                        else -> {
                            holder.glideImgV(R.id.base_image_view).apply {
                                reset()
                            }
                            holder.tv(R.id.base_tip_view).text = "unknown"
                        }
                    }

                    fun selectorItemView(itemView: RLinearLayout, selector: Boolean) {
                        if (selector) {
                            itemView.setRBackgroundDrawable(SkinHelper.getSkin().getThemeTranColor(0x80))
                        } else {
                            itemView.setRBackgroundDrawable(Color.TRANSPARENT)
                        }
                    }

                    val itemView: RLinearLayout = holder.itemView as RLinearLayout
                    selectorItemView(itemView, TextUtils.equals(selectorFilePath, bean.absolutePath))

                    //item 点击事件
                    holder.clickItem {
                        if (bean.isDirectory) {
                            resetPath(bean.absolutePath)
                        } else if (bean.isFile) {
                            setSelectorFilePath(bean.absolutePath)

                            selectorItemView?.let {
                                selectorItemView(it, false)
                            }

                            selectorItemView = itemView
                            selectorItemView(itemView, true)
                        }
                    }

                    if (L.LOG_DEBUG) {
                        if (bean.isFile) {
                            itemView.setOnLongClickListener {
                                val file = File(bean.absolutePath)
                                UIBottomItemDialog.build()
                                        .addItem("打开") {
                                            RUtils.openFile(mActivity, file)
                                        }
                                        .addItem("删除") {
                                            FileUtils.deleteFile(file)
                                            resetPath(file.path)
                                            setSelectorFilePath("")
                                        }
                                        .addItem("分享") {
                                            RUtils.shareFile(mActivity, bean.absolutePath)
                                        }
                                        .showDialog(mParentILayout)
                                false
                            }
                        }
                    }
                }
            }
            resetPath(targetPath)
        }
    }

    private fun setSelectorFilePath(path: String) {
        selectorFilePath = path
        mViewHolder.view(R.id.base_selector_button).isEnabled = File(selectorFilePath).exists()
    }

    private fun resetPath(path: String) {
        //L.e("call: resetPath -> $path")
        targetPath = path
        //mViewHolder.view(R.id.base_selector_button).isEnabled = false
        mViewHolder.tv(R.id.current_file_path_view).text = targetPath

        scrollView?.let {
            post {
                it.scrollTo((it.getChildAt(0).measuredWidth - it.measuredWidth).minValue(0), 0)
            }
        }

        mViewHolder.reV(R.id.base_recycler_view).adapterRaw.setShowState(IShowState.NORMAL)
        getFileList(targetPath) {
            mViewHolder.reV(R.id.base_recycler_view).adapterRaw.resetData(it)
            if (it.isEmpty()) {
                mViewHolder.reV(R.id.base_recycler_view).adapterRaw.setShowState(IShowState.EMPTY)
            }
        }

    }
}

data class FileItem(val file: File, val imageType: Ok.ImageType)
