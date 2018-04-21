package com.group4.im_cs656;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;

public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        AndPermission.with(this).permission(
                Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action() {
            @Override
            public void onAction(List<String> permissions) {
                //Log.i("lx", "申请权限成功 ");
                startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
                //Log.i("lx", "跳转 ");
                finish();
            }
        })
                .onDenied(new Action() {
            @Override
            public void onAction(List<String> permissions) {
                Log.i("lx", "被拒绝");
            }
        }).start();
    }

}
