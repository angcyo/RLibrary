package com.angcyo.uiview.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.base.UIIDialogImpl
import com.angcyo.uiview.recycler.RRecyclerView
import com.angcyo.uiview.recycler.adapter.RExBaseAdapter
import com.angcyo.uiview.rsen.RefreshLayout

/**
 * Created by angcyo on 2017-07-23.
 */
abstract class UIRecyclerDialog<H, T, F> : UIIDialogImpl() {
    lateinit var refreshLayout: RefreshLayout
    lateinit var recyclerView: RRecyclerView
    lateinit var adapter: RExBaseAdapter<H, T, F>

    override fun inflateDialogView(dialogRootLayout: FrameLayout?, inflater: LayoutInflater?): View {
        return inflate(R.layout.base_recycler_dialog_layout)
    }

    override fun initDialogContentView() {
        super.initDialogContentView()
        refreshLayout = mViewHolder.v(R.id.base_refresh_view)
        recyclerView = mViewHolder.v(R.id.base_recycler_view)

        adapter = createAdapter()
        initRefreshLayout()
        initRecyclerView()
    }

    abstract fun createAdapter(): RExBaseAdapter<H, T, F>

    open fun initRefreshLayout() {
        refreshLayout.setNotifyListener(false)
        refreshLayout.setPlaceholderView()
    }

    open fun initRecyclerView() {
        recyclerView.adapter = adapter
    }
}
