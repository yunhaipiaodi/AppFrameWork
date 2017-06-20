package com.guangzhou.liuliang.appframework.Application;

import android.app.Application;
import android.os.Bundle;

import com.guangzhou.liuliang.appframework.RongYun.RongCloudEvent;

import io.rong.imkit.RongIM;

/**
 * Created by yunhaipiaodi on 2016/10/8.
 */
public class MyApp extends Application{
    @Override
    public void onCreate(){
        super.onCreate();

        RongIM.init(this);
        RongCloudEvent.init(this);
    }
}
