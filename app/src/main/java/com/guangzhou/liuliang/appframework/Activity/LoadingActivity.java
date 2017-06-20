package com.guangzhou.liuliang.appframework.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.pm.ActivityInfoCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LoadingActivity extends Activity {

    private int delayTime = 5000;                           //画面停留时间,单位毫秒

    private boolean hasLoadImage = false;                //判断图片是否下载完成

    private boolean hasTimeOut = false;                  //判断是否到时间

    private boolean DSInit =  false;                     //DataSource初始化是否完成

    String url = URLManager.getInstance().getLoading_url();     //请求URL

    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    //定义延时任务
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            hasTimeOut= true;
            transToMainActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        if(Build.VERSION.SDK_INT >= 23){
            checkRuntimePermissions();
        }else{
            Init();
        }
    }

    private void Init(){
        //启动定时任务
        new Timer().schedule(timerTask,delayTime);

        //初始化JsonData
        //获得json
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                DataSource.getInstance().InitData(response, LoadingActivity.this,new DataSource.DSResult(){
                    @Override
                    public void done() {
                        DSInit = true;
                        transToMainActivity();
                    }
                });
                //获得加载图片
                ImageView imageView = (ImageView)findViewById(R.id.imageView_load);
                String image_url = DataSource.getInstance().Loading_image_url;
                setImage(imageView,image_url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoadingActivity.this,"读取数据失败，退出",Toast.LENGTH_SHORT).show();
                System.exit(0);
                Log.e("LoadingActivity",error.getMessage());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void checkRuntimePermissions(){
        ArrayList<String> permissions = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_SETTINGS)
                != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.WRITE_SETTINGS);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
                != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
        }

        if(permissions.size() == 0){
            Init();
            return;
        }

        String [] permissionArray = permissions.toArray(new String[permissions.size()]);
        ActivityCompat.requestPermissions(this,permissionArray,MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                   String permissions[],int [] grantResults){
        switch (requestCode){
            case 100:
                if(grantResults.length >0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //权限允许，下一步
                    Init();
                }else{
                    this.finish();
                    System.exit(0);
                }
                break;
        }
    }

    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    public void onStop(){
        super.onStop();
    }

    //跳转到MainActivity
    private void transToMainActivity(){
        if(hasLoadImage && hasTimeOut && DSInit){
           Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @RequiresPermission(Manifest.permission.INTERNET)
    private void setImage(ImageView imageView, @NonNull String image_url){
        Picasso.Builder builder = new Picasso.Builder(imageView.getContext());
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {

            }
        });
        Picasso pic = builder.build();
        if(!image_url.isEmpty()){
            pic.load(image_url)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            hasLoadImage=true;
                            transToMainActivity();
                        }
                        @Override
                        public void onError() {
                            hasLoadImage=false;
                        }
                    });
        }

    }
}
