package com.guangzhou.liuliang.appframework.Bind;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;
import android.widget.Toast;

import com.guangzhou.liuliang.appframework.Activity.MainActivity;
import com.guangzhou.liuliang.appframework.Activity.MainListActivity;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.WeakHashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yunhaipiaodi on 2016/5/15.
 */
public class MeInfoData extends UserInfoData {

    public String rongYunToken = "";
    public String openId = "";
    public String unionId = "";
    public boolean rongYunLoad = false;
    public boolean hasLoad =false;

    public final ObservableField<String> loadBtnText = new ObservableField<>("登陆");

    /*-------------------------------------------------------构建查询MeInfoData数据---------------------------------------------------------------------------------*/
    //空数据 MeInfoData
    public MeInfoData(Context context){
        //初始化值
        super(context);
        TRANS_TO_FANS_ACTIVITY = 1207;      //MeInfoData转入发布页面的情况不同，需要重新赋值
    }

    //有数据MeInfoData
    public MeInfoData(Context context,int user_id,boolean my_self,boolean hasLoad){
        super(context,user_id,my_self);
        this.hasLoad = hasLoad;
        if(hasLoad){
            loadBtnText.set("注销");
        }else{
            loadBtnText.set("登陆");
        }
        TRANS_TO_FANS_ACTIVITY = 1207;      //MeInfoData转入发布页面的情况不同，需要重新赋值
    }

    /*-------------------------------------------------------Data Binding,给layout资源文件调用---------------------------------------------------------------------------------*/

    @BindingAdapter("setImage")
    public static void setImageByUrl(CircleImageView circleImageView,String avatarImageUrl){
        if(avatarImageUrl != null)
        {
            if(!avatarImageUrl.isEmpty()){
                Picasso.with(circleImageView.getContext())
                        .load(avatarImageUrl)
                        .placeholder(R.drawable.bb_btn_account_unselect)
                        .error(R.drawable.bb_btn_account_unselect)
                        .into(circleImageView);
            }
            else{
                circleImageView.setImageResource(R.drawable.bb_btn_account_unselect);
            }
        }
        else{
            circleImageView.setImageResource(R.drawable.bb_btn_account_unselect);
        }

    }


   /* //点击"关注"数
    public void concernClick(View view){
        if(concernCount.get() == 0)
            return;
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("title","我 的关注");
        EventBus.getDefault().post(new MessageEvent(TRANS_TO_CONCERN_ACTIVITY,weakHashMap));
        return;
    }*/

 /*   //点击"粉丝"数
    public void fansClick(View view){
        if(fansCount.get() == 0)
            return;
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("title","我 的粉丝");
        EventBus.getDefault().post(new MessageEvent(TRANS_TO_FANS_ACTIVITY,weakHashMap));
        return;
    }*/

    /*//点击"喜爱"数
    public void likeItClick(View view){
        if(likeItCount.get() == 0)
            return;

        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("title","我 的喜爱");
        EventBus.getDefault().post(new MessageEvent(TRANS_TO_LIKE_IT_ACTIVITY,weakHashMap));
        return;
    }
*/
   /* //点击"发布"数
    public void publishClick(View view){
        if(publishCount.get() == 0)
            return;

        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("title","我 的发布");
        EventBus.getDefault().post(new MessageEvent(TRANS_TO_Publish_ACTIVITY,weakHashMap));
        return;
    }*/


    public void LoginBtnClick(View view){
        if(!hasLoad){
            EventBus.getDefault().post(new MessageEvent(TRANS_TO_LOGIN_ACTIVITY,null));
        }else{
            //清除数据
            userName.set("未登陆");
            avatarUrl.set("");
            loadBtnText.set("登陆");
            hasLoad =false;

            //重置本地储存数据
            SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("localUserInfo",0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("hasLoad",false);
            editor.putString("nickName","");
            editor.putString("avatarImageUrl","");
            editor.commit();

        }
    }

    //点击"发布"
    public void publishBtnClick(View view){
        if(!hasLoad){
            Toast.makeText(view.getContext(),"请先登录",Toast.LENGTH_SHORT).show();
            EventBus.getDefault().post(new MessageEvent(TRANS_TO_LOGIN_ACTIVITY,null));
        }else{
            EventBus.getDefault().post(new MessageEvent(TRANS_TO_Publish_ACTIVITY,null));
        }
    }
}
