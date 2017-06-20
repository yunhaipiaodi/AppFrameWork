package com.guangzhou.liuliang.appframework.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.Bind.MeInfoData;
import com.guangzhou.liuliang.appframework.Bind.MyInfo;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;

/**
 * Created by yunhaipiaodi on 2016/4/24.
 */
public class MeInfoFragment extends Fragment {

    public static MeInfoFragment newInstance() {
        MeInfoFragment fragment = new MeInfoFragment();
        return fragment;
    }

    MyInfo binding;

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
        binding = MyInfo.inflate(inflater,parent,false);
        //延迟加载数据，避免客户只是无意划过界面加载数据，导致内存无用损耗
        handler.postDelayed(LOAD_DATA,delayTime);
        View view = binding.getRoot();
        return view;
    }

    //加载数据
    private void initData(){
        MeInfoData meInfoData = DataSource.getInstance().meInfoData;
        binding.setMeInfo(meInfoData);
    }

    public void removeDelayTask(){
        handler.removeCallbacks(LOAD_DATA);
    }
}
