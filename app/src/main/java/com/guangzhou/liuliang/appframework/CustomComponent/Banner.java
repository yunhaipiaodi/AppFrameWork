package com.guangzhou.liuliang.appframework.CustomComponent;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.guangzhou.liuliang.appframework.Adapter.BannerAdapter;
import com.guangzhou.liuliang.appframework.R;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by yunhaipiaodi on 2016/4/24.
 */
public class Banner extends RelativeLayout {

    private Context mContext;
    private Activity activity;
    //轮换图片的viewpager
    private ViewPager viewPager;

    /** 小圆点的父控件 */
    private LinearLayout llDotGroup;

    //小圆点
    View dot;

    //图片数量
    int bannerCount = 0;

    /** 上一个被选中的小圆点的索引，默认值为0 */
    private int preDotPosition = 0;


    //轮换图片的时间，单位为毫秒，默认三秒
    private long delayTime = 3000;

    /** Banner滚动线程是否销毁的标志，默认不销毁 */
    private boolean isStop = false;

    //轮播任务
    AdSwitchTask adSwitchTask;

    public Banner(Context context) {
        super(context);
        mContext = context;
        activity = (Activity)context;
        init(context);
    }

    public Banner(Context context, AttributeSet attrs){
        super(context,attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.Banner);
        typedArray.recycle();
        mContext = context;
        activity = (Activity)context;
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Banner(Context context,AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.Banner);
        typedArray.recycle();
        mContext = context;
        activity = (Activity)context;
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Banner(Context context,AttributeSet attrs,int defStyleAttr,int defStyleRes){
        super(context,attrs,defStyleAttr,defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.Banner);
        typedArray.recycle();
        mContext = context;
        activity = (Activity)context;
        init(context);
    }

    public void init(Context context){
        View view = LayoutInflater.from(context).inflate(
                R.layout.banner_component,this,true);
        viewPager = (ViewPager)view.findViewById(R.id.banner_viewpager);
        llDotGroup = (LinearLayout)view.findViewById(R.id.ll_dot_group);
        bannerCount = 5;

        //根据数量创建小圆点
        viewPager.removeAllViews();
        llDotGroup.removeAllViews();
        if(llDotGroup.getChildCount() == bannerCount)
        {
        }
        for(int i = 0; i< bannerCount;i++){
            // 每循环一次添加一个点到线行布局中
            dot = new View(mContext);
            dot.setBackgroundResource(R.drawable.dot_bg_selector);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
                params.leftMargin = 30;
            dot.setEnabled(false);
            dot.setLayoutParams(params);
            llDotGroup.addView(dot);        // 向线性布局中添加"点"
        }

        viewPager.addOnPageChangeListener(new BannerPageChangeListener());

        //设置首个位置
        llDotGroup.getChildAt(0).setEnabled(true);

        //启动自动轮播功能
       adSwitchTask = new AdSwitchTask();
        startTurning();

    }

    public void setAdapter(BannerAdapter bannerAdapter){
        viewPager.setAdapter(bannerAdapter);
    }

    //轮播任务
    private class AdSwitchTask implements Runnable{
        @Override
        public void run() {
            int newindex = viewPager.getCurrentItem() + 1;
            viewPager.setCurrentItem(newindex);
            postDelayed(adSwitchTask,delayTime);
        }
    }

    //开始翻页
    public void startTurning(){
        postDelayed(adSwitchTask,delayTime);
    }

    //停止翻页
    public void stopTurning(){
        removeCallbacks(adSwitchTask);
    }

    //触碰控件的时候，翻页应该停止，离开的时候如果之前是开启了翻页的话则重新启动翻页
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP||action == MotionEvent.ACTION_CANCEL||action == MotionEvent.ACTION_OUTSIDE) {
            // 开始翻页
            startTurning();
        } else if (action == MotionEvent.ACTION_DOWN) {
            // 停止翻页
            stopTurning();
        }
        return super.dispatchTouchEvent(ev);
    }



    //Banner的Page切换监听器
    private class BannerPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // Nothing to do
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // Nothing to do
        }

        @Override
        public void onPageSelected(int position) {
            // 取余后的索引，得到新的page的索引
            int newPositon = position % bannerCount;
            // 把上一个点设置为被选中
            llDotGroup.getChildAt(preDotPosition).setEnabled(false);
            // 根据索引设置那个点被选中
            llDotGroup.getChildAt(newPositon).setEnabled(true);
            // 新索引赋值给上一个索引的位置
            preDotPosition = newPositon;
        }

    }


}
