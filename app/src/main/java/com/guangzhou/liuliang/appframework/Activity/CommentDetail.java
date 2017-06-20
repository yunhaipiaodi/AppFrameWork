package com.guangzhou.liuliang.appframework.Activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Adapter.CommentDetailAdapter;
import com.guangzhou.liuliang.appframework.Bind.BindCommentData;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.MeInfoData;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.WeakHashMap;

public class CommentDetail extends AppCompatActivity {

    RequestQueue queue;         //Volley 框架,请求队列

    String content = "";

    int index = 0;

    int classifyIndex = 0;

    RecyclerView recyclerView;

    EditText editText;

    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_detail);

        setTitle("评论");             //设置标题

        queue = Volley.newRequestQueue(this);       //初始化Volley请求队列

        //获得上个activity传过来的参数
        index = getIntent().getIntExtra("index",0);
        classifyIndex = getIntent().getIntExtra("ClassifyIndex",0);
        int type = getIntent().getIntExtra("type",1);

        //加载评论数据
        recyclerView = (RecyclerView)findViewById(R.id.CommentRecyclerView);
        recyclerView.setAdapter(new CommentDetailAdapter(classifyIndex,index));
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        //根据参数判定是否自动弹出键盘
        editText = (EditText)findViewById(R.id.edit_comment);
        if(type == 2){
            editText.requestFocus();
        }
        else{
            editText.setHint("输入你的精彩评论");
        }

        //提交评论
        Button button = (Button)findViewById(R.id.button_comment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment_content = editText.getText().toString();
                content = comment_content;
                if(!DataSource.getInstance().meInfoData.hasLoad){
                    //未登录则跳转到登录页面
                    Toast.makeText(CommentDetail.this,"请先登录",Toast.LENGTH_LONG).show();
                    Intent loginIntent = new Intent(CommentDetail.this,LoginActivity.class);
                    startActivityForResult(loginIntent,2);
                }else {
                    if(!comment_content.isEmpty()){             //内容不为空才提交评论
                        commitComment(comment_content);
                    }
                    else{
                        Toast.makeText(CommentDetail.this,"请输入你的评论",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    //提交评论
    private void commitComment(String comment_content){
        //更新后台数据
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("comment_detail",comment_content);

        weakHashMap.put("user_name", DataSource.getInstance().meInfoData.userName.get());
        weakHashMap.put("avatar_url",DataSource.getInstance().meInfoData.avatarUrl.get());
        weakHashMap.put("open_id",DataSource.getInstance().meInfoData.openId);
        weakHashMap.put("union_id",DataSource.getInstance().meInfoData.unionId);

        //获得当前内容的ID
        int id = DataSource.getInstance().classifyItemArrayList.get(classifyIndex).getListItemByIndex(index).id;
        weakHashMap.put("id",id);

        //请求网络，提交评论
        String url = URLManager.getInstance().getCommit_comment_url(weakHashMap);
        requestUrl(url);
    }

    //登陆成功后，继续提交评论结果
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        if(DataSource.getInstance().meInfoData.hasLoad){
            commitComment(content);
        }
    }

    //刷新数据，以达到界面刷新的效果
    private void refreshDataSource(){
        //更新DataSource数据和当前界面数据
        WeakHashMap newComment = new WeakHashMap();
        int id = DataSource.getInstance().classifyItemArrayList.get(classifyIndex).getListItemByIndex(index).id;
        newComment.put("id",id);
        newComment.put("user_id", DataSource.getInstance().meInfoData.userId.get());
        newComment.put("content",content);
        newComment.put("userName", DataSource.getInstance().meInfoData.userName.get());
        newComment.put("avatarUrl",DataSource.getInstance().meInfoData.avatarUrl.get());
        newComment.put("insertTime",refFormatNowDate());
        BindListItem bindListItem = DataSource.getInstance().classifyItemArrayList
                .get(classifyIndex).getListItemByIndex(index);

        bindListItem.bindCommentDatas.add(new BindCommentData(newComment));

        //更新数据
        recyclerView.getAdapter().notifyDataSetChanged();

        //跳转到最后一项
        linearLayoutManager.smoothScrollToPosition(recyclerView,null,recyclerView.getAdapter().getItemCount() - 1);

        //隐藏软键盘
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        //editText设置内容为空
       editText.setText("");

        //评论数加一
        int curCommentCount = bindListItem.commentCount.get();
        bindListItem.commentCount.set(curCommentCount + 1);
    }

    //获取当前时间
    public String refFormatNowDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String retStrFormatNowDate = sdFormatter.format(nowTime);
        return retStrFormatNowDate;
    }


    //请求网络，上传评论数据
    private void requestUrl(String url) {
        //新建String请求
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //返回上一页面
                        refreshDataSource();
                        Intent intent = new Intent();
                        intent.putExtra("result",1);
                        CommentDetail.this.setResult(100,intent);
                       // CommentDetail.this.finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Toast.makeText(CommentDetail.this,
                        "后台更新评论数据失败，错误原因：" + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
        queue.add(request);
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
