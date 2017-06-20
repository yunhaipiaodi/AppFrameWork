package com.guangzhou.liuliang.appframework.Bind;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Activity.MainListActivity;
import com.guangzhou.liuliang.appframework.BR;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.WeakHashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yunhaipiaodi on 2016/4/23.
 */
public class BindListItem {

    //数据
    public final ObservableInt userId = new ObservableInt();        //当前发布者用户的ID
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<String> userName = new ObservableField<>();
    public final ObservableField<String> avatarUrl = new ObservableField<>();
    public final ObservableField<String> imageUrl = new ObservableField<>();
    public final ObservableField<String> insertTime = new ObservableField<>();
    public final ObservableInt commentCount = new ObservableInt();
    public final ObservableInt index = new ObservableInt();
    public final ObservableInt classifyIndex = new ObservableInt();
    public final ObservableInt likeUserCount = new ObservableInt();
    public final ObservableBoolean hasLikeIt = new ObservableBoolean();     //是否喜爱该发布用户
    public final ObservableBoolean hasConcern = new ObservableBoolean(false);    //是否已经关注该用户，false显示“关注”按钮,true不显示“关注”按钮

    public final ObservableArrayList<BindCommentData> bindCommentDatas = new ObservableArrayList<>();
    public final ObservableArrayList<BindLikeUser> likeUserDatas = new ObservableArrayList<>();

    public int id = 0;                          //当前发布信息在数据库的ID；


    //EventBus 事件标识位
    private int COMMENT_CLICK = 1004;
    private int LIKE_USER_CLICK = 1005;
    private int LIKE_IT_CLICK =1015;
    private int COMMENT_IT_CLICK=1016;
    private int SHARE_IT_CLICK=1017;
    private int GOTO_SOURCE_CLICK=1018;
    private int USER_INFO_CLICK=1019;
    private int SUBSCRIBE_CLICK=1010;

    RequestQueue queue;

    //构造函数
    public BindListItem(WeakHashMap hashMap,Context context){
        userId.set((int)hashMap.get("user_id"));
        content.set(hashMap.get("content").toString());
        userName.set(hashMap.get("userName").toString());
        avatarUrl.set(hashMap.get("avatarUrl").toString());
        imageUrl.set(hashMap.get("imageUrl").toString());
        insertTime.set(hashMap.get("insertTime").toString());
        commentCount.set((int)hashMap.get("commentCount"));
        index.set((int)hashMap.get("listindex"));
        classifyIndex.set((int)hashMap.get("classifyIndex"));
        likeUserCount.set((int)hashMap.get("commentUserCount"));
        hasLikeIt.set((boolean)hashMap.get("hasLikeIt"));

        id = (int)hashMap.get("id");

        queue = Volley.newRequestQueue(context);
        hasConcernIt();
        //Log.d("BindListItem","user_id:" + userId.get() + "; id:" + id);
    }

    private void hasConcernIt(){
        if(DataSource.getInstance().meInfoData.hasLoad){     //已登陆
            if(userId.get() == DataSource.getInstance().meInfoData.userId.get()){       //本人不能关注自己
                hasConcern.set(true);
            }else{
                String url = URLManager.getInstance().getUserInfoUrl(userId.get());
                JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            //取得关注数据
                            JSONArray fansArray = response.getJSONArray("array_fans");
                            for(int i = 0 ; i < fansArray.length();i++){
                                JSONObject item = fansArray.getJSONObject(i);
                                int id = item.getString("fans_id").isEmpty()?0:Integer.parseInt(item.getString("fans_id"));
                                if(id == DataSource.getInstance().meInfoData.userId.get()){
                                    hasConcern.set(true);
                                }
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hasConcern.set(true);
                    }
                });
                queue.add(request);
            }
        }else{  //未登陆
            hasConcern.set(true);
        }
    }

    public void addBindCommentDataArrayList(ArrayList<BindCommentData> bindCommentDataArrayList){
        for(BindCommentData item : bindCommentDataArrayList){
            bindCommentDatas.add(item);
        }
    }

    public void addBindLikeUserArrayList(ArrayList<BindLikeUser> bindLikeUserArrayList){
        for(BindLikeUser item : bindLikeUserArrayList){
            likeUserDatas.add(item);
        }
    }

    //设置头像
    @BindingAdapter("set_avatar")
    public static void setAvatar(CircleImageView circleImageView,String avatarUrl){
        if(avatarUrl == null){
            circleImageView.setImageResource(R.drawable.bb_btn_account_unselect);
        }
        else{
            if(!avatarUrl.isEmpty()){
                Picasso.with(circleImageView.getContext())
                        .load(avatarUrl)
                        .tag("tag")
                        .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                        .config(Bitmap.Config.RGB_565)
                        .error(R.drawable.bb_btn_account_unselect)
                        .into(circleImageView);
            }
            else{
                circleImageView.setImageResource(R.drawable.bb_btn_account_unselect);
            }
        }

    }

    //设置图片
    @BindingAdapter({"set_content_image"})
    public static void setContentImage(final ImageView imageView, String imageUrl){
       if(!imageUrl.isEmpty()){
            //重置imageView
            int screenWidth = DataSource.getInstance().screenWidth;
            imageView.setMaxWidth(screenWidth);
            imageView.setMaxHeight(screenWidth*5);

            //下载图片并且插入imageView

            Picasso.with(imageView.getContext())
                    .load(imageUrl)
                    .tag("tag")
                    .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.loading_error)
                    .into(imageView);
        }
    }




    private void saveBitmapToLocal(Bitmap bitmap,String fileFolder,String fileName){
        File myDir = new File(fileFolder);
        //目录不存在则创建
        if(!myDir.exists()){
            myDir.mkdirs();
        }
        File file = new File(fileFolder,fileName);
        //已经存在则删除
        if(file.exists()){
            file.delete();
        }
        try{
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream);
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getFileName(String url){
        String fileName = "";
        String [] fileNameArray = url.split("/");
        fileName = fileNameArray[fileNameArray.length - 1];
        return fileName;
    }

    //点击“关注”按钮
    public void collectBtnClick(View view){
        int publisher_id = userId.get();        //当前发布者的id；
        int me_id = DataSource.getInstance().meInfoData.userId.get();

        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("publisher_id", publisher_id);
        weakHashMap.put("me_id", me_id);
        MainListActivity.eventBus.post(new MessageEvent(SUBSCRIBE_CLICK,weakHashMap));
    }

    //点击图片，跳转原图
    public void transToSourceImage(View view){
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("image_url",imageUrl.get());
        MainListActivity.eventBus.post(new MessageEvent(GOTO_SOURCE_CLICK,weakHashMap));
    }

    //设置‘喜欢人数’文本内容
    @BindingAdapter("like_count")
    public static void setLikeCount(TextView textView, int sayGoodCount){
       textView.setText( sayGoodCount + "人喜欢");
    }

    //设置‘评论’文本内容
    @BindingAdapter("comment_count")
    public static void setCommentCount(TextView textView, int commentCount){
        textView.setText( commentCount + "人评论");
    }

    //点击评论详情按钮
    public void CommentBtnClick(View view){
        if(commentCount.get() == 0){
            return;
        }
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("index",index.get());
        weakHashMap.put("classifyIndex",classifyIndex.get());
        MainListActivity.eventBus.post(new MessageEvent(COMMENT_CLICK,weakHashMap));
    }

    //点击喜欢人数按钮
    public void LikeUserBtnClick(View view){
        if(likeUserDatas.size() == 0){
            return;
        }
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("index",index.get());
        weakHashMap.put("classifyIndex",classifyIndex.get());
        MainListActivity.eventBus.post(new MessageEvent(LIKE_USER_CLICK,weakHashMap));
    }

    //设置"喜爱"按钮图片
    @BindingAdapter("like_it_back")
    public static void setLikeitBack(ImageView imageView, boolean hasLikeIt){
        if(hasLikeIt){
            imageView.setImageResource(R.drawable.dashboard_like_on_default);
        }else{
            imageView.setImageResource(R.drawable.dashboard_like_off_default);
        }
    }

    //点击“喜爱”按钮
    public void LikeItBtnClick(View view){
        if(hasLikeIt.get()){
            Log.d("BindListItem","hasLikeIt 为true");
            Toast.makeText(view.getContext(),"你已经表达喜爱了哦",Toast.LENGTH_SHORT).show();
        }
        else{
            Log.d("BindListItem","hasLikeIt 为false");
            //通知MainListActivity更新数据
            WeakHashMap weakHashMap = new WeakHashMap();
            weakHashMap.put("index",index.get());
            weakHashMap.put("classifyIndex",classifyIndex.get());
            MainListActivity.eventBus.post(new MessageEvent(LIKE_IT_CLICK,weakHashMap));
        }

    }

    //点击“评论”按钮
    public void CommentItBtnClick(View view){
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("index",index.get());
        weakHashMap.put("classifyIndex",classifyIndex.get());
        MainListActivity.eventBus.post(new MessageEvent(COMMENT_IT_CLICK,weakHashMap));
    }

    //点击“分享”按钮
    public void ShareItBtnClick(View view){
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("index",index.get());
        weakHashMap.put("classifyIndex",classifyIndex.get());
        MainListActivity.eventBus.post(new MessageEvent(SHARE_IT_CLICK,weakHashMap));
    }

    //点击头像，跳转到用户信息界面
    public void transToUserInfo(View view){
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("user_id",userId.get());
        EventBus.getDefault().post(new MessageEvent(USER_INFO_CLICK,weakHashMap));
    }
}
