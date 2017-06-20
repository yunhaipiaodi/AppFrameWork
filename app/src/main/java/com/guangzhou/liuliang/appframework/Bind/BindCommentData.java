package com.guangzhou.liuliang.appframework.Bind;

import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.guangzhou.liuliang.appframework.Activity.MainListActivity;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.WeakHashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yunhaipiaodi on 2016/4/28.
 */
public class BindCommentData implements Serializable {

    //数据
    //public final ObservableInt commentCount = new ObservableInt();
    public final  ObservableInt id = new ObservableInt();
    public final ObservableInt user_id = new ObservableInt();
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<String> userName = new ObservableField<>();
    public final ObservableField<String> avatarUrl = new ObservableField<>();
    public final ObservableField<String> insertTime = new ObservableField<>();

    private int USER_INFO_CLICK=1019;

    public BindCommentData(WeakHashMap weakHashMap){
        id.set((int)weakHashMap.get("id"));
        user_id.set((int)weakHashMap.get("user_id"));
        content.set(weakHashMap.get("content").toString());
        userName.set(weakHashMap.get("userName").toString());
        avatarUrl.set(weakHashMap.get("avatarUrl").toString());
        insertTime.set(weakHashMap.get("insertTime").toString());

    }

    @BindingAdapter("set_nickname")
    public static void setNickName(TextView textView,String userName){
        textView.setText(userName + ": ");
    }

    @BindingAdapter("set_image")
    public static void setImage(CircleImageView circleImageView,String avatarUrl){
        if(!avatarUrl.isEmpty()){
            Picasso.with(circleImageView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.bb_btn_account_unselect)
                    .error(R.drawable.bb_btn_account_unselect)
                    .into(circleImageView);
        }
    }

    public void transToUserInfo(View view){
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("user_id",user_id.get());
        EventBus.getDefault().post(new MessageEvent(USER_INFO_CLICK,weakHashMap));
    }
}
