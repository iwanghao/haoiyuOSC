package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.XWalkUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;

public class UpdateInitDialog extends BaseDialog {

    private OnListener listener;
    private  String  url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    //设置版本内容
    public void setVersion(String version) {
        TextView downText = findViewById(R.id.updateText);
        downText.setText("已有新版本: haoiyu "+version);
    }
    //设置更新内容
    public void setContent(String content) {
        TextView contentText = findViewById(R.id.updateContent);
        contentText.setText(content);
    }
    public UpdateInitDialog(@NonNull @NotNull Context context) {
        super(context);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        setContentView(R.layout.dialog_update);
        TextView updateDown = findViewById(R.id.updateDown);
        updateDown.setOnClickListener(new View.OnClickListener() {
            private void setTextEnable(boolean enable) {
                updateDown.setEnabled(enable);
                updateDown.setTextColor(enable ? Color.BLACK : Color.GRAY);
            }
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                setTextEnable(false);
                OkGo.<File>get(url).execute(new FileCallback(context.getCacheDir().getAbsolutePath(),"1.apk") {
                    @Override
                    public void onSuccess(Response<File> response) {
                        try {
                            XWalkUtils.installAPK(context, response.body().getAbsolutePath());
                            updateDown.setText("重新下载");
                            if (listener != null)
                                listener.onchange();
                            dismiss();
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            setTextEnable(true);
                        }
                    }
                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        Toast.makeText(context, response.getException().getMessage(), Toast.LENGTH_LONG).show();
                        setTextEnable(true);
                    }
                    @Override
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        updateDown.setText(String.format("%.2f%%", progress.fraction * 100));
                    }
                });
            }
        });
    }

    public UpdateInitDialog setOnListener(UpdateInitDialog.OnListener listener) {
        this.listener = listener;
        return this;
    }
    public interface OnListener {
        void onchange();
    }
}
