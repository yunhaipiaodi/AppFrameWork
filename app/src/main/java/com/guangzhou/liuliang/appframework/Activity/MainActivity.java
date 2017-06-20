package com.guangzhou.liuliang.appframework.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Adapter.ContentViewPagerAdapter;
import com.guangzhou.liuliang.appframework.Bind.MainTotalNotice;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemDataArray;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.Fragment.HomeViewFragment;
import com.guangzhou.liuliang.appframework.Fragment.MeInfoFragment;
import com.guangzhou.liuliang.appframework.Fragment.NoticeFragment;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.GlobalData.UriCreator;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.Service.NoticeService;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.guangzhou.liuliang.appframework.VersionUpdate.ApkDownload;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.List;
import java.util.WeakHashMap;

import io.rong.imlib.model.UserInfo;

public class MainActivity extends AppCompatActivity {

    //显示视图的ViewPager
    ViewPager viewPager;

    //切换视图的三个图片按钮
    ImageView iv_notice;
    ImageView iv_home;
    ImageView iv_userInfo;

    public static EventBus eventBus = EventBus.getDefault();      //Event Bus 事件管理插件，详情参见:https://github.com/greenrobot/EventBus/

    public static IWXAPI iwxapi;                                  //微信接口

    public static boolean noticeAdapterChanged = false;         //值为true的话，NoticeFragment里面的数据发生改变，需要刷新界面；false不用

    RequestQueue queue;                                             //Volley框架,请求队列

    int viewPagerItemPosition = 1;                              //viewPager当前Item位置，便于知道当前是否是NoticeFragment

    FragmentManager fragmentManager ;

    Snackbar snackbar;                                            //SnackBar信息提示框


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化界面，DataBinding写法
        MainTotalNotice binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        binding.setData(NoticeItemDataArray.getInstance());

        queue = Volley.newRequestQueue(this);               //初始化volley框架

        //启动后台service
        Intent serviceIntent = new Intent(this, NoticeService.class);
        startService(serviceIntent);

        //注册iwxapi
        String app_id = DataSource.getInstance().APP_ID;
        iwxapi = WXAPIFactory.createWXAPI(this,app_id,false);
        iwxapi.registerApp(app_id);


        /*--------获得组件实例----------*/
        viewPager = (ViewPager)findViewById(R.id.viewpager_content);
        iv_home = (ImageView)findViewById(R.id.imageView_home);
        iv_notice = (ImageView)findViewById(R.id.imageView_notice);
        iv_userInfo = (ImageView)findViewById(R.id.imageView_userInfo);

        //初始化SnackBar
        CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);
        snackbar = Snackbar.make(layout,"提示",Snackbar.LENGTH_LONG);

        fragmentManager = getSupportFragmentManager();                  //初始化fragmentManager

        //为viewPager设置管理，适配器
        setViewPager();

        //判断是否有更新
        checkUpdate();

    }

    public void setViewPager(){
        ContentViewPagerAdapter contentViewPagerAdapter = new ContentViewPagerAdapter(fragmentManager);
        viewPager.setAdapter(contentViewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position)
                {
                    case 0:
                        iv_notice.setImageResource(R.drawable.bell_middle);
                        iv_home.setImageResource(R.drawable.home_off);
                        iv_userInfo.setImageResource(R.drawable.people_off);
                        //设定标题
                        setTitle("提示信息");
                       /* //影藏红点
                        if(checkInflated != null){
                            viewStub.setVisibility(View.GONE);
                        }*/
                        break;
                    case 1:
                        iv_notice.setImageResource(R.drawable.bell_off);
                        iv_home.setImageResource(R.drawable.home_middle);
                        iv_userInfo.setImageResource(R.drawable.people_off);
                        //设定标题
                        setTitle("主页面");
                        break;
                    case 2:
                        iv_notice.setImageResource(R.drawable.bell_off);
                        iv_home.setImageResource(R.drawable.home_off);
                        iv_userInfo.setImageResource(R.drawable.people_middle);
                        //设定标题
                        setTitle("个人信息");
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {
                viewPagerItemPosition = position;
                NoticeFragment noticeFragment = null;
                HomeViewFragment homeViewFragment = null;
                MeInfoFragment meInfoFragment = null;


                switch(position){
                    case 0:
                        List<Fragment> list = fragmentManager.getFragments();
                        if(list != null){
                            for(Fragment fragment1 : list){
                                if(fragment1.getClass().getName().equals("com.guangzhou.liuliang.appframework.Fragment.NoticeFragment")){
                                    noticeFragment = (NoticeFragment)fragment1;
                                }else if(fragment1.getClass().getName().equals("com.guangzhou.liuliang.appframework.Fragment.HomeViewFragment")){
                                    homeViewFragment = (HomeViewFragment)fragment1;
                                }else if(fragment1.getClass().getName().equals("com.guangzhou.liuliang.appframework.Fragment.MeInfoFragment")){
                                    meInfoFragment = (MeInfoFragment)fragment1;
                                }

                            }
                        }

                        if(noticeAdapterChanged){
                            if(noticeFragment != null){
                                View view = noticeFragment.getView();
                                RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView_notice);
                                recyclerView.getAdapter().notifyDataSetChanged();
                                noticeAdapterChanged = false;
                            }else{
                                Log.d("MainActivity","fragment is null");
                            }
                        }

                        //停止其他两个fragment的数据加载任务
                        if(homeViewFragment != null){
                            homeViewFragment.removeDelayTask();
                        }
                        if(meInfoFragment != null){
                            meInfoFragment.removeDelayTask();
                        }
                        break;
                    case 1:
                        List<Fragment> list1 = fragmentManager.getFragments();
                        if(list1 != null){
                            for(Fragment fragment1 : list1){
                                if(fragment1.getClass().getName().equals("com.guangzhou.liuliang.appframework.Fragment.MeInfoFragment")){
                                    meInfoFragment = (MeInfoFragment)fragment1;
                                }
                            }
                        }
                        if(meInfoFragment != null){
                            meInfoFragment.removeDelayTask();
                        }
                        break;
                    case 2:
                        List<Fragment> list2 = fragmentManager.getFragments();
                        if(list2 != null){
                            for(Fragment fragment1 : list2){
                                if(fragment1.getClass().getName().equals("com.guangzhou.liuliang.appframework.Fragment.HomeViewFragment")){
                                    homeViewFragment = (HomeViewFragment)fragment1;
                                }
                            }
                        }
                        if(homeViewFragment != null){
                            homeViewFragment.removeDelayTask();
                        }
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //为三个图片按钮设定点击事件
        iv_notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
                iv_notice.setImageResource(R.drawable.bell_middle);
                iv_home.setImageResource(R.drawable.home_off);
                iv_userInfo.setImageResource(R.drawable.people_off);
                //设定标题
                setTitle("提示信息");

//                //隐藏红圈提示框
//                if(checkInflated != null){
//                    viewStub.setVisibility(View.GONE);
//                }
            }
        });

        iv_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transFragment(R.id.item_content, HomeViewFragment.newInstance());
                viewPager.setCurrentItem(1);
                iv_notice.setImageResource(R.drawable.bell_off);
                iv_home.setImageResource(R.drawable.home_middle);
                iv_userInfo.setImageResource(R.drawable.people_off);
                //设定标题
                setTitle("主要内容");
            }
        });

        iv_userInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transFragment(R.id.item_content, MeInfoFragment.newInstance());
                viewPager.setCurrentItem(2);
                iv_notice.setImageResource(R.drawable.bell_off);
                iv_home.setImageResource(R.drawable.home_off);
                iv_userInfo.setImageResource(R.drawable.people_middle);
                //设定标题
                setTitle("个人信息");
            }
        });

        //进入是默认页是home页面
        viewPager.setCurrentItem(1);
    }

    public void checkUpdate(){
        try{
            String pkName = this.getPackageName();
            String appVersionName = this.getPackageManager().getPackageInfo(pkName,0).versionName;
            if(!appVersionName.equals(DataSource.getInstance().versionData.versionName.get())){     //要更新，弹出更新提示框   ·1
                String message = DataSource.getInstance().versionData.updateContent.get();
                new AlertDialog.Builder(this).setTitle("更新")
                        .setMessage(message)
                        .setPositiveButton("取消",null)
                        .setNegativeButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //开始更新
                                String downLoadUrl = DataSource.getInstance().versionData.downloadUrl.get();
                                ApkDownload apkDownload = new ApkDownload(MainActivity.this,downLoadUrl);
                                apkDownload.startDownload();
                            }
                        })
                        .create()
                        .show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStart(){
        super.onStart();
        eventBus.register(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        eventBus.unregister(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    //event bus 事件接收
    @Subscribe
    public void onMessageEvent(MessageEvent event){
        int code = event.getEventCode();
        Log.d("MainActivity", "code:" + code);
        switch (code){
            case 1002:              //点击广告页，根据索引跳转到相应的广告页面
                WeakHashMap weakHashMap = event.getEventWeakHashMap();
                String url =weakHashMap.get("url").toString();
                transToBannerActivity(url);
                break;
            case 1003:              //点击分类框，根据索引跳转到相应的内容页面
                WeakHashMap weakHashMapClassify = event.getEventWeakHashMap();
                int ClassifyIndex =(int)weakHashMapClassify.get("index");
                Intent MainListIntent = new Intent(this,MainListActivity.class);
                MainListIntent.putExtra("Index",ClassifyIndex);
                startActivity(MainListIntent);
                break;
            case 1201:              //点击跳转到微信登陆页面
                loginWeiXin();
                break;
            case 1202:             //转入"关注"详情页面
                Intent concernIntent = new Intent(this,ConcernAndFansUserActivity.class);
                String title = event.getEventWeakHashMap().get("title").toString();
                concernIntent.putExtra("title",title);
                concernIntent.putExtra("userInfoData",DataSource.getInstance().meInfoData.concernUserList);
                concernIntent.putExtra("type",1);
                startActivity(concernIntent);
                break;
            case 1203:              //转入"粉丝"详情页面
                Intent fansIntent = new Intent(this,ConcernAndFansUserActivity.class);
                title = event.getEventWeakHashMap().get("title").toString();
                fansIntent.putExtra("title",title);
                fansIntent.putExtra("userInfoData",DataSource.getInstance().meInfoData.fansUserList);
                fansIntent.putExtra("type",2);
                startActivity(fansIntent);
                break;
            case 1204:              //转入"喜爱"页面
                Intent likeItIntent = new Intent(this,ListItemActivity.class);
                title = event.getEventWeakHashMap().get("title").toString();
                likeItIntent.putExtra("title",title);
                //将喜爱数据缓存到DataSource,供UserLikeItDetail页面调用
                DataSource.getInstance().ListItemActivityCache = DataSource.getInstance().meInfoData.likeItList;
                startActivity(likeItIntent);
                break;
            case 1207:              //转入"发布"页面
                Intent publish_Intent = new Intent(this,ListItemActivity.class);
                title = event.getEventWeakHashMap().get("title").toString();
                publish_Intent.putExtra("title",title);
                //将喜爱数据缓存到DataSource,供UserLikeItDetail页面调用
                DataSource.getInstance().ListItemActivityCache = DataSource.getInstance().meInfoData.likeItList;
                startActivity( publish_Intent);
                break;
            case 1205:
                Intent publishIntent = new Intent(this,PublishActivity.class);
                startActivity(publishIntent);
                break;
            case 1206:
                //刷新提示界面
                if(viewPagerItemPosition == 0) {
                    List<Fragment> list = fragmentManager.getFragments();
                    Log.d("MainActivity", "list size:" + list.size());
                    NoticeFragment fragment = null;
                    for (Fragment fragment1 : list) {
                        if (fragment1.getClass().getName().equals("com.guangzhou.liuliang.appframework.Fragment.NoticeFragment")) {
                            fragment = (NoticeFragment) fragment1;
                        }
                    }

                    if (fragment != null) {
                        View view = fragment.getView();
                        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_notice);
                        recyclerView.getAdapter().notifyDataSetChanged();
                    } else {
                        noticeAdapterChanged = true;
                    }
                }else{
                    //弹出SnackBar
                    final int msg_type = Integer.parseInt(event.getEventWeakHashMap().get("type").toString());
                    final String sendUserId = event.getEventWeakHashMap().get("sendUserId").toString();
                    final String targetId = event.getEventWeakHashMap().get("targetId").toString();
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
                                UriCreator uriCreator = new UriCreator(MainActivity.this);
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
                                MainActivity.this.startActivity(intent);
                            }
                        });
                        if(!snackbar.isShown()){
                            snackbar.show();
                        }
                    }else{      //如果RongUserInfo未包含当前sendUserid,去后台查询
                        Log.d("MainListActivity","RongUserInfo未包含当前sendUserid:" + sendUserId);
                        String url_user_info = URLManager.getInstance().getUserInfoUrl(Integer.parseInt(sendUserId));

                        final String sendUserName = name;
                        final String fId = sendUserId;
                        JsonObjectRequest request = new JsonObjectRequest(url_user_info, new Response.Listener<JSONObject>() {
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

                                            Uri uri = null;
                                            UriCreator uriCreator = new UriCreator(MainActivity.this);
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
                                            MainActivity.this.startActivity(intent);
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
                    //当前不在notice fragment,不能刷新，只能将noticeAdapterChanged设为true，延迟刷新
                    noticeAdapterChanged = true;
                }
                break;
            case 1220:          //Notice消息中心发来的event
                String sender_Name = event.getEventWeakHashMap().containsKey("userName")?(String)event.getEventWeakHashMap().get("userName"):"";
                final Uri target_Uri = event.getEventWeakHashMap().containsKey("targetUri")?(Uri)event.getEventWeakHashMap().get("targetUri"):null;
                snackbar.setText(sender_Name + " 发来一条信息,点击'详情'查看");
                snackbar.setAction("详情", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(null,target_Uri);
                        MainActivity.this.startActivity(intent);
                    }
                });
                break;
        }

    }

    //登陆
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

    //跳转到广告页面
    private void transToBannerActivity(String url){
        Intent intent = new Intent(this,BannerActivity.class);
        intent.putExtra("url",url);
        startActivity(intent);
    }



}
