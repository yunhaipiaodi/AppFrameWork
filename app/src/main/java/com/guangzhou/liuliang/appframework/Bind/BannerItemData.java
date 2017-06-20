package com.guangzhou.liuliang.appframework.Bind;

import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.view.View;
import android.widget.ImageView;

import com.guangzhou.liuliang.appframework.Activity.MainActivity;
import com.guangzhou.liuliang.appframework.EventBus.MessageEvent;
import com.guangzhou.liuliang.appframework.R;
import com.squareup.picasso.Picasso;

import java.util.WeakHashMap;

/**
 * Created by yunhaipiaodi on 2016/4/25.
 */
public class BannerItemData {
    public final ObservableField<String> bannerImageUrl = new ObservableField<>();
    public final ObservableField<String> linkUrl = new ObservableField<>();

    private int BANNER_CLICK = 1002;

    public BannerItemData(String mBannerImageUrl,String mLinkUrl){
        this.bannerImageUrl.set(mBannerImageUrl);
        this.linkUrl.set(mLinkUrl);
    }

    //public String getBannerImageUrl(){return this.bannerImageUrl;}
    //public String getLinkUrl(){return  this.linkUrl;}
    @BindingAdapter("set_image")
    public static void setImage(ImageView image,String bannerImageUrl){
        if(bannerImageUrl != null){
            if(bannerImageUrl != ""){
                Picasso.with(image.getContext())
                        .load(bannerImageUrl)
                        .error(R.drawable.loading_error)
                        .into(image);
            }
        }
    }

    public void onBannerClick(View view){
        WeakHashMap weakHashMap = new WeakHashMap();
        weakHashMap.put("url",linkUrl.get());
        MainActivity.eventBus.post(new MessageEvent(BANNER_CLICK,weakHashMap));
    }
}
