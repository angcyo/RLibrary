package com.angcyo.uiview.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.container.ILayout;
import com.angcyo.uiview.container.RSwipeLayout;
import com.angcyo.uiview.container.UILayoutImpl;
import com.angcyo.uiview.container.UIParam;
import com.angcyo.uiview.iview.PermissionDeniedUIView;
import com.angcyo.uiview.view.IView;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by angcyo on 2016-11-12.
 */

public abstract class UILayoutActivity extends StyleActivity {

    protected UILayoutImpl mLayout;
    protected RxPermissions mRxPermissions;
    protected RSwipeLayout mSwipeLayout;
    private boolean destroyed = false;

    @Override
    protected void onCreateView() {
        View rootView = initUILayout();
//        if (isEnableSwipeMenu()) {
//            mSwipeLayout = new RSwipeLayout(this, null);
//            mSwipeLayout.setDragDirection(getSwipeDirection());
//
//            View menuView = new View(this);
//            menuView.setBackgroundColor(Color.RED);
//
//            mSwipeLayout.setMenuView(menuView);
//            mSwipeLayout.setContentView(mLayout.getLayout());
//            setContentView(mSwipeLayout);
//        } else {
//            setContentView(mLayout.getLayout());
//        }

        setContentView(rootView);
        onAfterCreateView();
    }

    protected View initUILayout() {
        mLayout = new UILayoutImpl(this);
        mLayout.setMainLayout(true);
        return mLayout.getLayout();
    }

    protected void onAfterCreateView() {
        initScreenOrientation();

        onLoadView(getIntent());

        //checkPermissions();
    }

    /**
     * 初始化屏幕方向
     */
    protected void initScreenOrientation() {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        setRequestedOrientation(getResources().getConfiguration().orientation);//采用系统的布局方向
    }

    protected void checkPermissions() {
        checkPermissionsResult(needPermissions(), new Action1<String>() {
            @Override
            public void call(String string) {
//                onPermissionDenied(string);
                if (string.contains("0")) {
                    //有权限被拒绝
                    onPermissionDenied(string);
                } else {
                    //所有权限通过
                    onLoadViewAfterPermission(getIntent());
                }
            }
        });
    }

    public void checkPermissionsResult(String[] permissions, final Action1<String> onResult) {
        if (mRxPermissions == null) {
            mRxPermissions = new RxPermissions(this);
        }
        mRxPermissions.requestEach(permissions)
                .map(new Func1<Permission, String>() {
                    @Override
                    public String call(Permission permission) {
                        if (permission.granted) {
                            return permission.name + "1";
                        }
                        return permission.name + "0";
                    }
                })
                .scan(new Func2<String, String, String>() {
                    @Override
                    public String call(String s, String s2) {
                        return s + ":" + s2;
                    }
                })
                .last()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        L.e("\n" + UILayoutActivity.this.getClass().getSimpleName() + " 权限状态-->\n"
                                + s.replaceAll("1", " 允许").replaceAll("0", " 拒绝").replaceAll(":", "\n"));
                        onResult.call(s);
                    }
                });
//                .subscribe(new Action1<Permission>() {
//                    @Override
//                    public void call(Permission permission) {
//                        if (permission.granted) {
//                            T.show(UILayoutActivity.this, "权限允许");
//                        } else {
//                            notifyAppDetailView();
//                            T.show(UILayoutActivity.this, "权限被拒绝");
//                        }
//                    }
//                });
    }

    public void checkPermissions(String[] permissions, final Action1<Boolean> onResult) {
        checkPermissionsResult(permissions, new Action1<String>() {
            @Override
            public void call(String s) {
                if (s.contains("0")) {
                    //有权限被拒绝
                    onResult.call(false);
                } else {
                    //所欲权限通过
                    onResult.call(true);
                }
            }
        });
    }

    /**
     * 权限通过后回调
     */
    protected void onLoadViewAfterPermission(Intent intent) {

    }

    /**
     * 权限拒绝后回调
     */
    protected void onPermissionDenied(String permission) {
        startIView(new PermissionDeniedUIView(
                        permission.replaceAll("1", "").replaceAll("0", "")),
                false);
//        finishSelf();
        //notifyAppDetailView();
//        T_.show("必要的权限被拒绝!");
    }

    protected String[] needPermissions() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.CAMERA,
//                Manifest.permission.READ_CONTACTS
        };
    }

    public void finishSelf() {
        //finish();
        onUIBackPressed();
        //overridePendingTransition(R.anim.base_tran_to_top, R.anim.base_tran_to_bottom);
    }

    protected abstract void onLoadView(Intent intent);

    /**
     * 当Activity重新装载的时候调用
     */
    protected void onReLoadView(Intent intent) {
        L.d("call: onReLoadView([intent])-> " + intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onReLoadView(intent);
    }

    @Override
    public void onBackPressed() {
        if (mLayout.requestBackPressed()) {
            onUIBackPressed();
        }
    }

    protected void onUIBackPressed() {
        super.onBackPressed();
    }

    protected void moveTaskToBack() {
        super.moveTaskToBack(true);
    }

    public void startIView(final IView iView, UIParam uiParam) {
        mLayout.startIView(iView, uiParam);
    }

    public void startIView(final IView iView, boolean needAnim) {
        mLayout.startIView(iView, new UIParam(needAnim));
    }

    public void startIView(IView iView) {
        startIView(iView, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLayout.onActivityResult(requestCode, resultCode, data);
    }

    public boolean isDestroyedCompatible() {
        if (Build.VERSION.SDK_INT >= 17) {
            return isDestroyedCompatible17();
        } else {
            return destroyed || super.isFinishing();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;
    }

    @TargetApi(17)
    private boolean isDestroyedCompatible17() {
        return super.isDestroyed();
    }

    protected void showKeyboard(boolean isShow) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isShow) {
            if (getCurrentFocus() == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                imm.showSoftInput(getCurrentFocus(), 0);
            }
        } else {
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public ILayout getLayout() {
        return mLayout;
    }

    public boolean isEnableSwipeMenu() {
        return false;
    }

    public int getSwipeDirection() {
        return RSwipeLayout.DRAG_LEFT;
    }
}
