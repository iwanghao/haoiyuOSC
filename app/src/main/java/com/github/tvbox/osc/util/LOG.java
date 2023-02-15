package com.github.tvbox.osc.util;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.ui.dialog.UpdateInitDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

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
        OkGo.<String>post("http://app.haoiyu.cn:8081/phonesystem/addLog")
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
                                Toast.makeText(activity.getApplicationContext(), "该设备禁止使用10秒后关闭应用", Toast.LENGTH_LONG).show();
                                new Thread(() -> {
                                    SystemClock.sleep(10*1000);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                },"定时任务").start();
                            }else{
                                setUpdate(activity);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    //首页提醒是否有新版本
    public void setUpdate(Activity activity){
        Context context = activity.getApplicationContext();
        OkGo.<String>post(XWalkUtils.versionDownUrl()).execute(
                new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String String = response.body();
                            JSONObject myJsonObject = new JSONObject(String);
                            String version = myJsonObject.getJSONObject("result").getString("version");
                            if (!version.equalsIgnoreCase(XWalkUtils.version(context))) {
                                Toast.makeText(context, "发现新版本："+version, Toast.LENGTH_LONG).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return response.body().string();
                    }
                }
        );
    }
}