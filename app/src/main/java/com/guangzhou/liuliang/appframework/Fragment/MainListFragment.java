package com.guangzhou.liuliang.appframework.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Adapter.MainListRecyclerAdapter;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

/**
 * Created by yunhaipiaodi on 2016/4/28.
 */
public class MainListFragment extends Fragment {

    private static String PARAM1 = "index";
    int index = 0;

    SwipeRefreshLayout swipeRefreshLayout;

    RecyclerView recyclerView;

    //Volley框架
    RequestQueue queue;

    long delayTime = 1000;

    Handler handler;

    Runnable LOAD_DATA = new Runnable() {
        @Override
        public void run() {
            initData();
        }
    };

    MainListRecyclerAdapter mainListRecyclerAdapte;

    //加载数据
    private void initData(){
        recyclerView.setAdapter( mainListRecyclerAdapte);
        recyclerView.setItemViewCacheSize(3);
    }

    public void removeDelayTask(){
        if(handler != null){
            handler.removeCallbacks(LOAD_DATA);
        }
    }

    static public MainListFragment getInstance(int position){
        MainListFragment instance = new MainListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM1,position);
        instance.setArguments(bundle);
        return instance;
    }

    //获得传递过来的参数
    private void getParams(){
        index = getArguments().getInt(PARAM1);
    }

    @Override
    public void onCreate(Bundle savedStateInstance){
        super.onCreate(savedStateInstance);
        getParams();

        //初始化volley框架
        queue = Volley.newRequestQueue(getContext());
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup parent, Bundle savedStateInstance){
        View view = inflater.inflate(R.layout.fragment_main_list,parent,false);
        //设置刷新
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        //加载recyclerView数据
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerMainList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(parent.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        mainListRecyclerAdapte = new MainListRecyclerAdapter(index,parent.getContext());
        //延迟加载数据，避免客户只是无意划过界面加载数据，导致内存无用损耗
        handler.postDelayed(LOAD_DATA,delayTime);

        final Context context = parent.getContext();
        recyclerView.setItemViewCacheSize(3);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    Picasso.with(context).resumeTag("tag");
                }
                else
                {
                    Picasso.with(context).pauseTag("tag");
                }
            }
        });

        return view;
    }

    private void refresh(){
        String refreshUrl = URLManager.getInstance().getLoading_url();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,refreshUrl,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //更新数据
                DataSource.getInstance().refreshData(response);
                //更新界面
                recyclerView.getAdapter().notifyDataSetChanged();
                //关闭刷新图标
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               error.printStackTrace();
            }
        });
        queue.add(jsonObjectRequest);
    }
}


