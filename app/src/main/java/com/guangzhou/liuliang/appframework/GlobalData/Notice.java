package com.guangzhou.liuliang.appframework.GlobalData;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.guangzhou.liuliang.appframework.Activity.SystemNoticeActivity;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemData;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemDataArray;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by yunhaipiaodi on 2016/10/11.
 */
public class Notice {
    private static Notice instance;

    private EventBus eventBus ;

    int NOTIFICATION_SYSTEM = 10006;
    int NOTIFICATION_SUBSCRIBE = 10007;
    int NOTIFICATION_RONGYUN = 10008;

    public static Notice getInstance(){
        if(instance == null){
            instance = new Notice();
        }
        return instance;
    }

    public Notice(){
        eventBus = EventBus.getDefault();
    }

    public void sendMessage(@NonNull Context context, int messageType,
                            int messageId,String senderUserName,int RongYunTargetId,int senderUserId,
                            Uri targetUri,Uri avatarUri,String sendContent,MessageEvent event){
        //往消息数组集合里添加本条信息
        addMsgToNoticeArray(messageType,messageId,sendContent,avatarUri,senderUserName,RongYunTargetId,senderUserId);

        if(isAppOnForeground(context)){         //在前端，发送Event到当前Activity;
            sendEvent(event);
        }else{                                  //在后台，推送Notification;
            int notificationFlag = 1;
            switch (messageType){
                case 1:
                    notificationFlag = NOTIFICATION_SYSTEM;
                    break;
                case 2:
                    notificationFlag = NOTIFICATION_SUBSCRIBE;
                    break;
                case 3:
                    notificationFlag = NOTIFICATION_RONGYUN;
                    break;
            }
            showNotification(context,targetUri,sendContent,notificationFlag);
        }
    }

    private void addMsgToNoticeArray( int messageType,      //消息类型。1，系统推送消息;2,订阅用户消息,3，融云即时聊天消息;
                                        int messageId,        //消息数组编号ID,融云消息主体全部为其用户ID（user_id）,系统推送消息为-1
                                        String message,        //消息文本内容
                                        Uri avatarUri,          //目标头像图片地址
                                        String name,           //发送消息主体名称，系统推送消息，名称为应用名“圈子”
                                        int targetId,         //融云消息专用，为了兼容性，系统推送消息统一为 -1，关心用户发布消息为-2；
                                        int sendUserId       //消息主体ID,融云消息主体全部为其用户ID（user_id）,系统推送消息为-1
                                                        ){
        if(NoticeItemDataArray.getInstance().MessageArray.get(messageId) != null){
            NoticeItemDataArray.getInstance().addNoticeItemMessage(messageId,message);
        }else{
            NoticeItemDataArray.getInstance().addNoticeItemData(String.valueOf(messageId),
                    new NoticeItemData(avatarUri,message,name,String.valueOf(targetId),String.valueOf(sendUserId)));
        }

        if(messageType == 1 || messageType == 2){       //系统推送消息
            DataSource.getInstance().systemNotices.add(message);
        }
    }

    private void showNotification(@NonNull Context context,Uri targetUri, String sendContent,int notificationFlag){
        PendingIntent pendingIntent;
        if(targetUri != null){                      //如果传递的uri不为空，说明该Notification应该跳转到某个Activity
            Intent intent = new Intent(null,targetUri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        }else{
            pendingIntent = null;
        }

        String appName = context.getResources().getString(R.string.app_name);  //获得APP名字
        Notification notification = new Notification.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.icon))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(appName)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(sendContent)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationFlag,notification);
    }



    private void sendEvent(@NonNull MessageEvent event){
        eventBus.post(event);
    }

    //判定当前程序在前台还是后台
    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

}
