package com.guangzhou.liuliang.appframework.BroadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.guangzhou.liuliang.appframework.Activity.LoadingActivity;
import com.guangzhou.liuliang.appframework.R;

import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

/**
 * Created by yunhaipiaodi on 2016/7/27.
 */
public class RongNotificationReceiver extends PushMessageReceiver {

    private int NOTIFICATION = 10007;
    @Override
    public boolean onNotificationMessageArrived(Context context, PushNotificationMessage pushNotificationMessage) {
        showNotification(context,pushNotificationMessage.getPushContent(),pushNotificationMessage.getTargetUserName());
        String msg = pushNotificationMessage.getPushContent();
        Log.d("RongReceiver","msg:" + msg);
        return true;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushNotificationMessage pushNotificationMessage) {
       Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d("RongReceiver","NotificationMessageClicked");
        Uri.Builder builder = Uri.parse("rong://" + context.getPackageName()).buildUpon();
        builder.appendPath("conversation").appendPath(pushNotificationMessage.getConversationType().getName())
                .appendQueryParameter("targetId", pushNotificationMessage.getTargetId())
                .appendQueryParameter("isNotification", "yes")
                .appendQueryParameter("title", pushNotificationMessage.getTargetUserName());
        Uri uri = builder.build();
        intent.setData(uri);
        context.startActivity(intent);
        return true;
    }

    private void showNotification(Context context,String content,String userName){
        NotificationManager mNM = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >=16)
        {
            Log.d("RongReceiver","showNotification");
            Notification notification = new Notification.Builder(context)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.icon))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle("userName")
                    .setContentText(content)
                    .setStyle(new Notification.BigTextStyle().bigText(content))
                    .setAutoCancel(true)
                    .build();
            mNM.notify(NOTIFICATION,notification);
        }
    }
}
