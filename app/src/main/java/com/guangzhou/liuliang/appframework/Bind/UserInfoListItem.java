package com.guangzhou.liuliang.appframework.Bind;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;

import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.WeakHashMap;

/**
 * Created by yunhaipiaodi on 2016/7/1.
 */
public class UserInfoListItem implements Serializable{
    public final ObservableInt id = new ObservableInt();
    public final ObservableField<String> userNickName =  new ObservableField<>();
    public final ObservableField<String> userAvatarUrl = new ObservableField<>();

    private int USER_INFO_CLICK=1019;

    public UserInfoListItem(int id,String userNickName,String userAvatarUrl){
        this.id.set(id);
        this.userAvatarUrl.set(userAvatarUrl);
        this.userNickName.set(userNickName);
    }

    //点击跳转到用户界面
    public void onClick(View view){
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("user_id",id.get());
        EventBus.getDefault().post(new MessageEvent(USER_INFO_CLICK,weakHashMap));
    }
}
