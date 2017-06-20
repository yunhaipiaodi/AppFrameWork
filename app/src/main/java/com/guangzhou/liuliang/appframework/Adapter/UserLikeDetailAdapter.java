package com.guangzhou.liuliang.appframework.Adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.Bind.BindCommentData;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.MainListItem;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.BR;

import java.util.ArrayList;

/**
 * Created by yunhaipiaodi on 2016/7/5.
 */
public class UserLikeDetailAdapter extends RecyclerView.Adapter<UserLikeDetailAdapter.ViewHolder> {

    ArrayList<BindListItem> listItem;

    public UserLikeDetailAdapter(ArrayList<BindListItem> listItem){
        this.listItem = listItem;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_reyclerview_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BindListItem bindListItem = listItem.get(position);
        final ArrayList<BindCommentData> CommentArray = listItem.get(position).bindCommentDatas;
        holder.getBinding().setVariable(BR.ListItemData,bindListItem);
        holder.getBinding().setVariable(BR.StubCommentData,CommentArray);
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        int count = listItem.size();
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private MainListItem binding;
        public ViewHolder(View itemView){
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public MainListItem getBinding(){return this.binding;}

    }
}
