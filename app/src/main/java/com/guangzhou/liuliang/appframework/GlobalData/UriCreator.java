package com.guangzhou.liuliang.appframework.GlobalData;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.guangzhou.liuliang.appframework.R;

/**
 * Created by yunhaipiaodi on 2016/10/12.
 */
public class UriCreator {
    private Context context;

    public UriCreator(Context context){
        this.context = context;
    }

    //isNotify,当此uri以pendingIntent传递给Notification时为true,否则为false；

    public Uri getSystemNoticeActivityUri(Boolean isNotify){
        Uri uri = new Uri.Builder().scheme("circle")
                .authority(context.getPackageName())
                .appendPath("push_notice")
                .appendQueryParameter("isNotify",isNotify?"true":"false")
                .appendQueryParameter("title",context.getResources().getString(R.string.app_name))
                .build();
        return uri;
    }

    public Uri getPublishListActivityUri(Boolean isNotify){
        Uri uri = new Uri.Builder().scheme("circle")
                .authority(context.getPackageName())
                .appendPath("publish_list")
                .appendQueryParameter("isNotify",isNotify?"true":"false")
                .appendQueryParameter("title","你关注的")
                .build();
        return uri;
    }

    public Uri getAppIconUri(){
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"
                +context.getResources().getResourcePackageName(R.mipmap.icon)+"/"
                +context.getResources().getResourceTypeName(R.mipmap.icon)+"/"
                +context.getResources().getResourceEntryName(R.mipmap.icon)
        );
        return uri;
    }

    public Uri getRongYunPrivateUri(String targetId,String sendUserName,Boolean isNotify){
        String query = "targetId="+ targetId + "&title="+ sendUserName;
        String scheme = "rong";
        String packageName = context.getPackageName();
        Uri uri = new Uri.Builder().scheme(scheme)
                .authority(packageName)
                .path("conversation/private")
                .appendQueryParameter("targetId",targetId)
                .appendQueryParameter("title",sendUserName)
                .appendQueryParameter("isNotify",isNotify?"true":"false")
                .build();
        return uri;
    }

}
