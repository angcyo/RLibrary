package com.angcyo.uiview.github.pickerview;

import android.content.res.AssetManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.angcyo.github.pickerview.bean.JsonBean;
import com.angcyo.github.pickerview.view.WheelOptions;
import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.base.UIIDialogImpl;
import com.angcyo.uiview.net.RException;
import com.angcyo.uiview.net.RSubscriber;
import com.angcyo.uiview.net.Rx;
import com.angcyo.uiview.utils.T_;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：城市选择对话框
 * 创建人员：Robi
 * 创建时间：2017/02/21 18:19
 * 修改人员：Robi
 * 修改时间：2017/02/21 18:19
 * 修改备注：
 * Version: 1.0.0
 */
public class CityDialog extends UIIDialogImpl {

    private static ArrayList<JsonBean> options1Items = new ArrayList<>();
    private static ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
    private static ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();
    CityListener mCityListener;
    WheelOptions mWheelOptions;

    public CityDialog(CityListener cityListener) {
        mCityListener = cityListener;
    }

    /**
     * 获取城市json数据
     */
    private static String getJson(String fileName) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        AssetManager assetManager = RApplication.getApp().getAssets();
        BufferedReader bf = new BufferedReader(new InputStreamReader(
                assetManager.open(fileName)));
        String line;
        while ((line = bf.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    private static ArrayList<JsonBean> parseData(String result) throws JSONException {//Gson 解析
        ArrayList<JsonBean> detail = new ArrayList<>();
        JSONArray data = new JSONArray(result);
        Gson gson = new Gson();
        for (int i = 0; i < data.length(); i++) {
            JsonBean entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean.class);
            detail.add(entity);
        }
        return detail;
    }

    private static Observable<Boolean> initJson() {
        return Observable.create(new SyncOnSubscribe<Long, Boolean>() {
            @Override
            protected Long generateState() {
                return 1l;
            }

            @Override
            protected Long next(Long state, Observer<? super Boolean> observer) {
                if (!options1Items.isEmpty() && !options2Items.isEmpty()) {
                    observer.onNext(Boolean.TRUE);
                    observer.onCompleted();
                    return 1l;
                }

                options1Items.clear();
                options2Items.clear();
                options3Items.clear();

                /**
                 * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
                 * 关键逻辑在于循环体
                 *
                 * */
                String JsonData = null;//获取assets目录下的json文件数据
                try {
                    JsonData = getJson("province.json");
                } catch (IOException e) {
                    e.printStackTrace();
                    observer.onError(e);
                }

                ArrayList<JsonBean> jsonBean = null;//用Gson 转成实体
                try {
                    jsonBean = parseData(JsonData);
                } catch (JSONException e) {
                    e.printStackTrace();
                    observer.onError(e);
                }

                /**
                 * 添加省份数据
                 *
                 * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
                 * PickerView会通过getPickerViewText方法获取字符串显示出来。
                 */
                options1Items = jsonBean;

                for (int i = 0; i < jsonBean.size(); i++) {//遍历省份
                    ArrayList<String> CityList = new ArrayList<>();//该省的城市列表（第二级）
                    ArrayList<ArrayList<String>> Province_AreaList = new ArrayList<>();//该省的所有地区列表（第三极）

                    for (int c = 0; c < jsonBean.get(i).getCityList().size(); c++) {//遍历该省份的所有城市
                        String CityName = jsonBean.get(i).getCityList().get(c).getName();
                        CityList.add(CityName);//添加城市

                        ArrayList<String> City_AreaList = new ArrayList<>();//该城市的所有地区列表

                        //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
                        if (jsonBean.get(i).getCityList().get(c).getArea() == null
                                || jsonBean.get(i).getCityList().get(c).getArea().size() == 0) {
                            City_AreaList.add("");
                        } else {

                            for (int d = 0; d < jsonBean.get(i).getCityList().get(c).getArea().size(); d++) {//该城市对应地区所有数据
                                String AreaName = jsonBean.get(i).getCityList().get(c).getArea().get(d);

                                City_AreaList.add(AreaName);//添加该城市所有地区数据
                            }
                        }
                        Province_AreaList.add(City_AreaList);//添加该省所有地区数据
                    }

                    /**
                     * 添加城市数据
                     */
                    options2Items.add(CityList);

                    /**
                     * 添加地区数据
                     */
                    options3Items.add(Province_AreaList);
                }

                observer.onNext(Boolean.TRUE);

                observer.onCompleted();

                return 1l;
            }
        });
    }

    @Override
    protected View inflateDialogView(FrameLayout dialogRootLayout, LayoutInflater inflater) {
        return inflate(R.layout.pickerview_options);
    }

    @Override
    protected void initDialogContentView() {
        super.initDialogContentView();
        mWheelOptions = new WheelOptions(mViewHolder.v(R.id.optionspicker), true /*是否联动*/);

        mViewHolder.v(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] items = mWheelOptions.getCurrentItems();
                mCityListener.onCitySelector(
                        options1Items.get(items[0]).getName(),
                        options2Items.get(items[0]).get(items[1]),
                        options3Items.get(items[0]).get(items[1]).get(items[2])
                );
                finishDialog();
            }
        });
        mViewHolder.v(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishDialog();
            }
        });

        //取消循环
        mWheelOptions.setCyclic(false);

        initJson().compose(Rx.<Boolean>transformer())
                .subscribe(new RSubscriber<Boolean>() {
                    @Override
                    public void onSucceed(Boolean bean) {
                        super.onSucceed(bean);
                        if (bean) {
                            mWheelOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
                        }
                    }

                    @Override
                    public void onEnd(boolean isError, boolean isNoNetwork, RException e) {
                        super.onEnd(isError, isNoNetwork, e);
                        if (isError) {
                            T_.ok("数据异常,请重试.");
                        }
                    }
                });
    }

    public interface CityListener {
        void onCitySelector(String province/*省*/, String city /*市*/, String district /*区域*/);
    }
}
