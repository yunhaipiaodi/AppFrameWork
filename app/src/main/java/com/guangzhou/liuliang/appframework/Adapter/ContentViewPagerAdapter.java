package com.guangzhou.liuliang.appframework.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.Fragment.MeInfoFragment;
import com.guangzhou.liuliang.appframework.Fragment.NoticeFragment;
import com.guangzhou.liuliang.appframework.Fragment.HomeViewFragment;
import com.guangzhou.liuliang.appframework.R;

/**
 * Created by yunhaipiaodi on 2016/4/24.
 */
public class ContentViewPagerAdapter extends FragmentPagerAdapter {

    String TAG= "ContentViewPagerAdapter";



    public ContentViewPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new Fragment();
        switch (position)
        {
            case 0:
                fragment = NoticeFragment.getInstance();
                break;
            case 1:
                fragment = HomeViewFragment.newInstance();
                break;
            case 2:
                fragment = MeInfoFragment.newInstance();
                break;
        }
        return fragment;
    }




    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public void destroyItem(ViewGroup parent,int position,Object object){

    }
}
