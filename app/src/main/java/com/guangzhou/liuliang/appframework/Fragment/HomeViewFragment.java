package com.guangzhou.liuliang.appframework.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.Adapter.BannerAdapter;
import com.guangzhou.liuliang.appframework.Bind.ClassifyItem;
import com.guangzhou.liuliang.appframework.Bind.ClassifyItemData;
import com.guangzhou.liuliang.appframework.CustomComponent.Banner;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;

import java.util.ArrayList;

/**
 * Created by yunhaipiaodi on 2016/4/24.
 */
public class HomeViewFragment extends Fragment {

    ClassifyItemData classifyItemData;

    public static HomeViewFragment newInstance() {
        HomeViewFragment fragment = new HomeViewFragment();
        return fragment;
    }

    long delayTime = 500;

    Handler handler;

    Runnable LOAD_DATA = new Runnable() {
        @Override
        public void run() {
            initData();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState){
        //绑定数据
        classifyItemData = ClassifyItemData.inflate(inflater,parent,false);

        View view = classifyItemData.getRoot();

        //延迟加载数据，避免客户只是无意划过界面加载数据，导致内存无用损耗
        handler.postDelayed(LOAD_DATA,delayTime);
        //获得广告banner,并初始化
        Banner banner = (Banner)view.findViewById(R.id.banner_view);
        banner.setAdapter(new BannerAdapter(getActivity().getSupportFragmentManager(), DataSource.getInstance().bannerItemDataArrayList));

        return view;
    }

    //加载数据
    private void initData(){
        ArrayList<ClassifyItem> itemArrayList = DataSource.getInstance().classifyItemArrayList;
        classifyItemData.setDataArray(itemArrayList);
    }

    public void removeDelayTask(){
        handler.removeCallbacks(LOAD_DATA);
    }

}
