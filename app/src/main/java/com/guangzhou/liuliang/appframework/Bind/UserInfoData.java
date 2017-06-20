package com.guangzhou.liuliang.appframework.Bind;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

/**
 * Created by yunhaipiaodi on 2016/6/20.
 */
public class UserInfoData implements Serializable {

    public final ObservableInt userId = new ObservableInt(0);                                    //该用户ID
    public final ObservableField<String> userName = new ObservableField<>("未知");              //该用户昵称
    public final ObservableField<String> avatarUrl = new ObservableField<>("");                 //该用户头像地址，url形式
    public final ObservableInt likeItCount = new ObservableInt(0);                              //该用户喜欢其他用户的数量
    public final ObservableInt concernCount = new ObservableInt();                              //关注该用户的其他用户数量
    public final ObservableInt fansCount = new ObservableInt();                                 //该用户的粉丝数量
    public final ObservableInt publishCount = new ObservableInt(0);                             //该用户发布圈子的数量
    public final ObservableInt sex = new ObservableInt(0);                                       //性别。0，未知；1，男性，2，女性

    public final ObservableBoolean hasConcern = new ObservableBoolean();                        //本机用户是否关注该用户.true,已关注；false,未关注，
                                                                                                    // (接上)如果该用户为本人，是不能关注自己的，必须为false

    public final ObservableBoolean mySelf = new ObservableBoolean(false);                       //该用户是否为本人

    public final ArrayList<UserInfoListItem> concernUserList = new ArrayList<>();              //该用户关注的用户详情集合
    public final ArrayList<UserInfoListItem> fansUserList = new ArrayList<>();                  //该用户的粉丝详情集合
    public final ArrayList<BindListItem> likeItList = new ArrayList<>();                        //该用户喜欢其他用户的详情集合
    public final ArrayList<BindListItem> PublishList = new ArrayList<>();                       //该用户发布的圈子详情集合

    //EventBus指令集
    protected int TRANS_TO_LOGIN_ACTIVITY = 1201;                                             //跳转到登陆页LoginActivity
    protected int TRANS_TO_CONCERN_ACTIVITY = 1202;                                           //跳转到关注用户页ConcernAndFansUserActivity
    protected int TRANS_TO_FANS_ACTIVITY = 1203;                                               //跳转到粉丝用户页ConcernAndFansUserActivity
    protected int TRANS_TO_LIKE_IT_ACTIVITY = 1204;                                           //跳转喜欢用户页ListItemActivity
    protected int TRANS_TO_Publish_ACTIVITY = 1205;                                           //跳转到发布页PublishActivity

    public RequestQueue queue;

    //空数据UserInfoData
    public UserInfoData(Context context){
        if(queue == null){
            queue = Volley.newRequestQueue(context);
        }
    }

    /*-------------------------------------------------------构建查询UserInfoData数据---------------------------------------------------------------------------------*/

    //有数据UserInfoData
    public UserInfoData(Context context,int user_id,boolean my_self){
        if(queue == null){
            queue = Volley.newRequestQueue(context);
        }

        userId.set(user_id);
        mySelf.set(my_self);

        //获取个人平台数据
        getMeInfoDataFromNet();


    }

    public void putUserInfoToRongyun(String userId,String userName,String avatarUrl){
        DataSource.getInstance()
                .RongUserInfo
                .put(userId,
                        new UserInfo(userId, userName,Uri.parse(avatarUrl)));
    }

    public void getMeInfoDataFromNet(){
        String url = URLManager.getInstance().getUserInfoUrl(userId.get());
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //取得性别
                    String user_name = response.has("nick_name")?response.get("nick_name").toString():"";
                    String avatar_url = response.has("avatar_url")?response.get("avatar_url").toString():"";
                    int sex =response.has("sex")?response.getString("sex").isEmpty() ? 0 : response.getInt("sex"):0;

                    //给类数据赋值
                    UserInfoData.this.userName.set(user_name);
                    UserInfoData.this.avatarUrl.set(avatar_url);
                    UserInfoData.this.sex.set(sex);

                    //往融云用户信息集RongUserInfo里添加数据
                    putUserInfoToRongyun(String.valueOf(UserInfoData.this.userId.get()),user_name,avatar_url);

                    //取得关注数据
                    JSONArray concernArray = response.getJSONArray("array_concern");
                    for (int i = 0; i < concernArray.length(); i++) {
                        JSONObject item = concernArray.getJSONObject(i);
                        int id = item.getInt("concern_id");
                        String userNickName = item.getString("concern_nick_name");
                        String userAvatarUrl = item.getString("concern_avatar_url");
                        addConcernUserListItem(new UserInfoListItem(id, userNickName, userAvatarUrl));
                        int concern_Count = concernCount.get();
                        concernCount.set(concern_Count + 1);
                    }
                    //取得粉丝数据
                    JSONArray fansArray = response.getJSONArray("array_fans");
                    for (int i = 0; i < fansArray.length(); i++) {
                        JSONObject item = fansArray.getJSONObject(i);
                        int id = item.getInt("fans_id");
                        String userNickName = item.getString("fans_nick_name");
                        String userAvatarUrl = item.getString("fans_avatar_url");
                        addFansUserListItem(new UserInfoListItem(id, userNickName, userAvatarUrl));
                        int fans_Count = fansCount.get();
                        fansCount.set(fans_Count + 1);
                    }
                    //取得喜爱项数据
                    JSONArray likeItArray = response.getJSONArray("array_like_it");
                    List<Integer> idArray = new ArrayList<Integer>();
                    for (int i = 0; i < likeItArray.length(); i++) {
                        JSONObject item = likeItArray.getJSONObject(i);
                        int id = item.getInt("tab_data_id");
                        idArray.add(id);
                        int likeIt_Count = likeItCount.get();
                        likeItCount.set(likeIt_Count + 1);
                    }
                    ArrayList<ClassifyItem> classifyItemArrayList = DataSource.getInstance().classifyItemArrayList;
                    for (ClassifyItem classifyItem : classifyItemArrayList) {
                        ArrayList<BindListItem> bindListItemArrayList = classifyItem.bindListItems;
                        for (BindListItem bindListItem : bindListItemArrayList) {
                            if (idArray.contains(bindListItem.id)) {
                                likeItList.add(bindListItem);
                                int likeIt_Count = likeItCount.get();
                                likeItCount.set(likeIt_Count + 1);
                            }
                        }
                    }

                    //获得发布数据
                    JSONArray publishArray = response.getJSONArray("array_publish_it");
                    idArray = new ArrayList<Integer>();
                    for (int i = 0; i < publishArray.length(); i++) {
                        JSONObject item = publishArray.getJSONObject(i);
                        int id = item.getInt("tab_data_id");
                        idArray.add(id);
                        int publish_Count = publishCount.get();
                        publishCount.set(publish_Count + 1);
                    }
                    classifyItemArrayList = DataSource.getInstance().classifyItemArrayList;
                    for (ClassifyItem classifyItem : classifyItemArrayList) {
                        ArrayList<BindListItem> bindListItemArrayList = classifyItem.bindListItems;
                        for (BindListItem bindListItem : bindListItemArrayList) {
                            if (idArray.contains(bindListItem.id)) {
                                PublishList.add(bindListItem);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                concernCount.set(0);
                likeItCount.set(0);
                fansCount.set(0);
            }
        });
        queue.add(request);
    }

    public void addConcernUserListItem(UserInfoListItem userInfoListItem){
        this.concernUserList.add(userInfoListItem);
    }

    public void addFansUserListItem(UserInfoListItem userInfoListItem){
        this.fansUserList.add(userInfoListItem);
    }

    /*-------------------------------------------------------Data Binding,给layout资源文件调用---------------------------------------------------------------------------------*/

    @BindingAdapter("set_image")
    public static void setImage(CircleImageView circleImageView,String avatarUrl){
        if(avatarUrl.isEmpty() || avatarUrl == null){
            //Log.d("UserInfoData","avatarUrl 有问题");
            circleImageView.setImageResource(R.drawable.bb_btn_account_unselect);
        }else{
            //Log.d("UserInfoData","avatarUrl：" + avatarUrl);
            Picasso.with(circleImageView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.bb_btn_account_unselect)
                    .error(R.drawable.bb_btn_account_unselect)
                    .into(circleImageView);
        }
    }

    @BindingAdapter("set_background")
    public static void setBackground(Button button, boolean hasConcern){
        if(hasConcern){
            button.setBackgroundResource(R.drawable.no_operator);
            button.setText("已关注");
            if(Build.VERSION.SDK_INT >=23){
                button.setTextColor(button.getContext().getColor(R.color.gray));
            }
            else{
                button.setTextColor(button.getContext().getResources().getColor(R.color.gray));
            }
        }
    }

    //点击“关注”按钮
    public void concernBtnClick(View view){
        //是否已经关注
        if(hasConcern.get()){
            Toast.makeText(view.getContext(),"已经关注了",Toast.LENGTH_SHORT).show();
        }

        //判断是否登陆,没有则跳转到登陆界面
        if(DataSource.getInstance().meInfoData.hasLoad){
            WeakHashMap weakHashMap = new WeakHashMap();
            weakHashMap.put("concern_id",userId.get());
            EventBus.getDefault().post(new MessageEvent(TRANS_TO_LOGIN_ACTIVITY,weakHashMap));
            return;
        }

        //关注对象粉丝数+1
        addFansUserListItem(new UserInfoListItem( DataSource.getInstance()
                .meInfoData.userId.get(),
                DataSource.getInstance().meInfoData.userName.get(),
                DataSource.getInstance().meInfoData.avatarUrl.get()));

        //自己的关注数+1
        DataSource.getInstance()
                .meInfoData
                .concernCount.set(DataSource.getInstance()
                .meInfoData
                .concernCount.get() + 1);

        //更新DataSource中该发布者所有发布信息的关注状态；
        for (ClassifyItem classifyItem:DataSource.getInstance().classifyItemArrayList) {
            for(BindListItem bindListItem:classifyItem.bindListItems){
                if(bindListItem.userId.get() == userId.get()){      //该用户已被订阅
                    bindListItem.hasConcern.set(true);
                }
            }
        }

        //后台更新数据
        String insertConcern = URLManager.getInstance()
                .getConcernUrl(DataSource.getInstance().meInfoData.userId.get(),
                        userId.get());
        commitUrl(view,insertConcern);
        hasConcern.set(true);
    }

    //点击"关注"数
    public void concernClick(View view){
        if(concernCount.get() == 0)
            return;
        WeakHashMap weakHashMap = new WeakHashMap();
        if(userId.get() == DataSource.getInstance().meInfoData.userId.get()){       //我喜爱的
            weakHashMap.put("title","我 关注的");
        }else{
            weakHashMap.put("title",userName.get()+" 关注的");
        }
        EventBus.getDefault().post(new MessageEvent(TRANS_TO_CONCERN_ACTIVITY,weakHashMap));
        return;
    }

    //点击"粉丝"数
    public void fansClick(View view){
        if(fansCount.get() == 0)
            return;
        WeakHashMap weakHashMap = new WeakHashMap();
        if(userId.get() == DataSource.getInstance().meInfoData.userId.get()){       //我喜爱的
            weakHashMap.put("title","我 的粉丝");
        }else{
            weakHashMap.put("title",userName.get()+" 的粉丝");
        }
        EventBus.getDefault().post(new MessageEvent(TRANS_TO_FANS_ACTIVITY,weakHashMap));
        return;
    }

    //点击"喜爱"数
    public void likeItClick(View view){
        if(likeItCount.get() == 0)
            return;
        WeakHashMap weakHashMap = new WeakHashMap();
        if(userId.get() == DataSource.getInstance().meInfoData.userId.get()){       //我喜爱的
            weakHashMap.put("title","我 喜爱的");
        }else{
            weakHashMap.put("title",userName.get()+" 喜爱的");
        }

        EventBus.getDefault().post(new MessageEvent(TRANS_TO_LIKE_IT_ACTIVITY,weakHashMap));
        return;
    }

    //点击"发布"数
    public void publishClick(View view){
        if(publishCount.get() == 0)
            return;

        WeakHashMap weakHashMap = new WeakHashMap();
        if(userId.get() == DataSource.getInstance().meInfoData.userId.get()){       //我喜爱的
            weakHashMap.put("title","我 发布的");
        }else{
            weakHashMap.put("title",userName.get()+" 发布的");
        }
        EventBus.getDefault().post(new MessageEvent(TRANS_TO_Publish_ACTIVITY,weakHashMap));
        return;
    }

   //点击"聊天"按钮
    public void talkBtnClick(View view){
        if(!DataSource.getInstance().meInfoData.hasLoad){
            Toast.makeText(view.getContext(),"还没登录，请先登录",Toast.LENGTH_SHORT).show();
            return;
        }
        String userid = String.valueOf(userId.get());
        if (RongIM.getInstance() != null)
            RongIM.getInstance().startPrivateChat(view.getContext(), userid, userName.get());

    }

    private void commitUrl(View view,String url){
        RequestQueue queue = Volley.newRequestQueue(view.getContext());
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }
}
