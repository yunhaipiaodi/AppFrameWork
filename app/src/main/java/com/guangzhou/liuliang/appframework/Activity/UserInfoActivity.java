package com.guangzhou.liuliang.appframework.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.greenrobot.eventbus.Subscribe;
import com.guangzhou.liuliang.appframework.BR;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.ClassifyItem;
import com.guangzhou.liuliang.appframework.Bind.MeInfoData;
import com.guangzhou.liuliang.appframework.Bind.MyUserInfo;
import com.guangzhou.liuliang.appframework.Bind.UserInfoData;
import com.guangzhou.liuliang.appframework.Bind.UserInfoListItem;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class UserInfoActivity extends AppCompatActivity {

    RequestQueue queue;
    private int concernId = 0;
    private UserInfoData userInfoData;
    public EventBus eventBus = EventBus.getDefault();

    @Subscribe
    public void onMessageEvent(MessageEvent messageEvent){
        int code = messageEvent.getEventCode();
        switch (code){
            case 1201:              //转入登陆页面
                //未登录则跳转到登录页面
                concernId = (int) messageEvent.getEventWeakHashMap().get("concern_id");
                Toast.makeText(this,"请先登录",Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this,LoginActivity.class);
                startActivityForResult(loginIntent,1);
                break;
            case 1202:             //转入"关注"详情页面
                Intent concernIntent = new Intent(this,ConcernAndFansUserActivity.class);
                String title = messageEvent.getEventWeakHashMap().get("title").toString();
                concernIntent.putExtra("title",title);
                concernIntent.putExtra("userInfoData",userInfoData.concernUserList);
                concernIntent.putExtra("type",1);
                startActivity(concernIntent);
                break;
            case 1203:              //转入"粉丝"详情页面
                Intent fansIntent = new Intent(this,ConcernAndFansUserActivity.class);
                title = messageEvent.getEventWeakHashMap().get("title").toString();
                fansIntent.putExtra("title",title);
                fansIntent.putExtra("userInfoData",userInfoData.fansUserList);
                fansIntent.putExtra("type",2);
                startActivity(fansIntent);
                break;
            case 1204:              //转入"喜爱"页面
                Intent likeItIntent = new Intent(this,ListItemActivity.class);
                title = messageEvent.getEventWeakHashMap().get("title").toString();
                likeItIntent.putExtra("title",title);
                //将喜爱数据缓存到DataSource,供UserLikeItDetail页面调用
                DataSource.getInstance().ListItemActivityCache = userInfoData.likeItList;
                startActivity(likeItIntent);
                break;
            case 1205:              //转入"喜爱"页面
                Intent publishIntent = new Intent(this,ListItemActivity.class);
                title = messageEvent.getEventWeakHashMap().get("title").toString();
                publishIntent.putExtra("title",title);
                //将喜爱数据缓存到DataSource,供UserLikeItDetail页面调用
                DataSource.getInstance().ListItemActivityCache = userInfoData.PublishList;
                startActivity(publishIntent);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MyUserInfo binding = DataBindingUtil.setContentView(this,R.layout.activity_user_info);
        binding.setUserInfo(new UserInfoData(UserInfoActivity.this));
        final int user_id = getIntent().getIntExtra("user_id",0);
        String url = URLManager.getInstance().getUserInfoUrl(user_id);
        queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    int userId = response.getString("id").isEmpty()? 0 : response.getInt("id");

                    //取得用户个人信息
                    String userName = response.getString("nick_name");
                    String avatarUrl = response.getString("avatar_url");
                    //如果是本人，不能关注
                    boolean isMySelf = false;
                    if(userId == DataSource.getInstance().meInfoData.userId.get()){
                        isMySelf = true;
                    }else{
                        isMySelf = false;
                    }

                    userInfoData = new UserInfoData(UserInfoActivity.this,user_id,isMySelf);

                    //取得关注数据
                    JSONArray concernArray = response.getJSONArray("array_concern");
                    for(int i = 0 ; i < concernArray.length();i++){
                        JSONObject item = concernArray.getJSONObject(i);
                        int id = item.getInt("concern_id");
                        String userNickName = item.getString("concern_nick_name");
                        String userAvatarUrl = item.getString("concern_avatar_url");
                        userInfoData.addConcernUserListItem(new UserInfoListItem(id,userNickName,userAvatarUrl));
                        int concernCount = userInfoData.concernCount.get();
                        userInfoData.concernCount.set(concernCount + 1);
                    }
                    //取得粉丝数据
                    JSONArray fansArray = response.getJSONArray("array_fans");
                    for(int i = 0 ; i < fansArray.length();i++){
                        JSONObject item = fansArray.getJSONObject(i);
                        int id = item.getString("fans_id").isEmpty()?0:Integer.parseInt(item.getString("fans_id"));
                        if(id == DataSource.getInstance().meInfoData.userId.get()){
                            userInfoData.hasConcern.set(true);
                        }
                        String userNickName = item.getString("fans_nick_name");
                        String userAvatarUrl = item.getString("fans_avatar_url");
                        userInfoData.addFansUserListItem(new UserInfoListItem(id,userNickName,userAvatarUrl));
                        int fansCount = userInfoData.fansCount.get();
                        userInfoData.fansCount.set(fansCount + 1);
                    }
                    //取得喜爱项数据
                    JSONArray likeItArray = response.getJSONArray("array_like_it");
                    List<Integer> idArray = new ArrayList<Integer>();
                    for(int i = 0 ; i < likeItArray.length();i++){
                        JSONObject item = likeItArray.getJSONObject(i);
                        int id = item.getInt("tab_data_id");
                        idArray.add(id);
                        int likeItCount = userInfoData.likeItCount.get();
                        userInfoData.likeItCount.set(likeItCount + 1);
                    }
                    ArrayList<ClassifyItem> classifyItemArrayList = DataSource.getInstance().classifyItemArrayList;
                    for(ClassifyItem classifyItem:classifyItemArrayList){
                        ArrayList<BindListItem> bindListItemArrayList = classifyItem.bindListItems;
                        for(BindListItem bindListItem:bindListItemArrayList){
                            if(idArray.contains(bindListItem.id)){
                                userInfoData.likeItList.add(bindListItem);
                            }
                        }
                    }
                    //获得发布数据
                    JSONArray publishArray = response.getJSONArray("array_publish_it");
                    idArray = new ArrayList<Integer>();
                    for(int i = 0 ; i < publishArray.length();i++){
                        JSONObject item = publishArray.getJSONObject(i);
                        int id = item.getInt("tab_data_id");
                        idArray.add(id);
                        int publishCount = userInfoData.publishCount.get();
                        userInfoData.publishCount.set(publishCount + 1);
                    }
                    classifyItemArrayList = DataSource.getInstance().classifyItemArrayList;
                    for(ClassifyItem classifyItem:classifyItemArrayList){
                        ArrayList<BindListItem> bindListItemArrayList = classifyItem.bindListItems;
                        for(BindListItem bindListItem:bindListItemArrayList){
                            if(idArray.contains(bindListItem.id)){
                                userInfoData.PublishList.add(bindListItem);
                            }
                        }
                    }
                    binding.setVariable(BR.UserInfo,userInfoData);
                    binding.executePendingBindings();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int userId = 0;
                String userName ="未知";
                String avatarUrl = "";
                WeakHashMap weakHashMap = new WeakHashMap();
                weakHashMap.put("user_id",userId);
                weakHashMap.put("user_name",userName);
                weakHashMap.put("avatar_url",avatarUrl);
                UserInfoData userInfoData = new UserInfoData(UserInfoActivity.this,user_id,false);
                binding.setUserInfo(userInfoData);
            }
        });
        queue.add(request);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        switch (requestCode){
            case 1:             //从登陆界面返回

                //继续先前的操作

                if(DataSource.getInstance().meInfoData.hasLoad){
                    //关注对象粉丝数+1
                    userInfoData.addFansUserListItem(new UserInfoListItem( DataSource.getInstance()
                            .meInfoData.userId.get(),
                            DataSource.getInstance().meInfoData.userName.get(),
                            DataSource.getInstance().meInfoData.avatarUrl.get()));

                    //自己的关注数+1
                    DataSource.getInstance()
                            .meInfoData
                            .concernCount.set(DataSource.getInstance()
                            .meInfoData
                            .concernCount.get() + 1);

                    //后台更新数据
                    String insertConcern = URLManager.getInstance()
                            .getConcernUrl(DataSource.getInstance().meInfoData.userId.get(),
                                    userInfoData.userId.get());
                    commitUrl(this,insertConcern);
                    userInfoData.hasConcern.set(true);
                }
                break;
        }
    }

    private void commitUrl(Context context, String url){
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }



    @Override
    protected void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    protected void onStop() {
        eventBus.unregister(this);
        super.onStop();

    }
}
