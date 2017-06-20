package com.guangzhou.liuliang.appframework.wxapi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Activity.MainActivity;
import com.guangzhou.liuliang.appframework.Bind.MeInfoData;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.RongYun.RongYunMethod;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import org.json.JSONObject;

/**
 * Created by yunhaipiaodi on 2016/5/6.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {


    //应用在微信注册的信息


    //Volly请求框架
    RequestQueue queue ;

    ProgressDialog progressDialog;

    private void refreshData(String nickName,String openId,String headImgUrl,String unionId,String userId,int sex,String rongyun_token){
       // Log.d("WXEntryActivity","call refreshData");
       // Log.d("WXEntryActivity","nickName:" + nickName + "; openId:" + openId + "; headImgUrl:" + headImgUrl + "; unionId:" + unionId);
        //更新本地数据
        SharedPreferences sharedPreferences = getSharedPreferences("localUserInfo",0);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("hasLoad",true);
        editor.putString("nickName",nickName);
        editor.putString("avatarImageUrl",headImgUrl);
        editor.putString("openId",openId);
        editor.putString("unionId",unionId);
        editor.putInt("meId",Integer.parseInt(userId));
        editor.putString("rongYunToken",rongyun_token);
        editor.putInt("sex",sex);
        editor.commit();

        //更新DataSource数据
        DataSource.getInstance().meInfoData.loadBtnText.set("注销");
        DataSource.getInstance().meInfoData.userName.set(nickName);
        DataSource.getInstance().meInfoData.avatarUrl.set(headImgUrl);
        DataSource.getInstance().meInfoData.openId = openId;
        DataSource.getInstance().meInfoData.unionId = unionId;
        DataSource.getInstance().meInfoData.userId.set(Integer.parseInt(userId));
        DataSource.getInstance().meInfoData.rongYunToken = rongyun_token;
        DataSource.getInstance().meInfoData.sex.set(sex);

        DataSource.getInstance().refreshLocalData();

        DataSource.getInstance().meInfoData.hasLoad = true;

       //登陆融云
        if(!DataSource.getInstance().meInfoData.rongYunToken.isEmpty()){
            String token = DataSource.getInstance().meInfoData.rongYunToken;
            RongYunMethod method = new RongYunMethod(this);
            method.connect( token, new RongYunMethod.ConnectResult() {
                @Override
                public void onSuccess(String userid) {
                    DataSource.getInstance().meInfoData.rongYunLoad = true;
                    Log.d("WXEntryActivity","融云登陆成功");
                    finish();
                }
                @Override
                public void onFail(int code, String errorMsg) {
                    //Toast.makeText(WXEntryActivity.this,"即时登陆失败,错误码：" + code,Toast.LENGTH_LONG).show();
                    Log.d("WXEntryActivity","即时登陆失败,错误码：" + code + "; msg:" + errorMsg);
                    DataSource.getInstance().meInfoData.rongYunLoad = false;
                    finish();
                }
            });
        }else{
            //Toast.makeText(WXEntryActivity.this,"即时登陆授权信息获取失败,请尝试重新注销用户,重新登陆",Toast.LENGTH_LONG).show();
            DataSource.getInstance().meInfoData.rongYunLoad = false;
            finish();
        }

        //finish();

       /* //将数据记录到后台user_info表中
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("openId",openId);
        weakHashMap.put("nickName",nickName);
        weakHashMap.put("headImgUrl",headImgUrl);
        weakHashMap.put("unionId",unionId);
        weakHashMap.put("imsi",MainActivity.imsi == null ?"":MainActivity.imsi);
        weakHashMap.put("imei",MainActivity.imei == null ?"":MainActivity.imei);
        String url = URLManager.getInstance().getRegister_user_url(weakHashMap);
        StringRequest stringRequest = new StringRequest(Request.RongYunMethod.GET,
                    url,
                    new Response.Listener<String>(){
                        @Override
                        public void onResponse(String response) {
                            DataSource.getInstance().meInfoData.id.set(Integer.parseInt(response));
                            editor.putInt("meId",Integer.parseInt(response));
                            editor.commit();
                            WXEntryActivity.this.setResult(RESULT_OK);
                            finish();
                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            WXEntryActivity.this.setResult(RESULT_OK);
                            finish();
                        }
                    });

        queue.add(stringRequest);*/
    }

    /*private void getAccessToken(String Url){
        // Request a string response from the provided URL.
        Log.d("WXEntryActivity","call getAccessToken");
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.RongYunMethod.GET, Url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        // Display the first 500 characters of the response string.
                        String access_token = "";
                        String Open_id = "";
                        try{
                            access_token = json.getString("access_token");
                            Open_id = json.getString("openid");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        String url2 = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s",
                                access_token,Open_id);
                        Log.d("WXEntryActivity","url2:" + url2);
                        getUserInfo(url2);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(WXEntryActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void getUserInfo(String Url){
        Log.d("WXEntryActivity","call getUserInfo");
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.RongYunMethod.GET, Url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        // Display the first 500 characters of the response string.
                        String nickName = "";
                        String openId = "" ;
                        String headImgUrl = "";
                        String unionId = "";
                        try{
                            if(json.has("nickname"))
                            nickName = json.getString("nickname");
                            if(json.has("openid"))
                            openId = json.getString("openid");
                            if(json.has("headimgurl"))
                            headImgUrl = json.getString("headimgurl");
                            if(json.has("unionid"))
                            unionId = json.getString("unionid");
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        refreshData(nickName,openId,headImgUrl,unionId);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(WXEntryActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }*/

    private void getUserInfo(String url){
        JsonObjectRequest jsonRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try{
                    //Log.d("response","return json:" + response);
                    //JSONObject jsonObject = new JSONObject(response);
                    final String openId = jsonObject.getString("openid");
                    final String nickName = jsonObject.getString("nickname");
                    final String avatarUrl = jsonObject.getString("headimgurl");
                    final String unionId = jsonObject.getString("unionid");
                    final int sex = jsonObject.getInt("sex");
                    final String userId = jsonObject.getString("user_id");
                    final String RongYunToken = jsonObject.getString("rongyun_token");
                    progressDialog.dismiss();
                    //第一次显示修改名字
                    final SharedPreferences sharedPreferences = getSharedPreferences("localUserInfo",0);
                    Boolean hasChangeName = sharedPreferences.getBoolean("hasChangeName",false);
                    if(!hasChangeName){
                        //第一次登陆
                        final EditText editText = new EditText(WXEntryActivity.this);
                        editText.setHint(nickName);
                        new AlertDialog.Builder(WXEntryActivity.this).setTitle("用户名")
                                        .setView(editText)
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                final String newName = editText.getText().toString();
                                                if(!newName.isEmpty() && !newName.equals(nickName)) {        //更改名称
                                                    String url = URLManager.getInstance().getEditNickName(userId,newName,avatarUrl);
                                                    Log.d("weixin","update nickname:" + url);
                                                    StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            refreshData(newName,openId,avatarUrl,unionId,userId,sex,RongYunToken);
                                                        }
                                                    }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                            Toast.makeText(WXEntryActivity.this,"更改名称失败;" + error.getMessage(),Toast.LENGTH_SHORT).show();
                                                            refreshData(nickName,openId,avatarUrl,unionId,userId,sex,RongYunToken);
                                                        }
                                                    });
                                                    queue.add(stringRequest);
                                                }else{
                                                    refreshData(nickName,openId,avatarUrl,unionId,userId,sex,RongYunToken);
                                                }
                                            }
                                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                refreshData(nickName,openId,avatarUrl,unionId,userId,sex,RongYunToken);
                            }
                        }).show();
                       final SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("hasChangeName",true);
                        editor.commit();
                    }else{
                        refreshData(nickName,openId,avatarUrl,unionId,userId,sex,RongYunToken);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(WXEntryActivity.this,"登陆失败，"+ e.getMessage(),Toast.LENGTH_SHORT).show();
                    WXEntryActivity.this.finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressDialog.dismiss();
                WXEntryActivity.this.finish();
            }
        });
        queue.add(jsonRequest);
    }


    @Override
    protected void onCreate(Bundle savedStateInstance){
        super.onCreate(savedStateInstance);
        setContentView(R.layout.activity_wx_entry);
        Log.d("WXEntryActivity","onCreate");

        //获得Volly对象
        queue = Volley.newRequestQueue(this);

        MainActivity.iwxapi.handleIntent(getIntent(), this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("登陆");
        progressDialog.setMessage("开始登陆...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d("WXEntryActivity","onNewIntent");
        MainActivity.iwxapi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    //请求微信登陆后，此处为微信返回的回调，根据回调做下一步操作
    @Override
    public void onResp(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                //取得授权，进入第二部:通过code获取access_token
                String code = ((SendAuth.Resp)resp).code;
                String url = URLManager.getInstance().getUserInfoUrl(code,DataSource.getInstance().APP_ID,DataSource.getInstance().imsi,DataSource.getInstance().imei);
                getUserInfo(url);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                finish();
                //用户拒绝授权
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                finish();
                //用户取消
                break;
            default:
                finish();
                //未知错误
                break;

        }
    }
}
