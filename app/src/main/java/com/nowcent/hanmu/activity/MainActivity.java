package com.nowcent.hanmu.activity;


import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.nowcent.hanmu.R;
import com.nowcent.hanmu.pojo.User;
import com.nowcent.hanmu.utils.DataUtils;
import com.nowcent.hanmu.utils.SpiderUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;


@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    /**
     * 当前用户
     */
    private User user = new User();

    /**
     * 组件
     */

    @ViewById(R.id.editText)
    EditText editText;

    @AfterViews
    void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        String imei = DataUtils.getImei(this);
        if(imei != null){
            user.setIemiCode(imei);
            editText.setText(imei);
        }
    }

    @Click(R.id.submit_btn)
    void click(){
        user.setIemiCode(editText.getText().toString());
        run();
    }

    @Background
    void run(){
        String imeiCode = editText.getText().toString();
        if(imeiCode.length() != 32){
            showDialog("IMEI码错误", "请重新爬取IMEI码", "好", null);
            return;
        }

        JSONObject json;
        try {
            json = SpiderUtils.login(user.getIemiCode());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Log.e("login", json.toString());
        if(!json.getBoolean("Success")){
            showDialog("登录失败", "请重试", "好", null);
            return;
        }

        user.setIemiCode(json.getJSONObject("Data").getString("IMEICode"));
        user.setToken(json.getJSONObject("Data").getString("Token"));
        getUserInfo();
    }

    @Background
    void getUserInfo(){
        JSONObject json = null;
        try {
            json = SpiderUtils.getUserInfo(user.getToken());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Log.e("getUserInfo", json.toString());
        if(!json.getBoolean("Success")){
            showDialog("获取用户信息失败", "请重试", "好", null);
            return;
        }

        user.setMinSpeed(json.getJSONObject("Data").getJSONObject("SchoolRun").getDouble("MinSpeed"));
        user.setMaxSpeed(json.getJSONObject("Data").getJSONObject("SchoolRun").getDouble("MaxSpeed"));
        user.setDistance(json.getJSONObject("Data").getJSONObject("SchoolRun").getInteger("Lengths"));
        getRunId();
    }

    @Background
    void getRunId(){
        String runId = null;
        try {
            runId = SpiderUtils.getRunId(user.getToken(), user.getDistance());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if(runId == null){
            showDialog("获取RunId失败", "请重试", "好", null);
            return;
        }

        Log.e("RunId", runId);
        user.setRunId(runId);
        postFinishRunning();
    }

    @Background
    void postFinishRunning(){
        JSONObject json = null;
        try {
            json = SpiderUtils.postFinishRunning(user);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("finish", json.toString());

        if(json == null || !json.getBoolean("Success")){
            showDialog("跑步失败", "请重试", "好", null);
            return;
        }

        DataUtils.saveImei(this, user.getIemiCode());
        showDialog("执行成功", null, "好", null);
    }

    @UiThread
    void showDialog(String title, String text, String positiveButtonText, DialogInterface.OnClickListener positiveListener){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, positiveListener)
                .setCancelable(false)
                .show();

    }




}