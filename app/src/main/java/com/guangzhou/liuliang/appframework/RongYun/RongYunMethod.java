package com.guangzhou.liuliang.appframework.RongYun;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Bind.MeInfoData;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource.DSResult;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.URL.URLManager;

import org.json.JSONObject;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

/**
 * Created by yunhaipiaodi on 2016/7/26.
 */
public class RongYunMethod {

    private Context context;

    private RequestQueue queue;

    public RongYunMethod(Context context) {
        this.context = context;
        queue = Volley.newRequestQueue(context);
    }

    public void connect(String rongYunToken, final ConnectResult connectResult) {
        //Log.d("DataSource","connect 得到的token:" + rongYunToken);
        RongIM.connect(rongYunToken, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                //Connect Token 失效的状态处理，需要重新获取 Token
                connectResult.onFail(1, "失效的状态处理，需要重新获取Token");
            }

            @Override
            public void onSuccess(String userId) {
                connectResult.onSuccess(userId);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                connectResult.onFail(errorCode.getValue(), errorCode.getMessage());
            }
        });
    }

    public void InitRongYun(MeInfoData meInfoData, final DSResult result) {
        //将本机的用户信息录入RongUserInfo中，供融云SDK调用
        String meId = String.valueOf(meInfoData.userId.get());
        String nickName = meInfoData.userName.get();
        String avatarImageUrl = meInfoData.avatarUrl.get();
        DataSource.getInstance().RongUserInfo.put(meId, new UserInfo(meId, nickName, Uri.parse(avatarImageUrl)));

        //初始化
        RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String id) {
                UserInfo info = DataSource.getInstance().RongUserInfo.get(id);
                if (info == null) {
                    Log.d("DataSource", "userinfo is null");
                    String url = URLManager.getInstance().getUserInfoUrl(Integer.parseInt(id));

                    final String fId = id;
                    JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int userId = response.getString("id").isEmpty() ? 0 : response.getInt("id");
                                String userName = response.getString("nick_name");
                                String avatarUrl = response.getString("avatar_url");

                                DataSource.getInstance()
                                        .RongUserInfo
                                        .put(fId,
                                                new UserInfo(String.valueOf(userId),
                                                        userName, Uri.parse(avatarUrl)));
                                RongIM.getInstance().refreshUserInfoCache(new UserInfo(String.valueOf(userId),
                                        userName, Uri.parse(avatarUrl)));
                            } catch (Exception e) {
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
                return info;//根据 userId 去你的用户系统里查询对应的用户信息返回给融云 SDK。
            }

        }, true);

        String rongYunToken = meInfoData.rongYunToken;
        //登陆融云
        if (!rongYunToken.isEmpty()) {
            Log.d("DataSource", "token:" + rongYunToken);
            connect(rongYunToken, new ConnectResult() {
                @Override
                public void onSuccess(String userid) {
                    result.done();
                    Log.d("DataSource", "融云登陆成功");
                    DataSource.getInstance().meInfoData.rongYunLoad = true;
                }

                @Override
                public void onFail(int code, final String errorMsg) {
                    // Toast.makeText(context,"即时登陆失败,错误码：" + code,Toast.LENGTH_LONG).show();
                    Log.d("DataSource", "即时登陆失败,错误码：" + code + "; msg:" + errorMsg);
                    result.done();

                }
            });
        } else {
            result.done();
            Toast.makeText(context, "即时聊天功能登陆失败,请尝试重新注销用户,重新登陆", Toast.LENGTH_LONG).show();
            meInfoData.rongYunLoad = false;
        }

    }


    public interface ConnectResult{
        public void onSuccess(String userid);
        public void onFail(int code, String errorMsg);
    }


}
