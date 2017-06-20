package com.guangzhou.liuliang.appframework.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.Activity.MainActivity;
import com.guangzhou.liuliang.appframework.Adapter.NoticeAdapter;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemDataArray;
import com.guangzhou.liuliang.appframework.R;

import java.util.ArrayList;

/**
 * Created by yunhaipiaodi on 2016/4/24.
 */
public class NoticeFragment extends Fragment {

    public static NoticeFragment getInstance(){
        return new NoticeFragment();
    }

    private RecyclerView recyclerView ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_notice,parent,false);
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView_notice);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new NoticeAdapter());
        return view;
    }

}
