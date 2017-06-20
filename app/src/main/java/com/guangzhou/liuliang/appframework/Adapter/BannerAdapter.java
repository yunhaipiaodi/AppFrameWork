package com.guangzhou.liuliang.appframework.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.Bind.BannerItemData;
import com.guangzhou.liuliang.appframework.Fragment.BannerItemFragment;

import java.util.ArrayList;

/**
 * Created by yunhaipiaodi on 2016/4/25.
 */
public class BannerAdapter extends FragmentPagerAdapter {

    private ArrayList<BannerItemData> BannerItemDataS = new ArrayList<>();
    private int count;              //实际数量

    public BannerAdapter(FragmentManager fragmentManager, ArrayList<BannerItemData> bannerItemDataArrayList){
        super(fragmentManager);
        this.BannerItemDataS = bannerItemDataArrayList;
        count = BannerItemDataS.size();
    }

    @Override
    public Fragment getItem(int position) {
        int realPosition = position % count;
        return BannerItemFragment.getInstance(realPosition);
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;       //返回无限大，以实现无限循环的功能
    }
}
