package com.guangzhou.liuliang.appframework.Adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guangzhou.liuliang.appframework.BR;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemData;
import com.guangzhou.liuliang.appframework.Bind.NoticeItemDataArray;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.Bind.ItemNotice;
/**
 * Created by yunhaipiaodi on 2016/5/4.
 */
public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder>{
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //倒序显示
        int count = NoticeItemDataArray.getInstance().userIdArray.size();
        NoticeItemData data = NoticeItemDataArray.getInstance().getDataByIndex(count - 1 - position);
        holder.binding.setVariable(BR.noticeData,data);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        int count = NoticeItemDataArray.getInstance().userIdArray.size();
        Log.d("NoticeAdapter","count:" + count);
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ItemNotice binding;
        public ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

}
