package com.guangzhou.liuliang.appframework.Adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.BR;
import com.guangzhou.liuliang.appframework.Bind.BindCommentData;
import com.guangzhou.liuliang.appframework.Bind.CommentDataBind;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;

/**
 * Created by yunhaipiaodi on 2016/4/30.
 */
public class CommentDetailAdapter extends RecyclerView.Adapter<CommentDetailAdapter.ViewHolder> {

    private int classifyIndex = 0;
    private int index = 0;
    public CommentDetailAdapter(int ClassifyIndex,int Index){
        this.classifyIndex = ClassifyIndex;
        this.index = Index;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.comment_detail_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BindCommentData bindCommentData = DataSource.getInstance()
                .classifyItemArrayList.get(classifyIndex)
                .getListItemByIndex(index).bindCommentDatas.get(position);
        holder.commentDataBind.setVariable(BR.CommentData,bindCommentData);
        holder.commentDataBind.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return DataSource.getInstance()
                .classifyItemArrayList.get(classifyIndex)
                .getListItemByIndex(index).bindCommentDatas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private CommentDataBind commentDataBind;
        public ViewHolder(View itemView) {
            super(itemView);
            commentDataBind = DataBindingUtil.bind(itemView);
        }
        public CommentDataBind getBinding(){return this.commentDataBind;}
    }
}
