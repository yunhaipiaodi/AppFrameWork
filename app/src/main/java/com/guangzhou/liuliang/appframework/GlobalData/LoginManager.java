package com.guangzhou.liuliang.appframework.GlobalData;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONObject;

/**
 * Created by yunhaipiaodi on 2016/10/19.
 */
public class LoginManager {
    Context context;
    RequestQueue queue;                                     //Volley框架，请求队列
    String APP_ID = "";                                    //微信第三方登录app_id;
    IWXAPI iwxapi;

    public LoginManager(Context context){
        this.context = context;
        queue = Volley.newRequestQueue(context);
    }

    public void  Login(){
        //后台获取微信第三方登陆app_id
        String getWinXinAppIdUrl = URLManager.getInstance().getWinXinAppId(1);      //本应用在微信信息记录表的ID为1

        JsonObjectRequest request = new JsonObjectRequest(getWinXinAppIdUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //注册iwxapi
                try{
                    APP_ID = response.has("app_id")?response.getString("app_id"):"";
                    if(!APP_ID.isEmpty()){
                        iwxapi = WXAPIFactory.createWXAPI(context,APP_ID,false);
                        iwxapi.registerApp(APP_ID);
                    }else{
                        Toast.makeText(context,"登陆失败",Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(context,"登陆失败,请检查网络",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }

    private void loginWeiXin(){
        //先判断是否有安装微信
        if(!iwxapi.isWXAppInstalled()){
            Toast.makeText(context,"请先安装微信客户端,方能登陆!",Toast.LENGTH_SHORT).show();
            return;
        }

        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "App_FrameWork";
        iwxapi.sendReq(req);
    }
}
