package com.guangzhou.liuliang.appframework.Adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.BR;
import com.guangzhou.liuliang.appframework.Bind.BindLikeUser;
import com.guangzhou.liuliang.appframework.Bind.LikeUserDataBind;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;

import java.util.ArrayList;

/**
 * Created by yunhaipiaodi on 2016/4/30.
 */
public class LikeUserAdapter extends RecyclerView.Adapter<LikeUserAdapter.ViewHolder> {

    private int classifyIndex = 0;
    private int index = 0;

    public LikeUserAdapter(int ClassifyIndex,int Index){
        this.classifyIndex = ClassifyIndex;
        this.index = Index;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.like_user_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ArrayList<BindLikeUser> likeUserArrayList = DataSource.getInstance()
                .classifyItemArrayList.get(classifyIndex)
                .getListItemByIndex(index).likeUserDatas;
        BindLikeUser bindLikeUser = likeUserArrayList.get(position);
        holder.likeUserDataBind.setVariable(BR.LikeData,bindLikeUser);
        holder.likeUserDataBind.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return DataSource.getInstance()
                .classifyItemArrayList.get(classifyIndex)
                .getListItemByIndex(index).likeUserCount.get();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private LikeUserDataBind likeUserDataBind;
        public ViewHolder(View itemView) {
            super(itemView);
            likeUserDataBind = DataBindingUtil.bind(itemView);
        }
        public LikeUserDataBind getBinding(){return this.likeUserDataBind;}
    }
}
