package com.guangzhou.liuliang.appframework.Fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.Bind.BannerBind;
import com.guangzhou.liuliang.appframework.Bind.BannerItemData;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;

/**
 * Created by yunhaipiaodi on 2016/4/25.
 */
public class BannerItemFragment extends Fragment {

    //private String image_url = "";

    private int index = 0;

    private static String PARAM = "imageUrl";

    String TAG = "BannerItemFragment";

    private int BANNER_CLICK = 1002;

    public static BannerItemFragment getInstance(int index){
        BannerItemFragment bannerItemFragment = new BannerItemFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM,index);
        bannerItemFragment.setArguments(bundle);
        return bannerItemFragment;
    }

    private void getImageUrl(){
        Bundle bundle = this.getArguments();
        index = bundle.getInt(PARAM);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getImageUrl();
    }


        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        super.onCreateView(inflater,parent,savedInstanceState);
        BannerBind bind = BannerBind.inflate(inflater,parent,false);
        BannerItemData bannerItemData = DataSource.getInstance()
                .bannerItemDataArrayList.get(index);
        bind.setData(bannerItemData);
        return bind.getRoot();
    }



}
