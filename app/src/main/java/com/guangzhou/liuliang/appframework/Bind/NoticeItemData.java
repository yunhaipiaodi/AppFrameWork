package com.guangzhou.liuliang.appframework.Bind;

import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.guangzhou.liuliang.appframework.Activity.SystemNoticeActivity;
import com.guangzhou.liuliang.appframework.GlobalData.UriCreator;
import com.squareup.picasso.Picasso;

/**
 * Created by yunhaipiaodi on 2016/8/5.
 */
public class NoticeItemData {
    public final ObservableField<Uri> imageUri = new ObservableField<>();
    public final ObservableField<String> message = new ObservableField<>();
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableInt unReadMessage = new ObservableInt();
    public final ObservableField<String> targetId =new ObservableField<>();
    public final ObservableField<String> userId = new ObservableField<>();

    public NoticeItemData(Uri uri,String msg,String name,String targetId,String userId){
        imageUri.set(uri);
        message.set(msg);
        this.name.set(name);
        this.targetId.set(targetId);
        this.userId.set(userId);
        unReadMessage.set(1);
    }

    public void addMessage(String msg){
        message.set(msg);
        int unRead = unReadMessage.get();
        unReadMessage.set(unRead + 1);
    }


    @BindingAdapter("set_image")
    public static void setImage(ImageView view, Uri imageUri){
        Picasso.with(view.getContext())
                .load(imageUri)
                .into(view);
    }

    public void clearUnReadMessageCounts(){
        //将当前项的未读数从总未读数中减去
        NoticeItemDataArray.getInstance().subUnReadMessage(unReadMessage.get());

        unReadMessage.set(0);
    }

    public void onClick(View view){
       clearUnReadMessageCounts();

        UriCreator uriCreator = new UriCreator(view.getContext());
        if(targetId.get().equals("-1")){        //系统消息
            Uri uri_system = uriCreator.getSystemNoticeActivityUri(false);
            Intent intent = new Intent(null, uri_system);
            view.getContext().startActivity(intent);
        }else if(targetId.get().equals("-2")){
            Uri uri_publish_list = uriCreator.getPublishListActivityUri(false);
            Intent intent = new Intent(null, uri_publish_list);
            view.getContext().startActivity(intent);
        }
        else{                                  //用户消息
            Uri uri_rongyun = uriCreator.getRongYunPrivateUri(targetId.get(),name.get(),false);
            Intent intent = new Intent(null,uri_rongyun);
            view.getContext().startActivity(intent);
        }
    }
}
