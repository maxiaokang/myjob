package com.playcrab.util;

import java.util.ArrayList;

import com.soundcloud.android.crop.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class PermissionUtil {

    private static final int REQUEST_PERMISSION = 100;

    @SuppressLint("InlinedApi")
    private static String originPermission[] = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA };
    private static Activity activity;
    private static ArrayList needPermission;

    /**
     * Android 6.0权限获取 author by xiaonan
     */
    @SuppressLint("NewApi")
    public static void requestNeedPermission(Activity act) {
        activity = act;
        floatPermission();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            checkPermission(true);

        } else {
            // 6.0以下不用做权限处理
            checkPermission(false);
        }
    }

    private static void checkPermission(boolean needGrant) {
        boolean isNeedGrant = needGrant;
        if (!isNeedGrant) {
            Log.d("maxiaokang", "已经获取权限");
        } else {
            // 检查未赋值的权限
            needPermission = new ArrayList<String>();
            for (String permissionName : originPermission) {
                if (!checkIsAskPermission(permissionName)) {
                    needPermission.add(permissionName);
                }
            }
            // 处理权限
            if (needPermission.size() > 0) {
                // 弹出描述提示框
                showDialog(activity.getResources().getString(R.string.permission_dialog_title), activity.getResources().getString(R.string.permission_dialog_message), 0);
            }
        }

    }

    private static boolean checkIsAskPermission(String permission) {
        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * 
     * @param message
     * @param type
     *            0 -- 按钮获取权限 1 -- 直接关闭
     */
    private static void showDialog(String title, String message, final int type) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(message);
        dialog.setPositiveButton("確定", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type == 0) {
                    activity.requestPermissions((String[]) needPermission.toArray(new String[needPermission.size()]), REQUEST_PERMISSION);
                }
            }
        });
        dialog.create();
        dialog.show();
    }

    @SuppressLint("NewApi")
    public static void floatPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                // 启动Activity让用户授权
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, 100);
            }
        } else {
            checkPermission(false);
        }
    }
}
