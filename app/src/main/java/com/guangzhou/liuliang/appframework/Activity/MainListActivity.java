package com.guangzhou.liuliang.appframework.Activity;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Adapter.MainListViewPagerAdapter;
import com.guangzhou.liuliang.appframework.Bind.BindLikeUser;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.ClassifyItem;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemDataArray;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.Fragment.MainListFragment;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.GlobalData.UriCreator;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.Share.AndroidShare;
import com.guangzhou.liuliang.appframework.URL.URLManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.WeakHashMap;

import io.rong.imlib.model.UserInfo;

public class MainListActivity extends AppCompatActivity {

    private int classifyIndex = 0;                              //当前分类序号，同时也是ViewPager的子项序号

    public static EventBus eventBus = EventBus.getDefault();    //Event Bus 事件管理插件，详情参见:https://github.com/greenrobot/EventBus/

    RequestQueue queue;                                         //Volley框架,请求队列

    private WeakHashMap cache = new WeakHashMap();

    ViewPager viewPager;

    FragmentManager fragmentManager;

    Snackbar snackbar;                                       //SnackBar信息提示框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        setTitle("精彩内容");                                         //设置标题

        queue = Volley.newRequestQueue(this);                        //初始化Volley请求队列

        classifyIndex = getIntent().getIntExtra("Index", 0);       //获得分类序号

       /* WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);*/

        //设置ViewPager
        setViewPager();

        //显示返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //初始化SnackBar
        CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.coord_layout);
        snackbar = Snackbar.make(layout,"提示",Snackbar.LENGTH_LONG);

        //设置退出动画
        //setExitAnimation();
    }

    private void setViewPager(){
        viewPager = (ViewPager) findViewById(R.id.viewPagerMainList);
        fragmentManager = getSupportFragmentManager();
        final MainListViewPagerAdapter  mainListViewPagerAdapter = new MainListViewPagerAdapter(fragmentManager);
        viewPager.setAdapter(mainListViewPagerAdapter);
        //根据序号跳转到相应分类页
        viewPager.setCurrentItem(classifyIndex);

        viewPager.setOffscreenPageLimit(3);

        //跳过停留不超过一定时间的fragment数据加载
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for(int i=0;i< mainListViewPagerAdapter.fragments.size();i++){
                    if(i!=position){
                        MainListFragment mainListFragment =  mainListViewPagerAdapter.fragments.get(position);
                        if(mainListFragment != null){
                            mainListFragment.removeDelayTask();
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

   /* private void setExitAnimation(){
        if(Build.VERSION.SDK_INT >= 21){
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.LEFT);
            slide.setDuration(500);
            getWindow().setExitTransition(slide);
        }
    }*/

    //从上一个界面返回的事件处理
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
                            requestUrl(refreshLikeIt);
                            //喜爱数加1
                            likeCountAddOne(likeIndex,likeClassifyIndex);
                            break;
                        }

                        //更新数据
                        upLoadData();
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
                refresh();
                break;
        }

    }


    private void upLoadData(){
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

    //更新所有页面数据
    private void refresh(){
        for(int i = 0; i < viewPager.getAdapter().getCount();i++){
            Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, i);
            View view = fragment.getView();
            if(view != null){
                RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recyclerMainList);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
            else{
                Log.d("MainListActivity","view为空");
            }
        }

    }

    //获取当前时间
    public String refFormatNowDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String retStrFormatNowDate = sdFormatter.format(nowTime);
        return retStrFormatNowDate;
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
                    requestUrl(refreshLikeIt);

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
                if(!DataSource.getInstance().meInfoData.hasLoad){
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
            case 1206:          //有融云信息来，弹出SnackBar提示
                final int msg_type = Integer.parseInt(messageEvent.getEventWeakHashMap().get("type").toString());
                final String sendUserId = messageEvent.getEventWeakHashMap().get("sendUserId").toString();
                final String targetId = messageEvent.getEventWeakHashMap().get("targetId").toString();
                String name= "";
                if(DataSource.getInstance().RongUserInfo.containsKey(sendUserId)){
                    UserInfo userInfo = DataSource.getInstance().RongUserInfo.get(sendUserId);
                    name = userInfo.getName();
                    final String sendUserName = name;

                    String snackBarMessage = sendUserName + " 发来一条信息,点击'详情'查看";
                    snackbar.setText(snackBarMessage);
                    snackbar.setAction("详情", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //将当前点击的聊天未读数从总未读数中减去
                            int send_user_id = Integer.parseInt(sendUserId);
                            NoticeItemDataArray.getInstance().MessageArray.get(send_user_id).clearUnReadMessageCounts();

                            //系统推送和融云推送的Uri结构是不同的，所以要不同架构
                            Uri uri = null;
                            UriCreator uriCreator = new UriCreator(MainListActivity.this);
                            switch (msg_type){
                                case 1:
                                    uri = uriCreator.getSystemNoticeActivityUri(false);
                                    break;
                                case 2:
                                    uri = uriCreator.getPublishListActivityUri(false);
                                    break;
                                case 3:
                                    uri = uriCreator.getRongYunPrivateUri(targetId,sendUserName,false);
                                    break;
                            }
                            Intent intent = new Intent(null,uri);
                            MainListActivity.this.startActivity(intent);
                        }
                    });
                    if(!snackbar.isShown()){
                        snackbar.show();
                    }
                }else{      //如果RongUserInfo未包含当前sendUserid,去后台查询
                    Log.d("MainListActivity","RongUserInfo未包含当前sendUserid:" + sendUserId);
                    String url = URLManager.getInstance().getUserInfoUrl(Integer.parseInt(sendUserId));

                    final String fId = sendUserId;
                    JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                int userId = response.getString("id").isEmpty()? 0 : response.getInt("id");
                                final String userName = response.getString("nick_name");
                                String avatarUrl = response.getString("avatar_url");

                                DataSource.getInstance()
                                        .RongUserInfo
                                        .put(fId,
                                                new UserInfo(String.valueOf(userId),
                                                        userName,Uri.parse(avatarUrl)));

                                String snackBarMessage = userName + " 发来一条信息,点击'详情'查看";
                                snackbar.setText(snackBarMessage);
                                snackbar.setAction("详情", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //将当前点击的聊天未读数从总未读数中减去
                                        int send_user_id = Integer.parseInt(sendUserId);
                                        NoticeItemDataArray.getInstance().MessageArray.get(send_user_id).clearUnReadMessageCounts();

                                        //跳转到相应用户聊天界面
                                        Uri uri = null;
                                        UriCreator uriCreator = new UriCreator(MainListActivity.this);
                                        switch (msg_type){
                                            case 1:
                                                uri = uriCreator.getSystemNoticeActivityUri(false);
                                                break;
                                            case 2:
                                                uri = uriCreator.getPublishListActivityUri(false);
                                                break;
                                            case 3:
                                                uri = uriCreator.getRongYunPrivateUri(targetId,userName,false);
                                                break;
                                        }
                                        Intent intent = new Intent(null,uri);
                                        MainListActivity.this.startActivity(intent);
                                    }
                                });
                                if(!snackbar.isShown()){
                                    snackbar.show();
                                }
                            }catch (Exception e){
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

        break;
        }

    }

    private void requestUrl(String url) {
        //新建String请求
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Toast.makeText(MainListActivity.this,
                        "后台更新数据失败，错误原因：" + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
        queue.add(request);
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventBus.register(this);

    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        eventBus.unregister(this);
        super.onStop();

    }
}
