package com.guangzhou.liuliang.appframework.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.guangzhou.liuliang.appframework.Fragment.MainListFragment;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;

/**
 * Created by yunhaipiaodi on 2016/4/28.
 */
public class MainListViewPagerAdapter extends FragmentPagerAdapter {
    public  SparseArray<MainListFragment> fragments;
    Fragment curFragment ;
    public MainListViewPagerAdapter(FragmentManager fragmentManager){
        super(fragmentManager);
        fragments = new SparseArray<>(DataSource.getInstance().classifyItemArrayList.size());
    }

    @Override
    public Fragment getItem(int position) {
        curFragment = MainListFragment.getInstance(position);
        fragments.put(position,(MainListFragment)curFragment);
        return MainListFragment.getInstance(position);
    }

    @Override
    public int getCount() {
        return DataSource.getInstance().classifyItemArrayList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return DataSource.getInstance().classifyItemArrayList.get(position).classifyName.get();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragments.remove(position);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
    }
}
