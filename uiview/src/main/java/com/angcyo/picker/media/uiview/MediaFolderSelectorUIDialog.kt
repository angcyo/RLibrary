package com.angcyo.picker.media.uiview

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.angcyo.picker.media.bean.MediaFolder
import com.angcyo.picker.media.bean.MediaLoaderConfig
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIIDialogImpl
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.adapter.RBaseAdapter

/**
 * Created by angcyo on 2018/06/17 08:39
 */
class MediaFolderSelectorUIDialog(val mediaFolderList: List<MediaFolder>, val curPosition: Int, val observer: (Int) -> Unit) : UIIDialogImpl() {
    override fun inflateDialogView(dialogRootLayout: FrameLayout, inflater: LayoutInflater): View {
        return inflate(R.layout.dialog_media_folder_selector_layout)
    }

    override fun initDialogContentView() {
        super.initDialogContentView()

        rv(R.id.base_recycler_view).adapter = FolderAdapter()
    }

    override fun isDimBehind(): Boolean {
        return false
    }

    inner class FolderAdapter : RBaseAdapter<MediaFolder>(mActivity, mediaFolderList) {
        override fun onBindView(holder: RBaseViewHolder, position: Int, bean: MediaFolder?) {
            bean?.let {

                //封面加载
                holder.giv(R.id.base_image_view).apply {
                    BaseMediaUIView.loadImageView(this, bean.mediaItemList.first())

                    //视频 / 音频 特殊提示
                    if (bean.mediaFolderType == MediaLoaderConfig.LOADER_TYPE_VIDEO) {
                        setPlayDrawable(getDrawable(R.drawable.base_play_png))
                    } else {
                        setPlayDrawable(null)
                    }
                }

                //文件夹名
                holder.tv(R.id.base_name_view).text = bean.folderName

                //是否是当前文件夹
                holder.visible(R.id.base_radio_button, curPosition == position)

                //个数提示信息
                when {
                    bean.mediaFolderType == MediaLoaderConfig.LOADER_TYPE_ALL -> {
                        holder.visible(R.id.base_tip_view)
                        holder.tv(R.id.base_tip_view).text = "${bean.mediaCount}个"
                    }
                    bean.mediaFolderType == MediaLoaderConfig.LOADER_TYPE_IMAGE_VIDEO -> {
                        holder.visible(R.id.base_tip_view)
                        holder.tv(R.id.base_tip_view).text = "${bean.mediaCount}个"
                    }
                    bean.mediaFolderType == MediaLoaderConfig.LOADER_TYPE_VIDEO -> {
                        holder.visible(R.id.base_tip_view)
                        holder.tv(R.id.base_tip_view).text = "${bean.mediaCount}个"
                    }
                    bean.mediaFolderType == MediaLoaderConfig.LOADER_TYPE_IMAGE -> {
                        holder.visible(R.id.base_tip_view)
                        holder.tv(R.id.base_tip_view).text = "${bean.mediaCount}张"
                    }
                    bean.mediaFolderType == MediaLoaderConfig.LOADER_TYPE_AUDIO -> {
                        holder.visible(R.id.base_tip_view)
                        holder.tv(R.id.base_tip_view).text = "${bean.mediaCount}个"
                    }
                }

                //点击事件
                holder.clickItem {
                    finishDialog {
                        if (curPosition == position) {
                        } else {
                            observer.invoke(position)
                        }
                    }
                }
            }
        }

        override fun getItemLayoutId(viewType: Int): Int {
            return R.layout.base_item_media_folder_layout
        }

    }
}