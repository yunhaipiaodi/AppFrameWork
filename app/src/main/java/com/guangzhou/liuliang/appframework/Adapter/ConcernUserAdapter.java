package com.guangzhou.liuliang.appframework.Adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guangzhou.liuliang.appframework.BR;
import com.guangzhou.liuliang.appframework.Bind.ConcernUserDataBinding;
import com.guangzhou.liuliang.appframework.Bind.UserInfoListItem;
import com.guangzhou.liuliang.appframework.R;

import java.util.ArrayList;

/**
 * Created by yunhaipiaodi on 2016/7/4.
 */
public class ConcernUserAdapter extends RecyclerView.Adapter<ConcernUserAdapter.ViewHolder>  {

    private int type = 0;
    ArrayList<UserInfoListItem> userInfoList;
    public ConcernUserAdapter(ArrayList<UserInfoListItem> userInfoDataList, int type){
        userInfoList = userInfoDataList;
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.concern_user_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserInfoListItem bindConcernUser = userInfoList.get(position);
        holder.concernUserDataBinding.setVariable(BR.ConcernData,bindConcernUser);
        holder.concernUserDataBinding.executePendingBindings();
        View view = holder.itemView;
        TextView textView = (TextView)view.findViewById(R.id.describe);
        switch(type){
            case 1:
                textView.setText("关注了他/她");
                break;
            case 2:
                textView.setText("是他/她的粉丝");
                break;
        }
    }

    @Override
    public int getItemCount() {
      return userInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ConcernUserDataBinding concernUserDataBinding;
        public ViewHolder(View itemView) {
            super(itemView);
            concernUserDataBinding = DataBindingUtil.bind(itemView);
        }

    }
}
