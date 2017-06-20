package com.guangzhou.liuliang.appframework.GlobalData;

import android.content.Context;

import com.guangzhou.liuliang.appframework.Bind.BindCommentData;
import com.guangzhou.liuliang.appframework.Bind.BindLikeUser;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.ClassifyItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * Created by yunhaipiaodi on 2016/10/14.
 */
public class DataMaker {

    public ClassifyItem makeClassifyItem( JSONObject Object,Context context,int classifyIndex){
        try {
            JSONObject jsonObject = Object;

            //获取分类集合组
            String name = jsonObject.getString("name");
            int classifyId = Integer.parseInt(jsonObject.getString("tabID"));
            WeakHashMap ClassifyItemMap = new WeakHashMap();
            ClassifyItemMap.put("id", classifyId);
            ClassifyItemMap.put("classify_name", name);
            ClassifyItemMap.put("classify_image", "http://120.24.61.188:9999/uploads/image/noimage.png");
            //绑定索引
            ClassifyItemMap.put("index", classifyIndex);
            ClassifyItem classifyItem = new ClassifyItem(ClassifyItemMap);


            //获取各个内容选项数据
            JSONArray tabData = jsonObject.getJSONArray("tabData");
            if (tabData.length() != 0) {
                //获取分类里面最后录入的一张图片
                String ClassifyItemImageUrl = "";
                if (jsonObject.getJSONArray("tabData").length() > 0) {
                    ClassifyItemImageUrl = jsonObject.getJSONArray("tabData").getJSONObject(0).has("image_url") ? jsonObject.getJSONArray("tabData").getJSONObject(0).getString("image_url") : "";
                }
                classifyItem.classifyImageUrl.set(ClassifyItemImageUrl);

                ArrayList<BindListItem> bindListItemArrayList = new ArrayList<>();  //内容集合组
                for (int j = 0; j < tabData.length(); j++) {
                    JSONObject jsonObject2 = tabData.getJSONObject(j);
                    BindListItem bindListItem = makeBindListItem(jsonObject2, context, j,classifyIndex);
                    bindListItemArrayList.add(bindListItem);
                }
                classifyItem.addBindListItems(bindListItemArrayList);
            }
            return  classifyItem;
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }


    public BindListItem makeBindListItem(JSONObject object,Context context){
       return makeBindListItem(object,context,0,0);
    }

    public BindListItem makeBindListItem(JSONObject object,
                                               Context context,
                                               int listIndex,            // 所在ListArray索引
                                               int classifyIndex        // 所在classifyArray索引
                                                ){
        try {
            JSONObject jsonObject = object;
            int ListItemId = jsonObject.has("id")?jsonObject.getInt("id"):0;
            int listItemUserId =  jsonObject.has("user_id")?jsonObject.getInt("user_id"):0;
            int classifyId = jsonObject.has("item_type_id")?jsonObject.getInt("item_type_id"):0;
            String itemUserName = jsonObject.has("user_name")?jsonObject.getString("user_name"):"";
            String itemAvatarUrl = jsonObject.has("avatar_url")?jsonObject.getString("avatar_url"):"";
            String content = jsonObject.has("content")?jsonObject.getString("content"):"";
            String imageUrl = jsonObject.has("image_url")?jsonObject.getString("image_url"):"";
            int sayGoodCount = jsonObject.has("digg_count")?jsonObject.getInt("digg_count"):0;
            String insertTime = jsonObject.has("insert_time")?jsonObject.getString("insert_time"):"";

            WeakHashMap ListItemhashMap = new WeakHashMap();
            ListItemhashMap.put("id", ListItemId);
            ListItemhashMap.put("user_id", listItemUserId);
            ListItemhashMap.put("classifyItemId", classifyId);
            ListItemhashMap.put("content", content);
            ListItemhashMap.put("userName", itemUserName);
            ListItemhashMap.put("avatarUrl", itemAvatarUrl);
            ListItemhashMap.put("imageUrl", imageUrl);
            ListItemhashMap.put("sayGoodCount", sayGoodCount);
            ListItemhashMap.put("insertTime", insertTime);
            ListItemhashMap.put("listindex", listIndex);
            ListItemhashMap.put("classifyIndex", classifyIndex);


            //获得对应评论数据
            ArrayList<BindCommentData> itemCommentArray = new ArrayList<>();
            if (jsonObject.isNull("comment_details")) {
                ListItemhashMap.put("commentCount", 0);
            } else {
                JSONArray CommentArray = jsonObject.getJSONArray("comment_details");
                int CommentCounts = CommentArray.length();
                ListItemhashMap.put("commentCount", CommentCounts);
                for (int k = 0; k < CommentArray.length(); k++) {
                    JSONObject jsonObject3 = CommentArray.getJSONObject(k);
                    itemCommentArray.add(makeCommentData(jsonObject3));
                }
            }

            //获得点击喜爱用户集
            ArrayList<BindLikeUser> likeUserArrayList = new ArrayList<>();
            boolean hasLikeIt = false;
            if (jsonObject.isNull("comment_users_info")) {
                ListItemhashMap.put("commentUserCount", 0);
                ListItemhashMap.put("hasLikeIt", false);
            } else {
                JSONArray CommentUserArray = jsonObject.getJSONArray("comment_users_info");
                int commentUserCounts = CommentUserArray.length();
                ListItemhashMap.put("commentUserCount", commentUserCounts);

                for (int h = 0; h < commentUserCounts; h++) {
                    JSONObject jsonObject3 = CommentUserArray.getJSONObject(h);
                    BindLikeUser bindLikeUser = makeBindLikeUser(jsonObject3);
                    likeUserArrayList.add(bindLikeUser);
                }
                ListItemhashMap.put("hasLikeIt", hasLikeIt);
            }

            BindListItem item = new BindListItem(ListItemhashMap, context);
            item.addBindCommentDataArrayList(itemCommentArray);
            item.addBindLikeUserArrayList(likeUserArrayList);

           return item;
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    public BindCommentData makeCommentData(JSONObject jsonObject){
        try {
            JSONObject object = jsonObject;
            int commentId =  Integer.parseInt(object.has("id")?object.getString("id"):"0");
            int user_id = Integer.parseInt(object.getString("user_id").isEmpty() ? "0" : object.getString("user_id"));
            String commentContent = object.has("content")?object.getString("content"):"";
            String commentInsertTime = object.has("insert_time")?object.getString("insert_time"):"";
            String userName = object.has("user_name")?object.getString("user_name"):"";
            String avatarUrl = object.has("avatar_url")?object.getString("avatar_url"):"";
            WeakHashMap CommentHashMap = new WeakHashMap();
            CommentHashMap.put("id", commentId);
            CommentHashMap.put("user_id", user_id);
            CommentHashMap.put("content", commentContent);
            CommentHashMap.put("insertTime", commentInsertTime);
            CommentHashMap.put("userName", userName);
            CommentHashMap.put("avatarUrl", avatarUrl);
            return new BindCommentData(CommentHashMap);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public BindLikeUser makeBindLikeUser(JSONObject jsonObject){
        try{
            JSONObject object =jsonObject;
            String id = object.has("id")?object.getString("id"):"";
            String userName = object.has("user_name")?object.getString("user_name"):"";
            String userId = object.has("user_id")?object.getString("user_id"):"";
            String avatarUrl = object.has("avatar_url")?object.getString("avatar_url"):"";
            String open_id = object.has("open_id")?object.getString("open_id"):"";
            String insert_time = object.has("insert_time")?object.getString("insert_time"):"";

            WeakHashMap CommentUserHashMap = new WeakHashMap();
            CommentUserHashMap.put("id", id);
            CommentUserHashMap.put("user_id", userId);
            CommentUserHashMap.put("userName", userName);
            CommentUserHashMap.put("avatarUrl", avatarUrl);
            CommentUserHashMap.put("openId", open_id);
            CommentUserHashMap.put("insertTime", insert_time);
            BindLikeUser bindLikeUser = new BindLikeUser(CommentUserHashMap);
            return bindLikeUser;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
