package com.guangzhou.liuliang.appframework.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.guangzhou.liuliang.appframework.Adapter.ConcernUserAdapter;
import com.guangzhou.liuliang.appframework.Bind.UserInfoData;
import com.guangzhou.liuliang.appframework.Bind.UserInfoListItem;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class ConcernAndFansUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concern_user);

        //根据传过来的信息设置页面
        ArrayList<UserInfoListItem> userInfoData
                = (ArrayList<UserInfoListItem>) getIntent().getSerializableExtra("userInfoData");          //获得上个页面传过来的UserInfoData类序列组流
        int type = getIntent().getIntExtra("type",0);
        String title = getIntent().getStringExtra("title");
        //设置标题
        setTitle(title);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.concern_detail_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(new ConcernUserAdapter(userInfoData ,type));

    }

    //EventBus 事件接收处理
    @Subscribe
    public void onMessageEvent(MessageEvent messageEvent) {
        int code = messageEvent.getEventCode();
        switch (code) {
            case 1019:  //点击item项，跳转到用户界面
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
