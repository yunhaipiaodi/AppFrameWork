package com.guangzhou.liuliang.appframework.Activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Adapter.UserLikeDetailAdapter;
import com.guangzhou.liuliang.appframework.Bind.BindLikeUser;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.ClassifyItem;
import com.guangzhou.liuliang.appframework.Bind.MeInfoData;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.Share.AndroidShare;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.WeakHashMap;

public class ListItemActivity extends AppCompatActivity {

    RequestQueue queue;                                     //Volley框架请求队列

    private WeakHashMap cache = new WeakHashMap();

    RecyclerView recyclerView;

    Boolean isNotify  = false;                            //是否是Notification推送消息传送过来的。True，是；False,否
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_like_it_detail);

        //获得传递过来的参数
        isNotify = getIntent().getData().getBooleanQueryParameter("isNotify",false);

        String title = getIntent().getStringExtra("title"); //显式启动传过来的title

        if(title == null){            //title为空,本activity可能是隐式启动，查询data里的query参数
            title = getIntent().getData().getQueryParameter("title");
        }

        //初始化volley队列
        queue = Volley.newRequestQueue(this);

        //设置标题
        setTitle(title);

        //RecyclerView
        ArrayList<BindListItem> userInfoDataList = DataSource.getInstance().ListItemActivityCache;
        recyclerView = (RecyclerView)findViewById(R.id.userLikeItDetailRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new UserLikeDetailAdapter(userInfoDataList));

        recyclerView.setItemViewCacheSize(3);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    Picasso.with( ListItemActivity.this).resumeTag("tag");
                }
                else
                {
                    Picasso.with( ListItemActivity.this).pauseTag("tag");
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && isNotify) { //按下的如果是BACK，同时没有重复
            //do something here
            Intent intent = new Intent(this,LoadingActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    //获取当前时间
    public String refFormatNowDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String retStrFormatNowDate = sdFormatter.format(nowTime);
        return retStrFormatNowDate;
    }

    //喜爱数加一
    private void likeCountAddOne(int index,int classifyIndex){
        BindListItem bindListItem= DataSource.getInstance()
                .classifyItemArrayList.get(classifyIndex)
                .getListItemByIndex(index);
        bindListItem.hasLikeIt.set(true);
        int likeCount = bindListItem.likeUserCount.get();
        bindListItem.likeUserCount.set(likeCount + 1);

        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("userName",DataSource.getInstance().meInfoData.userName.get());
        weakHashMap.put("avatarUrl",DataSource.getInstance().meInfoData.avatarUrl.get());
        weakHashMap.put("openId",DataSource.getInstance().meInfoData.openId);
        weakHashMap.put("id",DataSource.getInstance().meInfoData.userId.get());
        weakHashMap.put("user_id",DataSource.getInstance().meInfoData.userId.get());
        weakHashMap.put("insertTime",refFormatNowDate());

        bindListItem.likeUserDatas.add(new BindLikeUser(weakHashMap));
    }

    private void refreshData(){
        String refreshUrl = URLManager.getInstance().getLoading_url();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,refreshUrl,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //更新数据
                DataSource.getInstance().refreshData(response);
                refresh();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(jsonObjectRequest);
    }

    //更新所有页面数据
    private void refresh(){
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    //未登录情况下点击“喜爱”，“关注”按钮，会跳转到登陆页面，返回到这里继续处理前面的操作
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        switch(requestCode){
            case 2:
                //登录界面返回后，继续处理相关操作
                int typeIndex = (int)cache.get("operator_id");
                switch (typeIndex){
                    case 1:         //点击“喜爱”按钮
                        int likeIndex = (int) cache.get("index");
                        int likeClassifyIndex = (int) cache.get("classifyIndex");
                        BindListItem bindListItem= DataSource.getInstance()
                                .classifyItemArrayList.get(likeClassifyIndex)
                                .getListItemByIndex(likeIndex);
                        String refreshLikeIt = URLManager.getInstance().getRefreshLikeIt_url(bindListItem.id,
                                DataSource.getInstance().meInfoData.openId,
                                DataSource.getInstance().meInfoData.userName.get(),
                                DataSource.getInstance().meInfoData.avatarUrl.get());

                        //判断是否已经点击“喜爱”按钮
                        String openId = DataSource.getInstance().meInfoData.openId;
                        boolean hasLike = false;
                        for(BindLikeUser bindLikeUser:bindListItem.likeUserDatas){
                            if(bindLikeUser.openId.get().equals(openId)){
                                hasLike = true;
                                break;
                            }
                        }
                        if(hasLike){
                            Toast.makeText(this,"你已经表达喜爱了哦",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            queue.add(new StringRequest(refreshLikeIt,null,null));
                            //喜爱数加1
                            likeCountAddOne(likeIndex,likeClassifyIndex);
                            break;
                        }

                        //更新数据
                        refreshData();
                        break;
                    case 3:     //点击“关注”按钮
                        //更新DataSource中该发布者所有发布信息的关注状态；
                        int publisher_id = (int)cache.get("publisher_id");
                        int me_id = (int)cache.get("me_id");
                        for (ClassifyItem classifyItem:DataSource.getInstance().classifyItemArrayList) {
                            for(BindListItem bindListItem1:classifyItem.bindListItems){
                                if(bindListItem1.userId.get() == publisher_id){      //该用户已被订阅
                                    bindListItem1.hasConcern.set(true);
                                }
                            }
                        }
                        //后台记录订阅信息
                        String insertConcern = URLManager.getInstance()
                                .getConcernUrl(me_id,publisher_id
                                );
                        StringRequest stringRequest = new StringRequest(insertConcern, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        },new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        queue.add(stringRequest);
                        break;

                }
                break;
            case 3:         //评论界面返回
                Log.d("MainListActivity","从评论界面返回");
                refresh();
                break;
        }

    }


    //EventBus 事件接收处理
    @Subscribe
    public void onMessageEvent(MessageEvent messageEvent) {
        int code = messageEvent.getEventCode();
        Log.d("MainListActivity", "code:" + code);
        switch (code) {
            case 1004:          //点击"评论详情"按钮
                int CommentIndex = (int) messageEvent.getEventWeakHashMap().get("index");
                int CommentClassifyIndex = (int) messageEvent.getEventWeakHashMap().get("classifyIndex");
                int type = 1;   //不自动弹出评论框
                Intent intent = new Intent(this, CommentDetail.class);
                intent.putExtra("index", CommentIndex);
                intent.putExtra("ClassifyIndex", CommentClassifyIndex);
                intent.putExtra("type",type);
                startActivityForResult(intent,3);
                break;
            case 1005:         //点击"喜欢人数"按钮
                int LikeIndex = (int) messageEvent.getEventWeakHashMap().get("index");
                int LikeClassifyIndex = (int) messageEvent.getEventWeakHashMap().get("classifyIndex");
                Intent likeIntent = new Intent(this, LikeUserActivity.class);
                likeIntent.putExtra("index", LikeIndex);
                likeIntent.putExtra("ClassifyIndex", LikeClassifyIndex);
                startActivity(likeIntent);
                break;
            case 1015:          //点击"喜爱"按钮
                //访问一条链接更新数据
                if(!DataSource.getInstance().meInfoData.hasLoad){
                    //当前操作储存起来
                    cache.clear();
                    cache.put("operator_id",1);     //1：点击“喜爱”按钮；2:点击分享；3：订阅
                    cache.put("index",(int) messageEvent.getEventWeakHashMap().get("index"));
                    cache.put("classifyIndex",(int) messageEvent.getEventWeakHashMap().get("classifyIndex"));

                    //未登录则跳转到登录页面
                    Toast.makeText(this,"请先登录",Toast.LENGTH_LONG).show();
                    Intent loginIntent = new Intent(this,LoginActivity.class);
                    startActivityForResult(loginIntent,2);

                }else{
                    int likeIndex = (int) messageEvent.getEventWeakHashMap().get("index");
                    int likeClassifyIndex = (int) messageEvent.getEventWeakHashMap().get("classifyIndex");
                    BindListItem bindListItem= DataSource.getInstance()
                            .classifyItemArrayList.get(likeClassifyIndex)
                            .getListItemByIndex(likeIndex);
                    //在DataSource中添加喜爱用户
                    WeakHashMap meInfoData = new WeakHashMap();
                    meInfoData.put("userName",DataSource.getInstance().meInfoData.userName.get());
                    meInfoData.put("avatarUrl",DataSource.getInstance().meInfoData.avatarUrl.get());
                    meInfoData.put("openId",DataSource.getInstance().meInfoData.openId);
                    meInfoData.put("insertTime",refFormatNowDate());
                    meInfoData.put("id",DataSource.getInstance().meInfoData.userId.get());
                    meInfoData.put("user_id",DataSource.getInstance().meInfoData.userId.get());
                    bindListItem.likeUserDatas.add(new BindLikeUser(meInfoData));

                    //后台更新喜爱用户数据
                    String refreshLikeIt = URLManager.getInstance().getRefreshLikeIt_url(bindListItem.id,
                            DataSource.getInstance().meInfoData.openId,
                            DataSource.getInstance().meInfoData.userName.get(),
                            DataSource.getInstance().meInfoData.avatarUrl.get());
                    queue.add(new StringRequest(refreshLikeIt,null,null));

                    //喜爱数加1
                    likeCountAddOne(likeIndex,likeClassifyIndex);
                }
                break;
            case 1016:          //点击"评论"按钮
                CommentIndex = (int) messageEvent.getEventWeakHashMap().get("index");
                CommentClassifyIndex = (int) messageEvent.getEventWeakHashMap().get("classifyIndex");
                type = 2;   //自动弹出评论框
                intent = new Intent(this, CommentDetail.class);
                intent.putExtra("index", CommentIndex);
                intent.putExtra("ClassifyIndex", CommentClassifyIndex);
                intent.putExtra("type",type);
                startActivityForResult(intent,3);
                break;
            case 1017:          //点击"分享"按钮
                int likeIndex = (int) messageEvent.getEventWeakHashMap().get("index");
                int likeClassifyIndex = (int) messageEvent.getEventWeakHashMap().get("classifyIndex");
                BindListItem bindListItemShare= DataSource.getInstance()
                        .classifyItemArrayList.get(likeClassifyIndex)
                        .getListItemByIndex(likeIndex);
                String content = bindListItemShare.content.get();
                String imageUrl = bindListItemShare.imageUrl.get();
                AndroidShare androidShare = new AndroidShare(this,content,imageUrl);
                androidShare.show();
                break;
            case 1018:          //跳转到原图
                String image_url =  messageEvent.getEventWeakHashMap().get("image_url").toString();
                Intent gotoSourceImageIntent = new Intent(this,SourceImage.class);
                gotoSourceImageIntent.putExtra("image_url",image_url);
                startActivity(gotoSourceImageIntent);
                break;
            case 1019:          //跳转到个人信息页面
                int user_id = (int)messageEvent.getEventWeakHashMap().get("user_id");
                Intent gotoUserInfoIntent = new Intent(this,UserInfoActivity.class);
                gotoUserInfoIntent.putExtra("user_id",user_id);
                startActivity(gotoUserInfoIntent);
                break;
            case 1010:          //订阅用户
                int publisher_id = (int)messageEvent.getEventWeakHashMap().get("publisher_id");        //当前发布者的id；
                int me_id = (int)messageEvent.getEventWeakHashMap().get("me_id");           //使用者id;
                if(DataSource.getInstance().meInfoData.hasLoad){
                    //当前操作储存起来
                    cache.clear();
                    cache.put("operator_id",3);     //1：点击“喜爱”按钮；2:点击分享；3：订阅
                    cache.put("publisher_id",publisher_id);
                    cache.put(" me_id", me_id);

                    //未登录则跳转到登录页面
                    Toast.makeText(this,"请先登录",Toast.LENGTH_LONG).show();
                    Intent loginIntent = new Intent(this,LoginActivity.class);
                    startActivityForResult(loginIntent,2);

                }else{
                    //更新DataSource中该发布者所有发布信息的关注状态；
                    for (ClassifyItem classifyItem:DataSource.getInstance().classifyItemArrayList) {
                        for(BindListItem bindListItem:classifyItem.bindListItems){
                            if(bindListItem.userId.get() == publisher_id){      //该用户已被订阅
                                bindListItem.hasConcern.set(true);
                            }
                        }
                    }
                    //后台记录订阅信息
                    String insertConcern = URLManager.getInstance()
                            .getConcernUrl(me_id,publisher_id
                            );
                    StringRequest stringRequest = new StringRequest(insertConcern, new Response.Listener<String>() {
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
                break;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
