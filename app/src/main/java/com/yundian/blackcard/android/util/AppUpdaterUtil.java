package com.yundian.blackcard.android.util;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.qiangxi.checkupdatelibrary.dialog.ForceUpdateDialog;
import com.qiangxi.checkupdatelibrary.dialog.UpdateDialog;
import com.yundian.blackcard.android.R;
import com.yundian.blackcard.android.model.UpdateInfo;
import com.yundian.blackcard.android.networkapi.NetworkAPIFactory;
import com.yundian.comm.networkapi.listener.OnAPIListener;
import com.yundian.comm.util.DeviceUtils;
import com.yundian.comm.util.ToastUtils;

import java.io.File;

import static com.qiangxi.checkupdatelibrary.dialog.ForceUpdateDialog.FORCE_UPDATE_DIALOG_PERMISSION_REQUEST_CODE;
import static com.qiangxi.checkupdatelibrary.dialog.UpdateDialog.UPDATE_DIALOG_PERMISSION_REQUEST_CODE;

/**
 * Created by yaowang on 2017/7/10.
 */

public class AppUpdaterUtil {

    private static ForceUpdateDialog forceupdatedialog;
    private static UpdateDialog updateDialog;

    public static void checkAppVersion(final Context context) {

        Integer appVersionCode = Integer.parseInt(DeviceUtils.getVersionCode(context));
        NetworkAPIFactory.getCommService().checkAppVersion(appVersionCode, new OnAPIListener<UpdateInfo>() {
            @Override
            public void onError(Throwable ex) {
                ex.printStackTrace();
            }

            @Override
            public void onSuccess(UpdateInfo updateInfo) {
                if (updateInfo != null && updateInfo.getIsUpdate() == 1) {
                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!path.exists()) {
                        path.mkdirs();
                    }
                    String fileName = context.getPackageName() + "_" + updateInfo.getVersion() + ".apk";
                    if (updateInfo.getIsForce() == 1) {
                        forceupdatedialog = new ForceUpdateDialog(context);
                        forceupdatedialog.setDownloadUrl(updateInfo.getUrl())
                                .setTitle(context.getString(R.string.app_name) + "有更新啦")
                                .setVersionName(updateInfo.getVersion())
                                .setUpdateDesc(updateInfo.getDescription())
                                .setFileName(fileName)
                                .setFilePath(path.getAbsolutePath())
                                .show();
                    } else {
                        updateDialog = new UpdateDialog(context);
                        updateDialog.setTitle(context.getString(R.string.app_name) + "有更新啦")
                                .setDownloadUrl(updateInfo.getUrl())
                                .setVersionName(updateInfo.getVersion())
                                .setUpdateDesc(updateInfo.getDescription())
                                .setFileName(fileName)
                                .setFilePath(path.getAbsolutePath())
                                .setShowProgress(true)
                                .setIconResId(R.mipmap.ic_launcher)
                                .setAppName(context.getString(R.string.app_name))
                                .show();
                    }

                }
            }
        });
    }


    public static void onRequestPermissionsResult(final Context context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == UPDATE_DIALOG_PERMISSION_REQUEST_CODE) {
                updateDialog.download();
            } else if (requestCode == FORCE_UPDATE_DIALOG_PERMISSION_REQUEST_CODE) {
                forceupdatedialog.download();
            }
        } else {
            ToastUtils.show(context, "请开启读写sd卡权限,不然无法正常升级");
        }
    }
}
