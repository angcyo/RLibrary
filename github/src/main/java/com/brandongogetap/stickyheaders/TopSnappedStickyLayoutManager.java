package com.brandongogetap.stickyheaders;

import android.content.Context;

import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;

public final class TopSnappedStickyLayoutManager extends StickyLayoutManager {

    TopSnappedStickyLayoutManager(Context context, StickyHeaderHandler headerHandler) {
        super(context, headerHandler);
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPositionWithOffset(position, 0);
    }
}
