package com.angcyo.uiview.github.pickerview;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.base.UIIDialogImpl;
import com.angcyo.uiview.github.pickerview.view.WheelTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：日期选择对话框, 时间选择对话框
 * 创建人员：Robi
 * 创建时间：2017/02/21 18:19
 * 修改人员：Robi
 * 修改时间：2017/02/21 18:19
 * 修改备注：
 * Version: 1.0.0
 */
public class DateDialog extends UIIDialogImpl {

    public static DateFormat Date_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


    /**
     * 年月日 时分秒 是否显示
     */
    boolean[] mShowType = new boolean[]{true, true, true, false, false, false};

    DateConfig mDateConfig;
    private WheelTime wheelTime;

    public DateDialog(DateConfig dateConfig) {
        mDateConfig = dateConfig;
    }

    /**
     * 获取周岁(时间格式 必须是: yyyy-MM-dd)
     */
    public static int getBirthday(String date) {
        if (TextUtils.isEmpty(date)) {
            return 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);//当前那一年

        try {
            calendar.setTime(Date_FORMAT.parse(date));
            int y = calendar.get(Calendar.YEAR);//当前那一年
            return year - y;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected View inflateDialogView(RelativeLayout dialogRootLayout, LayoutInflater inflater) {
        return inflate(com.angcyo.uiview.R.layout.pickerview_time);
    }

    @Override
    protected void initDialogContentView() {
        super.initDialogContentView();
        wheelTime = new WheelTime(mViewHolder.v(R.id.timepicker), mShowType);
        mViewHolder.v(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDateConfig.onDateSelector(wheelTime);
                finishDialog();
            }
        });
        mViewHolder.v(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishDialog();
            }
        });

        wheelTime.setLabels(null, null, null, null, null, null);

        //禁止循环滚动
        setCyclic(false);

        //设置时间范围
        if (!TextUtils.isEmpty(mDateConfig.getMaxDate())) {
            Date maxData = null;
            try {
                maxData = Date_FORMAT.parse(mDateConfig.getMaxDate());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(maxData);

                Calendar startDate = Calendar.getInstance();
                startDate.set(1970, 1, 1);
                Calendar endDate = Calendar.getInstance();
                endDate.setTimeInMillis(System.currentTimeMillis());

                wheelTime.setRangDate(startDate, endDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Calendar calendar = Calendar.getInstance();
        String currentDate = mDateConfig.getCurrentDate();
        //设置当前时间
        if (!TextUtils.isEmpty(currentDate)) {
            try {
                calendar.setTime(Date_FORMAT.parse(currentDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
        }
        setTime(calendar.getTime());
    }

    /**
     * 设置选中时间
     *
     * @param date 时间
     */
    public void setTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (date == null)
            calendar.setTimeInMillis(System.currentTimeMillis());
        else
            calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        wheelTime.setPicker(year, month, day, hours, minute, second);
    }

    /**
     * 设置是否循环滚动
     *
     * @param cyclic 是否循环
     */
    public void setCyclic(boolean cyclic) {
        wheelTime.setCyclic(cyclic);
    }

    public interface DateConfig {
        void onDateSelector(WheelTime wheelTime);

        /**
         * 当前的日期, 如果空, 表示使用当前的日期
         * "yyyy-MM-dd"
         */
        String getCurrentDate();

        /**
         * 最大的日期
         * "yyyy-MM-dd"
         */
        String getMaxDate();
    }

    public static class SimpleDateConfig implements DateConfig {

        @Override
        public void onDateSelector(WheelTime wheelTime) {
            L.e("onDateSelector() -> " + wheelTime.getTime());
        }

        @Override
        public String getCurrentDate() {
            return null;
        }

        @Override
        public String getMaxDate() {
            return Date_FORMAT.format(new Date(System.currentTimeMillis()));
        }
    }

}
