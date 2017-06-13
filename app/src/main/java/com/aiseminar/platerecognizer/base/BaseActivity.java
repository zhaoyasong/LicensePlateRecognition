package com.aiseminar.platerecognizer.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aiseminar.platerecognizer.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 创建所有Activity的继承类
 * <p>
 * created by song on 2017-06-12.9:45
 */
public class BaseActivity extends AppCompatActivity {
    SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置Activity切换的动画
        overridePendingTransition(R.anim.slide_left_in, R.anim.hold);

    }

    /**
     * 显示提示框
     */
    public void showDialog() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("加载中");
        pDialog.show();
        //设置提示框显示
        pDialog.setCancelable(false);
    }


    /**
     * 显示提示框的主题
     *
     * @param c
     */
    public void showDialogWithTitle(String c) {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(c);
        pDialog.show();
        pDialog.setCancelable(false);

    }


    /**
     * 关闭提示框
     */
    public void dimissDialog() {
        pDialog.dismissWithAnimation();
    }


    public void getTopBar(String arg0) {


    }


    /**
     * Activity退出的时候调用的方法
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_right_out);
    }
}
