package com.guangzhou.liuliang.appframework.Adapter;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.guangzhou.liuliang.appframework.Bind.ClassifyItem;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;

/**
 * Created by yunhaipiaodi on 2016/7/7.
 */
public class classifyItemAdapter implements SpinnerAdapter {
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(parent.getContext());
        ClassifyItem item= DataSource.getInstance().classifyItemArrayList.get(position);
        textView.setText(item.classifyName.get());
        textView.setTextSize(20);
        return textView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return  DataSource.getInstance().classifyItemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(parent.getContext());
        ClassifyItem item= DataSource.getInstance().classifyItemArrayList.get(position);
        textView.setText(item.classifyName.get());
        textView.setTextSize(20);
        return textView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
