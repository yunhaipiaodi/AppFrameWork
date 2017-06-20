package com.guangzhou.liuliang.appframework.Bind;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.SparseArrayCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Created by yunhaipiaodi on 2016/5/3.
 */
public class NoticeItemDataArray implements Serializable {
    public final SparseArrayCompat<NoticeItemData> MessageArray = new SparseArrayCompat<>();
    public ArrayList<String> userIdArray = new ArrayList<>();

    private static NoticeItemDataArray instance;

    public boolean hasNewNotice = false;

    public ObservableInt totalUnReadMessage = new ObservableInt(0);

    public static NoticeItemDataArray getInstance(){
        if(instance == null){
            instance = new NoticeItemDataArray();
        }
        return instance;
    }

   public void addNoticeItemData(String userId,NoticeItemData data){
       int user_id = Integer.parseInt(userId);
       MessageArray.put(user_id,data);
       userIdArray.add(userId);
       //每添加一个消息项，未读数+1
       addUnReadMessage(1);
   }

   public void addNoticeItemMessage(int userId,String content){
       //添加消息
       MessageArray.get(userId).addMessage(content);

       //总未读数+1
       int curUnReadMessage = totalUnReadMessage.get();
       totalUnReadMessage.set(curUnReadMessage + 1);

       //将当前消息项移至数据项末（显示项是倒序显示，所以这一项会显示在第一位）
       String sUserId = String.valueOf(userId);
       userIdArray.remove(sUserId);
       userIdArray.add(sUserId);
   }

   public void addUnReadMessage(int counts){
       int curUnReadMessage = totalUnReadMessage.get();
       totalUnReadMessage.set(curUnReadMessage + counts);
   }

    public void subUnReadMessage(int counts){
        int curUnReadMessage = totalUnReadMessage.get();
        int result = curUnReadMessage - counts;
        //判断结果是否小于0
        if(result <0){
            result = 0;
        }
        totalUnReadMessage.set(result);
    }

    public NoticeItemData getDataByIndex(int index){
        return MessageArray.get(Integer.parseInt(userIdArray.get(index)));
    }
}
