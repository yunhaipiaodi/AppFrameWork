package com.guangzhou.liuliang.appframework.Bind;

import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;

import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.InterruptedIOException;
import java.io.Serializable;
import java.util.WeakHashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yunhaipiaodi on 2016/4/30.
 */
public class BindLikeUser implements Serializable {
    public final ObservableInt id = new ObservableInt();
    public final ObservableInt userId = new ObservableInt();
    public final ObservableField<String> userName = new ObservableField<>();
    public final ObservableField<String> avatarUrl = new ObservableField<>();
    public final ObservableField<String> openId = new ObservableField<>();
    public final ObservableField<String> insertTime = new ObservableField<>();

    private int USER_INFO_CLICK=1019;

    public BindLikeUser(WeakHashMap weakHashMap){
        id.set(Integer.parseInt(weakHashMap.get("id").toString()));
        String user_id = weakHashMap.get("user_id").toString();
        userId.set(Integer.parseInt(user_id.equals("")?"0":user_id));
        userName.set(weakHashMap.get("userName").toString());
        avatarUrl.set(weakHashMap.get("avatarUrl").toString());
        openId.set(weakHashMap.get("openId").toString());
        insertTime.set(weakHashMap.get("insertTime").toString());
    }

    @BindingAdapter("set_image")
    public static void setImage(CircleImageView circleImageView, String avatarUrl){
        if(!avatarUrl.isEmpty()){
            Picasso.with(circleImageView.getContext())
                    .load(avatarUrl)
                    .into(circleImageView);
        }
    }

    public void transToUserInfo(View view){
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("user_id",userId.get());
        EventBus.getDefault().post(new MessageEvent(USER_INFO_CLICK,weakHashMap));
    }
}
