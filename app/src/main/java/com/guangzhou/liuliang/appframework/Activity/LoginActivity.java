package com.guangzhou.liuliang.appframework.Activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Bind.MyInfo;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {


    //Event Bus
    public static EventBus eventBus = EventBus.getDefault();
    public static IWXAPI iwxapi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyInfo binding = DataBindingUtil.setContentView(this,R.layout.fragment_me_info);
        binding.setMeInfo(DataSource.getInstance().meInfoData);
        setTitle("登陆");


        //后台获取微信第三方登陆app_id
        String getWinXinAppIdUrl = URLManager.getInstance().getWinXinAppId(1);      //本应用在微信信息记录表的ID为1

        //注册iwxapi
        String app_id = DataSource.getInstance().APP_ID;
        iwxapi = WXAPIFactory.createWXAPI(this,app_id,false);
        iwxapi.registerApp(app_id);
    }

    @Subscribe
    public void onMessageEvent(MessageEvent event){
        int code = event.getEventCode();
        switch (code){
            case 1009:              //点击跳转到微信登陆页面
                loginWeiXin();
                break;
        }
    }

    private void loginWeiXin(){
        //先判断是否有安装微信
        if(!iwxapi.isWXAppInstalled()){
            Toast.makeText(this,"请先安装微信客户端,方能登陆!",Toast.LENGTH_SHORT).show();
            return;
        }

        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "App_FrameWork";
        iwxapi.sendReq(req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("LoginActivity","返回,requestCode:" + requestCode + ";resultCode:" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            finish();
        }
    }


    @Override
    protected void onStart(){
        super.onStart();
        eventBus.register(this);
    }

    @Override
    protected void onStop(){
        eventBus.unregister(this);
        super.onStop();
    }


}
