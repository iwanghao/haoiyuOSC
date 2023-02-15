package com.github.tvbox.osc.util;

import android.app.Activity;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class LOG{
    private static String TAG = "haoiyu";

    public static void e(String msg) {
        Log.e(TAG, "" + msg);
    }

    public static void i(String msg) {
        Log.i(TAG, "" + msg);
    }
    //设置日志
    public  void setLog(Activity activity,ApiConfig.LoadConfigCallback callback, String name){
        String version = XWalkUtils.version(activity);
        OkGo.<String>post("http://192.168.2.42:8081/phonesystem/addLog")
                .params("devicebrand",SystemUtil.getDeviceBrand())
                .params("systemmodel",SystemUtil.getSystemModel())
                .params("systemlanguage",SystemUtil.getSystemLanguage())
                .params("systemversion",SystemUtil.getSystemVersion())
                .params("imei",SystemUtil.getIMEI(activity))
                .params("version",version)
                .params("name",name)
                .isSpliceUrl(true)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String json = response.body();
                            JsonObject infoJson = new Gson().fromJson(json, JsonObject.class);
                            int code = infoJson.get("code").getAsInt();
                            if(code != 2000){
                                Toast.makeText(activity.getApplicationContext(), "该设备禁止使用10秒后关闭应用", Toast.LENGTH_SHORT).show();
                                new Thread(() -> {
                                    SystemClock.sleep(10*1000);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                },"定时任务").start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

    }
}