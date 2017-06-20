package com.guangzhou.liuliang.appframework.GlobalData;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.Bind.BannerItemData;
import com.guangzhou.liuliang.appframework.Bind.BindCommentData;
import com.guangzhou.liuliang.appframework.Bind.BindLikeUser;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.ClassifyItem;
import com.guangzhou.liuliang.appframework.Bind.ILikeItData;
import com.guangzhou.liuliang.appframework.Bind.MeInfoData;
import com.guangzhou.liuliang.appframework.Bind.UserInfoListItem;
import com.guangzhou.liuliang.appframework.Bind.VersionData;
import com.guangzhou.liuliang.appframework.RongYun.RongYunMethod;
import com.guangzhou.liuliang.appframework.URL.URLManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

/**
 * Created by yunhaipiaodi on 2016/4/23.
 */
public class DataSource {

    /*********************************************   参数   ********************************************************************************/

    static private DataSource Instance;

    public ArrayList<BannerItemData> bannerItemDataArrayList = new ArrayList<>();             //广告集合组

    public ArrayList<ClassifyItem> classifyItemArrayList = new ArrayList<>();                  //分类集合组

    public String Loading_image_url = "";                                                      //加载页面图片地址

    public MeInfoData meInfoData;                                                                //个人数据

    public VersionData versionData = new VersionData();                                          //版本数据

    public ArrayList<String> systemNotices = new ArrayList<>();                                 //系统信息集合，在NoticeFragment那里显示

    public ArrayList<BindListItem> ListItemActivityCache = new ArrayList<>();                   //个人信息类BindListItem数组类的缓存，因为该类不能序列化传递，暂时储存在数据中心供调用；

    private JSONObject lastJson;                                                                  //将最后一次查询的json记录在这

    public HashMap<String, UserInfo> RongUserInfo = new HashMap<String, UserInfo>();                            //用户信息集，融云SDK专用

    RequestQueue queue;                                                                             //请求队列，Volley框架

    DataMaker dataMaker;                                                                           //数据生成器

    public int screenWidth = 0;                                                                  //屏幕宽度

    public String APP_ID = "";                                               //微信第三方登录app_id;

    public String imsi = "";                                                                      //手机参数

    public String imei = "";                                                                      //手机参数

    @NonNull
    private Context context;


    /*********************************************  方法  ********************************************************************************/

    static public DataSource getInstance() {
        if (Instance == null)
            Instance = new DataSource();
        return Instance;
    }                                                 //返回类实例，全局存在，占一定内存，注意！


    public DataSource() {
        dataMaker = new DataMaker();
    }

    /*-------------------------------------------------------初始化该类数据——————————————————————————————————————*/
    public void InitData(@NonNull JSONObject Json, Context mContext, final DSResult result) {
        this.context = mContext;

        //获取屏幕宽度
        getScreenSize();

        //获得Imsi和Imei
        getImsiAndImei();

        //初始化VOLLY
        queue = Volley.newRequestQueue(mContext);

        //缓存json
        lastJson = Json;

        //获得meInfoData
        meInfoData = getMeInfoDataFromLocal();

        //为微信APP_ID赋值
        APP_ID = "wxce47f6480b2762e8";

        //初始化融云
        new RongYunMethod(context).InitRongYun(meInfoData, result);

        try {
            //解析JSON
            JSONObject jsonObject = Json;
            Loading_image_url = jsonObject.getString("loading_image_url");
            //获取banner数组
            JSONArray bannerArray = jsonObject.getJSONArray("banner");
            for (int x = 0; x < bannerArray.length(); x++) {
                JSONObject bannerItem = bannerArray.getJSONObject(x);
                bannerItemDataArrayList.add(new BannerItemData(bannerItem.getString("image_url")
                        , bannerItem.getString("link_url")));
            }
            //获取version对象
            JSONObject version = jsonObject.getJSONObject("version");
            String versionName = version.getString("versionname");
            String updateContent = version.getString("updateContent");
            String apkDownLoadUrl = version.getString("apkDownLoadURL");
            versionData.setData(versionName, updateContent, apkDownLoadUrl);

            //获取data数组
            JSONArray data = jsonObject.getJSONArray("data");

            int likeCount = 0;
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject1 = data.getJSONObject(i);
                classifyItemArrayList.add(dataMaker.makeClassifyItem(jsonObject1,context,i));
            }

        } catch(Exception e)
            {
                e.printStackTrace();
            }

    }

    private void getImsiAndImei(){
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try{
            imsi = mTelephonyMgr.getSubscriberId() == null?"00000000":mTelephonyMgr.getSubscriberId();
            imei = mTelephonyMgr.getDeviceId() == null?"00000000":mTelephonyMgr.getDeviceId();
        }
        catch (Exception e){
            imsi = "00000000";
            imei = "00000000";
            e.printStackTrace();
        }
    }
    //获得本机登陆用户信息
    private MeInfoData getMeInfoDataFromLocal(){
          /*----------初始化个人信息数据---------------*/
        //查看本地有没有保存个人数据
        SharedPreferences sharedPreferences = context.getSharedPreferences("localUserInfo", 0);
        int meId = sharedPreferences.getInt("meId",0);
        Boolean hasLoad = sharedPreferences.getBoolean("hasLoad",false);
        String openId = sharedPreferences.getString("openId","");
        String unionId = sharedPreferences.getString("unionId","");
        String rongYunToken = sharedPreferences.getString("rongYunToken","");

        MeInfoData meInfoData = new MeInfoData(context,meId,true,hasLoad);
        meInfoData.openId = openId;
        meInfoData.unionId = unionId;
        meInfoData.rongYunToken = rongYunToken;

        return  meInfoData;
    }

    //获取屏幕宽度
    private void getScreenSize(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
    }

    //清空数据
    private void clearAllArray(){
        bannerItemDataArrayList.clear();
        classifyItemArrayList.clear();
    }


    /*-------------------------------------------------------重新查询后台数据，刷新数据——————————————————————————————————————*/
    public void refreshData(@NonNull JSONObject Json){
        try{
            //缓存json
            lastJson = Json;

            //清空数据，以便缓存
            clearAllArray();

            //解析JSON
            JSONObject jsonObject = Json;
            Loading_image_url = jsonObject.getString("loading_image_url");
            //获取banner数组
            JSONArray bannerArray = jsonObject.getJSONArray("banner");
            for (int x = 0; x < bannerArray.length(); x++) {
                JSONObject bannerItem = bannerArray.getJSONObject(x);
                bannerItemDataArrayList.add(new BannerItemData(bannerItem.getString("image_url")
                        , bannerItem.getString("link_url")));
            }

            //获取data数组
            JSONArray data = jsonObject.getJSONArray("data");

            int likeCount = 0;
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject1 = data.getJSONObject(i);
                classifyItemArrayList.add(dataMaker.makeClassifyItem(jsonObject1,context,i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*-------------------------------------------------------不重新查询后台数据，刷新数据——————————————————————————————————————*/
    public void refreshLocalData(){
        try{
            JSONObject Json = lastJson;
           //清空数据，以便缓存
            clearAllArray();

            //解析JSON
            JSONObject jsonObject = Json;

            //获取banner数组
            JSONArray bannerArray = jsonObject.getJSONArray("banner");
            for (int x = 0; x < bannerArray.length(); x++) {
                JSONObject bannerItem = bannerArray.getJSONObject(x);
                bannerItemDataArrayList.add(new BannerItemData(bannerItem.getString("image_url")
                        , bannerItem.getString("link_url")));
            }

            //获取data数组
            JSONArray data = jsonObject.getJSONArray("data");

            int likeCount = 0;
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject1 = data.getJSONObject(i);
                classifyItemArrayList.add(dataMaker.makeClassifyItem(jsonObject1,context,i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //DataSource加载信息完成状态回调
    public interface DSResult{
        public void done();
    }
}
