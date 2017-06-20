package com.guangzhou.liuliang.appframework.VersionUpdate;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

/**
 * Created by yunhaipiaodi on 2016/6/13.
 */
public class DownLoadBroadcastReceiver  extends BroadcastReceiver {

    @SuppressLint("NewApi")

    public void onReceive(Context context, Intent intent) {

        long myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        SharedPreferences sPreferences = context.getSharedPreferences("downloadplato", 0);

        long refernece = sPreferences.getLong("plato", 0);

        if (refernece == myDwonloadID) {

            String serviceString = Context.DOWNLOAD_SERVICE;

            DownloadManager dManager = (DownloadManager) context.getSystemService(serviceString);

            Intent install = new Intent(Intent.ACTION_VIEW);

            Uri downloadFileUri = dManager.getUriForDownloadedFile(myDwonloadID);

            install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");

            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(install);
        }

    }

}
