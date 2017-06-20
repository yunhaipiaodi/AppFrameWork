package com.guangzhou.liuliang.appframework.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;

import com.guangzhou.liuliang.appframework.Adapter.SystemNoticeAdapter;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;

import java.util.ArrayList;

public class SystemNoticeActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    Boolean isNotify  = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_notice);

        isNotify = getIntent().getData().getBooleanQueryParameter("isNotify",false);

        String title = getIntent().getData().getQueryParameter("title");

        if(!title.isEmpty()){
            setTitle(title);
        }

        ArrayList<String> list = DataSource.getInstance().systemNotices;
        recyclerView = (RecyclerView)findViewById(R.id.system_notice_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SystemNoticeAdapter adapter = new SystemNoticeAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollToPosition(DataSource.getInstance().systemNotices.size() - 1);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("SystemNoticeActivity","onResume");
        if(recyclerView != null)
        recyclerView.getAdapter().notifyDataSetChanged();
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
}
