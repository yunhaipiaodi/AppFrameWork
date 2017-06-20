package com.guangzhou.liuliang.appframework.RongYun;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemData;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemDataArray;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.GlobalData.Notice;
import com.guangzhou.liuliang.appframework.GlobalData.UriCreator;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.List;
import java.util.WeakHashMap;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.utils.StringUtils;
import io.rong.imkit.widget.AlterDialogFragment;
import io.rong.imkit.widget.provider.CameraInputProvider;
import io.rong.imkit.widget.provider.ImageInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.TextInputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.RealTimeLocationConstant;
import io.rong.imlib.location.message.RealTimeLocationStartMessage;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.CommandMessage;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.DiscussionNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.LocationMessage;
import io.rong.message.PublicServiceMultiRichContentMessage;
import io.rong.message.PublicServiceRichContentMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;
//import io.rong.imkit.widget.provider.CameraInputProvider;
//import io.rong.imkit.widget.provider.VoIPInputProvider;

//import io.rong.imkit.widget.provider.VoIPInputProvider;


/**
 * Created by zhjchen on 1/29/15.
 */

/**
 * 融云SDK事件监听处理。
 * 把事件统一处理，开发者可直接复制到自己的项目中去使用。
 * <p/>
 * 该类包含的监听事件有：
 * 1、消息接收器：OnReceiveMessageListener。
 * 2、发出消息接收器：OnSendMessageListener。
 * 3、用户信息提供者：GetUserInfoProvider。
 * 4、好友信息提供者：GetFriendsProvider。
 * 5、群组信息提供者：GetGroupInfoProvider。
 * 7、连接状态监听器，以获取连接相关状态：ConnectionStatusListener。
 * 8、地理位置提供者：LocationProvider。
 * 9、自定义 push 通知： OnReceivePushMessageListener。
 * 10、会话列表界面操作的监听器：ConversationListBehaviorListener。
 */
public final class RongCloudEvent implements RongIMClient.OnReceiveMessageListener{

    private static final String TAG = RongCloudEvent.class.getSimpleName();

    private static RongCloudEvent mRongCloudInstance;

    private Context mContext;

    private int RONGCLOUDMESSAGE= 1206;

    private static RequestQueue queue;

    private int NOTIFICATION = 10007;

    private static MediaPlayer mediaPlayer;
    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static void init(Context context) {

        if (mRongCloudInstance == null) {
            synchronized (RongCloudEvent.class) {
                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new RongCloudEvent(context);
                }
            }
        }

        queue = Volley.newRequestQueue(context);

        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }
    }

    private RongCloudEvent(Context context) {
        mContext = context;
        setOtherListener();
    }

    /**
     * 获取RongCloud 实例。
     *
     * @return RongCloud。
     */
    public static RongCloudEvent getInstance() {
        return mRongCloudInstance;
    }

    /**
     * RongIM.init(this) 后直接可注册的Listener。
     */


    /**
     * 连接成功注册。
     * <p/>
     * 在RongIM-connect-onSuccess后调用。
     */
    public void setOtherListener() {

        RongIM.setOnReceiveMessageListener(this);//设置消息接收监听器。

        TextInputProvider textInputProvider = new TextInputProvider(RongContext.getInstance());
        RongIM.setPrimaryInputProvider(textInputProvider);

//        扩展功能自定义
        InputProvider.ExtendProvider[] provider = {
                new ImageInputProvider(RongContext.getInstance()),//图片
//                new CameraInputProvider(RongContext.getInstance()),//相机

        };

        InputProvider.ExtendProvider[] provider1 = {
                new ImageInputProvider(RongContext.getInstance()),//图片
//                new CameraInputProvider(RongContext.getInstance()),//相机
        };

        RongIM.resetInputExtensionProvider(Conversation.ConversationType.PRIVATE, provider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.DISCUSSION, provider1);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.GROUP, provider1);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.CUSTOMER_SERVICE, provider1);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.CHATROOM, provider1);
    }


    private Bitmap getAppIcon() {
        BitmapDrawable bitmapDrawable;
        Bitmap appIcon;
        bitmapDrawable = (BitmapDrawable) RongContext.getInstance().getApplicationInfo().loadIcon(RongContext.getInstance().getPackageManager());
        appIcon = bitmapDrawable.getBitmap();
        return appIcon;
    }

    /**
     * 接收消息的监听器：OnReceiveMessageListener 的回调方法，接收到消息后执行。
     *
     * @param message 接收到的消息的实体信息。
     * @param left    剩余未拉取消息数目。
     */
    @Override
    public boolean onReceived(Message message, int left) {
        final String targetId = message.getTargetId();
        final String sendUserId = message.getSenderUserId();
        String content = "";

        MessageContent messageContent = message.getContent();
        if (messageContent instanceof TextMessage) {//文本消息
            TextMessage textMessage = (TextMessage) messageContent;
           // Log.d(TAG, "onReceived-TextMessage:" + textMessage.getContent());
            content = textMessage.getContent();
        } else if (messageContent instanceof ImageMessage) {//图片消息
            ImageMessage imageMessage = (ImageMessage) messageContent;
            Log.d(TAG, "onReceived-ImageMessage:" + imageMessage.getRemoteUri());
        } else if (messageContent instanceof VoiceMessage) {//语音消息
            VoiceMessage voiceMessage = (VoiceMessage) messageContent;
            Log.d(TAG, "onReceived-voiceMessage:" + voiceMessage.getUri().toString());
        } else if (messageContent instanceof RichContentMessage) {//图文消息
            RichContentMessage richContentMessage = (RichContentMessage) messageContent;
            Log.d(TAG, "onReceived-RichContentMessage:" + richContentMessage.getContent());
        } else if (messageContent instanceof InformationNotificationMessage) {//小灰条消息
            InformationNotificationMessage informationNotificationMessage = (InformationNotificationMessage) messageContent;
            Log.e(TAG, "onReceived-informationNotificationMessage:" + informationNotificationMessage.getMessage());

        }
        else {
            Log.d(TAG, "onReceived-其他消息，自己来判断处理");
        }

        Log.d("RongCloudEvent", "收到的信息，targetId:" + targetId +
                                    ";sendUserId:" + sendUserId +
                                    ";content:" + content);
        //添加消息
        final WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("targetId",targetId);
        weakHashMap.put("sendUserId",sendUserId);
        weakHashMap.put("content",content);
        String url = URLManager.getInstance().getUserInfoUrl(Integer.parseInt(sendUserId));
        final String fContent = content;
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject object = new JSONObject(response);
                    String name = object.getString("nick_name");
                    String avatarUrl =object.getString("avatar_url");
                    Uri uri = Uri.parse(avatarUrl);
                    int send_user_id = Integer.parseInt(sendUserId);
                   /* //添加消息队列
                    int send_user_id = Integer.parseInt(sendUserId);
                    if(NoticeItemDataArray.getInstance().MessageArray.get(send_user_id) != null){
                        NoticeItemDataArray.getInstance().addNoticeItemMessage(send_user_id,fContent);
                    }else{
                        NoticeItemDataArray.getInstance().addNoticeItemData(sendUserId,
                                new NoticeItemData(uri,fContent,name,targetId,sendUserId));
                    }
                    NoticeItemDataArray.getInstance().hasNewNotice = true;*/

                    //发送消息
                    String scheme = "rong";
                    Uri targetUri = new UriCreator(mContext).getRongYunPrivateUri(targetId,name,true);
                    WeakHashMap weakHashMap = new WeakHashMap();
                    weakHashMap.put("type",3);      //1，系统消息；2，关注用户发布消息；3，融云消息推送
                    weakHashMap.put("sendUserId",sendUserId);
                    weakHashMap.put("targetId",targetId);
                    MessageEvent messageEvent = new MessageEvent(RONGCLOUDMESSAGE,weakHashMap);
                    Notice.getInstance().sendMessage(mContext,3,send_user_id,name,Integer.parseInt(targetId),send_user_id,targetUri,uri,fContent,messageEvent);

                    /*
                    //根据当前activity是否为MainActivity来发通知更新图标
                    String curActivityName = getCurrentActivityName(mContext);
                    Log.d("MainActivity","curActivityName:" + curActivityName);
                    /*if(curActivityName.equals("com.guangzhou.liuliang.appframework.Activity.MainActivity")){
                        //使用EventBus通知

                    }
                   EventBus.getDefault().post(new MessageEvent(RONGCLOUDMESSAGE,weakHashMap));
                   showNotification(name + ": " + fContent, targetId, name);

                  //android 5.0以上系统显示浮动通知
                    if(Build.VERSION.SDK_INT >= 21){
                        Notification notification =  new NotificationCompat.Builder(mContext)
                                .setSmallIcon(R.mipmap.icon)
                                .setContentTitle("有新消息")
                                .setContentText(name + ": " + fContent )
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setVibrate(new long[0])
                                .build();
                        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(NOTIFICATION,notification);
                    }*/
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);

        return true;

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

    private void showNotification(String text,String targetId,String name){
        if(!isAppOnForeground(mContext)){                  //app不在前台则发通知
            //PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this, LoadingActivity.class),0);
            String scheme = "rong";
            String packageName = mContext.getPackageName();
           // String query = "targetId="+ targetId + "&title="+ name;
            Uri uri = new Uri.Builder().scheme(scheme)
                    .authority(packageName)
                    .appendPath("conversation")
                    .appendPath("private")
                    .appendQueryParameter("targetId", targetId)
                    .appendQueryParameter("title", name)
                    .appendQueryParameter("isNotification", "yes")
                    .build();

            Log.d("RongCloudEvent","uri:" + uri.toString());

            Intent notifyIntent = new Intent(null,uri);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(mContext,0,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            String appName = mContext.getResources().getString(R.string.app_name);
            Notification notification =  new NotificationCompat.Builder(mContext)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.icon))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(appName)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentText(text)
                    .setContentIntent(notifyPendingIntent)
                    .build();
            //notification.defaults |= Notification.DEFAULT_SOUND;
            //notification.defaults |= Notification.DEFAULT_VIBRATE;
            NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION,notification);
        }
    }

    private String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }


}
