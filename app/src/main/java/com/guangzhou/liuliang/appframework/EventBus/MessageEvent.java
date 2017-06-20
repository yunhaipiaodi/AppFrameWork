package com.guangzhou.liuliang.appframework.EventBus;

import java.util.WeakHashMap;

/**
 * Created by yunhaipiaodi on 2016/4/27.
 */
public class MessageEvent {
    private int eventCode = 0;
    private WeakHashMap eventWeakHashMap;

    public MessageEvent(int code,WeakHashMap weakHashMap){
        this.eventCode = code;
        this.eventWeakHashMap = weakHashMap;
    }

    public int getEventCode(){return this.eventCode;}
    public WeakHashMap getEventWeakHashMap(){return this.eventWeakHashMap;}
}
