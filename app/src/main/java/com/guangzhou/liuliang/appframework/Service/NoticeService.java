package com.guangzhou.liuliang.appframework.Service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Activity.SystemNoticeActivity;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.ClassifyItem;
import com.guangzhou.liuliang.appframework.Bind.MeInfoData;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemData;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemDataArray;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.GlobalData.DataMaker;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.GlobalData.Notice;
import com.guangzhou.liuliang.appframework.GlobalData.UriCreator;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;


/**
 * Created by yunhaipiaodi on 2016/5/1.
 */
public class NoticeService extends Service {

    private NotificationManager mNM;
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private int NOTIFICATION = 10006;
    private String ACTION = "android.intent.action.NoticeServiceCast";

    private int delayTime = 60000;

    private int SYSTEM_NOTICE = 1206;

    ArrayList<BindListItem> new_publish_list;

    RequestQueue queue;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            int code = message.what;
            switch (code){
                case 1:         //此处执行定时任务
                    getNotificationByNet();
                    break;
                default:
                    break;
            }
        }

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        //第一次登陆发送欢迎信息
        SharedPreferences sharedPreferences = getSharedPreferences("firstLogin",0);
        boolean firstLogin = sharedPreferences.getBoolean("hasLogin",false);

        new_publish_list = new ArrayList<>();
        if(!firstLogin){
            String firstMsg = "欢迎来到我要的生活，希望你在这里找到自己的一片天地";
            UriCreator uriCreator = new UriCreator(this);
            Uri systemNoticeUri =uriCreator.getSystemNoticeActivityUri(true);            //获得SystemNoticeActivity跳转URI
            Uri appIconUri = uriCreator.getAppIconUri();
            WeakHashMap weakHashMap = new WeakHashMap();
            String appName = getResources().getString(R.string.app_name);
            weakHashMap.put("sendUserId","-1");
            weakHashMap.put("targetId","-1");
            MessageEvent event = new MessageEvent(SYSTEM_NOTICE,weakHashMap);
            Notice.getInstance().sendMessage(this,1,-1,appName,-1,-1,systemNoticeUri,appIconUri,firstMsg,event);         //发送消息；

            //将第一次登陆后的状态储存
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("hasLogin",true);
            editor.commit();
        }

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        timerTask = new TimerTask(){
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(timerTask,delayTime,delayTime);
        queue = Volley.newRequestQueue(this);
        getNotificationByNet();
    }

    @Override
    public void onDestroy(){
        mNM.cancel(NOTIFICATION);
        super.onDestroy();
    }

    int requestTaskCount = 0;               //后台查询任务数
    private void getNotificationByNet(){
        int user_id = -1;
        if(DataSource.getInstance().meInfoData.hasLoad){
            user_id = DataSource.getInstance().meInfoData.userId.get();
        }
        String url = URLManager.getInstance().getNotice_url(user_id);            //天气接口，测试用
        //new GetNotificationTast().execute(url);
        //Volley框架网络请求
        new_publish_list.clear();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //解析JSON
                        try{
                            JSONArray notice_array = response.has("notice")?response.getJSONArray("notice"):new JSONArray();
                            for(int i = 0; i < notice_array.length();i++){
                                JSONObject object = notice_array.getJSONObject(i);

                                //获取系统推送消息
                                String notice = object.has("message")?object.getString("message"):"";
                                if(!noticeHasShow(notice)){
                                    //显示信息
                                    String appName = NoticeService.this.getResources().getString(R.string.app_name);
                                    UriCreator uriCreator = new UriCreator(NoticeService.this);
                                    Uri systemNoticeUri =uriCreator.getSystemNoticeActivityUri(true);            //获得SystemNoticeActivity跳转URI
                                    Uri appIconUri = uriCreator.getAppIconUri();
                                    WeakHashMap weakHashMap = new WeakHashMap();
                                    weakHashMap.put("type","1");
                                    weakHashMap.put("sendUserId","-1");
                                    weakHashMap.put("targetId","-1");
                                    MessageEvent event = new MessageEvent(SYSTEM_NOTICE,weakHashMap);
                                    Notice.getInstance().sendMessage(NoticeService.this,1,-1,appName,-1,-1,systemNoticeUri,appIconUri,notice,event);
                                    //将此信息添加都本地储存
                                }
                            }

                            //获取关注用户发布消息
                            JSONArray publish_array = response.has("concern")? response.getJSONArray("concern"):new JSONArray();
                            ArrayList<Integer> idArray = new ArrayList<Integer>();               //储存发布者发布ID
                            final ArrayList<String> nameArray = new ArrayList<>();          //储存发布者名称
                            for(int j=0;j<publish_array.length();j++){
                                JSONObject object1 = publish_array.getJSONObject(j);
                                int publish_user_id = Integer.parseInt(object1.has("publish_id")?object1.getString("publish_id"):"0");
                                idArray.add(publish_user_id);
                                String publish_user_name = object1.has("publish_user_name")?object1.getString("publish_user_name"):"";
                                if(!nameArray.contains(publish_user_name)){
                                    nameArray.add(publish_user_name);
                                }
                            }

                            //根据发布者用户ID构建RecycleView数据集

                            ArrayList<ClassifyItem> classifyItemArrayList = DataSource.getInstance().classifyItemArrayList;

                            for(ClassifyItem classifyItem:classifyItemArrayList) {
                                ArrayList<BindListItem> bindListItemArrayList = classifyItem.bindListItems;
                               /*
                                }*/
                                for (int id : idArray) {       //遍历新发布ID
                                    if(!publishHasShow(id)){         //此ID已经显示，跳过
                                        boolean result = false;
                                        for (BindListItem bindListItem : bindListItemArrayList) {
                                            //判断原始数据中是否含有该ID对应的项，将结果记录在result中
                                            if (bindListItem.id == id) {        //包含该ID
                                                new_publish_list.add(bindListItem);
                                                result = true;
                                                break;                         //跳出该遍历查询
                                            }
                                        }

                                        //如果bindListItem未包含该id，需要联网查询结果
                                        if(!result){
                                            String getItemUrl = URLManager.getInstance().getItemById(id);
                                            JsonObjectRequest objectRequest = new JsonObjectRequest(getItemUrl, new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    requestTaskCount--;
                                                    BindListItem bindListItem = new DataMaker().makeBindListItem(response,NoticeService.this);
                                                    new_publish_list.add(bindListItem);
                                                    if(requestTaskCount == 0){      //所有后台查询任务都已经完成
                                                        if(new_publish_list.size() > 0){        //有数据才发送
                                                            //将数据缓存
                                                            DataSource.getInstance().ListItemActivityCache = new_publish_list;

                                                            /*---------发送推送信息--------------*/
                                                            //构建推送消息
                                                            String notice = "你关注的用户 ";
                                                            for(String name:nameArray){
                                                                notice += name + ", ";
                                                            }
                                                            notice += "发布了新的圈子,点击查看";


                                                            String appName = NoticeService.this.getResources().getString(R.string.app_name);
                                                            UriCreator uriCreator = new UriCreator(NoticeService.this);
                                                            Uri publishListUri =uriCreator.getPublishListActivityUri(true);          //获得SystemNoticeActivity跳转URI
                                                            Uri appIconUri = uriCreator.getAppIconUri();
                                                            WeakHashMap weakHashMap = new WeakHashMap();
                                                            weakHashMap.put("type","2");
                                                            weakHashMap.put("sendUserId","-2");
                                                            weakHashMap.put("targetId","-2");
                                                            MessageEvent event = new MessageEvent(SYSTEM_NOTICE,weakHashMap);
                                                            Notice.getInstance().sendMessage(NoticeService.this,2,-2,appName,-2,-2,publishListUri,appIconUri,notice,event);
                                                        }
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {

                                                }
                                            });

                                            queue.add(objectRequest);
                                            requestTaskCount ++;
                                        }
                                    }

                                }
                            }





                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NoticeService","查询失败,error:" + error.getMessage() );
            }
        });
        queue.add(jsonObjectRequest);
    }

    //判断系统推送是否已经显示
    private boolean noticeHasShow(String notice){
        boolean result = false;
        SharedPreferences sharedPreferences = getSharedPreferences("notice",0);
        Set<String> setStr = sharedPreferences.getStringSet("noticeArray",new HashSet<String>());
        if(setStr != null){
            if(setStr.contains(notice)){
                result= true;
            }
            else{
                result=false;
                setStr.add(notice);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet("noticeArray",setStr);
                editor.commit();
            }
        }
        else{
            result=false;
            setStr.add(notice);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("noticeArray",setStr);
            editor.commit();
        }

        return result;
    }

    //判断关注用户发布的推送是否已经显示
    private boolean publishHasShow(int publishId){
        boolean result = false;
        SharedPreferences sharedPreferences = getSharedPreferences("notice",0);
        Set<String> setStr = sharedPreferences.getStringSet("publishIdArray",new HashSet<String>());
        if(setStr != null){
            if(setStr.contains(String.valueOf(publishId))){
                result= true;
            }
            else{
                result=false;
                setStr.add(String.valueOf(publishId));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet("publishIdArray",setStr);
                editor.commit();
            }
        }
        else{
            result=false;
            setStr.add(String.valueOf(publishId));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("publishIdArray",setStr);
            editor.commit();
        }

        return result;
    }



    //判定当前程序在前台还是后台
    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void showNotification(String text){
        if(!isAppOnForeground(this)){                  //app不在前台则发通知
            //记录数据
            Resources r = getResources();
            String appName = getResources().getString(R.string.app_name);
            Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"
                    +r.getResourcePackageName(R.mipmap.icon)+"/"
                    +r.getResourceTypeName(R.mipmap.icon)+"/"
                    +r.getResourceEntryName(R.mipmap.icon)
            );
            if(NoticeItemDataArray.getInstance().MessageArray.get(-1) != null){
                NoticeItemDataArray.getInstance().addNoticeItemMessage(-1,text);
            }else{
                NoticeItemDataArray.getInstance().addNoticeItemData("-1",new NoticeItemData(uri,text,appName,"-1","-1"));
            }

            DataSource.getInstance().systemNotices.add(text);

            //发送notification
            Intent notifyIntent = new Intent(this, SystemNoticeActivity.class);
            notifyIntent.putExtra("isNotify",true);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(this,0,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification =  new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(appName)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVibrate(new long[0])
                    .setContentIntent(notifyPendingIntent)
                    .build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION,notification);
        }
        else{                                   //app在前台则发消息在通知栏显示
            SendBroadcastMessage(text,ACTION);
        }
    }

    private void SendBroadcastMessage(String Message,String Action){
        Intent intent = new Intent();
        intent.putExtra("message",Message);
        intent.setAction(Action);
        this.sendBroadcast(intent);
    }

    public String readFully(InputStream entityResponse) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = entityResponse.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString();
    }

   /* class GetNotificationTast extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... params) {
            String mUrl = params[0];
            String result = "";
            try{
                URL url = new URL(mUrl);
                URLConnection urlConnection = url.openConnection();
                HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.connect();
                InputStream is = httpURLConnection.getInputStream();
                result = readFully(is);
            }catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result){
            showNotification(result);
        }
    }*/
}
