package com.guangzhou.liuliang.appframework.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.guangzhou.liuliang.appframework.Adapter.LikeUserAdapter;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class LikeUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_user);

        //设置标题
        setTitle("喜爱用户");

        //显示返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int index = getIntent().getIntExtra("index",0);
        int classifyIndex = getIntent().getIntExtra("ClassifyIndex",0);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.LikeUserRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new LikeUserAdapter(classifyIndex,index));
    }

    //EventBus 事件接收处理
    @Subscribe
    public void onMessageEvent(MessageEvent messageEvent) {
        int code = messageEvent.getEventCode();
        Log.d("MainListActivity", "code:" + code);
        switch (code) {
            case 1019:          //跳转到个人信息页面
                int user_id = (int)messageEvent.getEventWeakHashMap().get("user_id");
                Intent gotoUserInfoIntent = new Intent(this,UserInfoActivity.class);
                gotoUserInfoIntent.putExtra("user_id",user_id);
                startActivity(gotoUserInfoIntent);
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
