package com.guangzhou.liuliang.appframework.Adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.BR;
import com.guangzhou.liuliang.appframework.Bind.BindCommentData;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.MainListItem;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;

import java.util.ArrayList;

/**
 * Created by yunhaipiaodi on 2016/4/28.
 */
public class MainListRecyclerAdapter extends RecyclerView.Adapter<MainListRecyclerAdapter.ViewHolder> {

    private int index = 0;
    private Context context;


    public MainListRecyclerAdapter(int mIndex,Context context){
        index = mIndex;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_reyclerview_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
            BindListItem bindListItem = DataSource.getInstance().classifyItemArrayList.get(index).getListItemByIndex(position);
            final ArrayList<BindCommentData> CommentArray = DataSource.getInstance().classifyItemArrayList.get(index).getListItemByIndex(position).bindCommentDatas;
            holder.getBinding().setVariable(BR.ListItemData,bindListItem);
            holder.getBinding().setVariable(BR.StubCommentData,CommentArray);
            holder.getBinding().executePendingBindings();
            holder.position = position;
    }


    @Override
    public int getItemCount() {
        int count = DataSource.getInstance().classifyItemArrayList.get(index).bindListItems.size();
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private  MainListItem binding;
        public int position = 0;
        public ViewHolder(View itemView){
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

    public MainListItem getBinding(){return this.binding;}

    }
}
