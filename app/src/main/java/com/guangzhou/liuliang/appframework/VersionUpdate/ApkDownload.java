package com.guangzhou.liuliang.appframework.VersionUpdate;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;

import java.util.List;

/**
 * Created by yunhaipiaodi on 2016/6/13.
 */
public class ApkDownload {
    private Context context;

    //下载地址
    private String url;

    public ApkDownload(Context context,String url){
        this.context = context;
        this.url = url;
    }


    //下载文件存放路径,不设置则存放在系统默认下载文件夹
    private String file_path = "";
    public void setFilePath(String file_path){
        this.file_path = file_path;
    }

    //下载
    public void startDownload(){

        // uri 是你的下载地址，可以使用Uri.parse("http://")包装成Uri对象
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));

        // 通过setAllowedNetworkTypes方法可以设置允许在何种网络下下载，
        // 也可以使用setAllowedOverRoaming方法，它更加灵活
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

        // 此方法表示在下载过程中通知栏会一直显示该下载，在下载完成后仍然会显示，
        // 直到用户点击该通知或者消除该通知。还有其他参数可供选择
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 设置下载文件存放的路径，同样你可以选择以下方法存放在你想要的位置。
        // setDestinationUri
        // setDestinationInExternalPublicDir
        if(file_path.isEmpty()){
            req.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, getFileName(url));
        }else{
            req.setDestinationInExternalFilesDir(context, file_path, getFileName(url));
        }


        // 设置一些基本显示信息
        req.setTitle("圈子");
        req.setDescription("新版本下载中...");
        req.setMimeType("application/vnd.android.package-archive");

        // 设置为可被媒体扫描器找到

        req.allowScanningByMediaScanner();

        // 设置为可见和可管理
        DownloadManager dm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);

        req.setVisibleInDownloadsUi(true);

        long refernece = dm.enqueue(req);

        // 把当前下载的ID保存起来
        SharedPreferences sPreferences = context.getSharedPreferences("downloadplato", 0);

        sPreferences.edit().putLong("plato", refernece).commit();
    }

    private String getFileName(String url){
        String fileName = "";
        String [] strArray = url.split("/");
        fileName = strArray[strArray.length - 1];
        return fileName;
    }
}
