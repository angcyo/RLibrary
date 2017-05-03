package com.angcyo.uiview.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.angcyo.uiview.R;

import java.util.Locale;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：录制语音时间显示
 * 创建人员：Robi
 * 创建时间：2017/05/03 16:26
 * 修改人员：Robi
 * 修改时间：2017/05/03 16:26
 * 修改备注：
 * Version: 1.0.0
 */
public class RecordTimeView extends RTextView {
    long mTime = 0, mMaxTime = Integer.MAX_VALUE;
    boolean isRecording = false;

    OnMaxTimeListener mOnMaxTimeListener;

    Runnable record = new Runnable() {
        @Override
        public void run() {
            mTime++;
            setTime(mTime);
            if (mTime > mMaxTime) {
                if (mOnMaxTimeListener != null) {
                    mOnMaxTimeListener.onMaxTime(mTime);
                }
            } else {
                postDelayed(record, 1000);
            }
        }
    };

    public RecordTimeView(Context context) {
        super(context);
    }

    public RecordTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setTextSize(25);
        setTime(0);
    }

    /**
     * 设置录制最大时间, 秒
     */
    public void setMaxTime(long maxTime) {
        mMaxTime = maxTime;
    }

    /**
     * 设置时间
     */
    private void setTime(long time) {
        mTime = time;

        long s = mTime % 60;
        long m = mTime / 60;

        if (isRecording) {
            setTextColor(ContextCompat.getColor(getContext(), R.color.base_dark_red));
        } else {
            setTextColor(ContextCompat.getColor(getContext(), R.color.base_text_color_dark));
        }
        setText(String.format(Locale.CHINA, "%02d:%02d", m, s));
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        startRecord(null);
    }

    public void startRecord(OnMaxTimeListener listener) {
        mOnMaxTimeListener = listener;
        if (isRecording) {
            return;
        }
        isRecording = true;
        postDelayed(record, 1000);
    }

    /**
     * 停止
     */
    public void stopRecord() {
        if (isRecording) {
            isRecording = false;
            removeCallbacks(record);
            setTime(0);
        }
    }

    public interface OnMaxTimeListener {
        void onMaxTime(long maxTime);
    }
}
