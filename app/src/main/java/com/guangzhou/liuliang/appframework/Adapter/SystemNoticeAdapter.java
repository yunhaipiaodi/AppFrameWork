package com.guangzhou.liuliang.appframework.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;

/**
 * Created by yunhaipiaodi on 2016/9/2.
 */
public class SystemNoticeAdapter extends RecyclerView.Adapter<SystemNoticeAdapter.ViewHolder>  {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.system_notice_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView textView = (TextView)holder.itemView.findViewById(R.id.system_content);
        textView.setText(DataSource.getInstance().systemNotices.get(position));
    }

    @Override
    public int getItemCount() {
        int count = DataSource.getInstance().systemNotices.size();
        //Log.d("SystemNoticeAdapter","count:" + count);
        return DataSource.getInstance().systemNotices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView){
            super(itemView);
        }
    }
}
