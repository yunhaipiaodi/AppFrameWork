package com.guangzhou.liuliang.appframework.Bind;

import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.guangzhou.liuliang.appframework.Activity.MainActivity;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * Created by yunhaipiaodi on 2016/4/23.
 */
public class ClassifyItem {
    public final ObservableInt tabId = new ObservableInt();
    public final ObservableField<String> classifyName = new ObservableField<>();
    public final ObservableField<String> classifyImageUrl = new ObservableField<>();
    public final ObservableArrayList<BindListItem> bindListItems = new ObservableArrayList<>();
    private int CLASSIFY_ITEM_CLICK = 1003;
    int index = 0;

    public ClassifyItem(WeakHashMap weakHashMap){
        classifyName.set(weakHashMap.get("classify_name").toString());
        classifyImageUrl.set(weakHashMap.get("classify_image").toString());
        index = (int)weakHashMap.get("index");
        tabId.set((int)weakHashMap.get("id"));
    }

    public void addBindListItems(ArrayList<BindListItem> bindListItemArrayList){
        for(BindListItem item : bindListItemArrayList){
            bindListItems.add(item);
        }
    }

    @BindingAdapter("set_image")
    public static void setImage(ImageView imageView,String classifyImageUrl){
        if(classifyImageUrl !=null){
            if(!classifyImageUrl.isEmpty()){
                Picasso.with(imageView.getContext())
                        .load(classifyImageUrl)
                        .error(R.drawable.loading_error)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {

                            }
                        });
            }
        }
    }

    //点击事件
    public void onClickEvent(View view){
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("index",index);
        MainActivity.eventBus.post(new MessageEvent(CLASSIFY_ITEM_CLICK,weakHashMap));
    }

    //获取相应BindListItem
    public BindListItem getListItemByIndex(int index){
        return bindListItems.get(index);
    }
}
