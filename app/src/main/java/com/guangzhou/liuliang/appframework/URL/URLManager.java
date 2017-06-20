package com.guangzhou.liuliang.appframework.URL;

import java.net.URLEncoder;
import java.util.WeakHashMap;

/**
 * Created by yunhaipiaodi on 2016/5/22.
 */
public class URLManager {
    public static URLManager instance;

    public static URLManager getInstance(){
        if(instance == null)
            instance = new URLManager();
        return  instance;
    }

    /*------设定URL-----------*/
    private String loading_url = "http://120.24.61.188:9999/dbLink.php";
    public String getLoading_url(){return this.loading_url;}

    /*------设定URL-----------*/
    private String notice_url = "http://120.24.61.188:9999/getNoticeToday.php?user_id=";
    public String getNotice_url(int user_id){return this.notice_url + user_id;}

    //后台添加喜爱用户数据
    private String refreshLikeIt_url = "http://120.24.61.188:9999/insertLikeUser.php?tab_data_id=%d&open_id=%s&user_name=%s&avatar_url=%s";//待添加
    public String getRefreshLikeIt_url(int tab_data_id,String open_id,String user_name,String avatar_url){
        String userName = specialSymbolManage(user_name);
        try{
            userName = URLEncoder.encode(userName,"UTF-8");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        String avatarUrl = specialSymbolManage(avatar_url);
        String openId = specialSymbolManage(open_id);
        String beTransferStr = String.format(refreshLikeIt_url, tab_data_id,openId,userName,avatarUrl);
        return beTransferStr;
    }

    //提交评论
    private String commit_comment_url = "http://120.24.61.188:9999/insertComment.php" +
            "?comment_details=%s&tab_data_item_id=%s&user_name=%s&avatar_url=%s&open_id=%s&union_id=%s";

    public String getCommit_comment_url(WeakHashMap weakHashMap){
        String comment_detail = weakHashMap.get("comment_detail").toString();
        String user_name = weakHashMap.get("user_name").toString();
        //中文进行utf8转码
        try{
            comment_detail = URLEncoder.encode(comment_detail,"UTF-8");
            user_name = URLEncoder.encode(user_name,"UTF-8");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        String avatar_url = weakHashMap.get("avatar_url").toString();
        String open_id = weakHashMap.get("open_id").toString();
        String union_id = weakHashMap.get("union_id").toString();
        int id = (int)weakHashMap.get("id");
        String beTransferStr = String.format(commit_comment_url,comment_detail,id,user_name,avatar_url,open_id,union_id);
        return beTransferStr;
    }

    //提交注册用户信息
    private String register_user_url = "http://120.24.61.188:9999/insertUserInfo.php" +
            "?openid=%s&nickname=%s&headimgurl=%s&unionid=%s&imsi=%s&imei=%s";
    public String getRegister_user_url(WeakHashMap weakHashMap){
        String beTransferStr2 = "";
        String openId = weakHashMap.get("openId").toString();
        String nickName = weakHashMap.get("nickName").toString();
        String headImgUrl = weakHashMap.get("headImgUrl").toString();
        String unionId = weakHashMap.get("unionId").toString();
        String imsi = weakHashMap.get("imsi").toString();
        String imei = weakHashMap.get("imei").toString();

        //用户名可能为中文，要进行utf8转码
        String nickNameUtf8 = "";

        try{
            nickNameUtf8 = URLEncoder.encode(nickName,"UTF-8");
            beTransferStr2 = String.format(register_user_url,openId,nickNameUtf8,headImgUrl,unionId,imsi,imei);
        }catch (Exception e){
            e.printStackTrace();
        }
        return beTransferStr2;
    }

    //根据ID获取个人信息
    private  String user_info_url = "http://120.24.61.188:9999/queryUserInfo.php?user_id=%d";
    public String getUserInfoUrl(int user_id){
        String userInfoUrl = "";
        try{
            userInfoUrl = String.format(user_info_url,user_id);
        }catch (Exception e){
            e.printStackTrace();
        }
        return userInfoUrl;
    }



    //提交关注数据
    private String insert_concern_url = "http://120.24.61.188:9999/insertConcern.php?user_id=%d&concern_id=%d";

    public String getConcernUrl(int user_id,int concern_id){
        String concernUrl = "";
        try{
            concernUrl = String.format(insert_concern_url,user_id,concern_id);
        }catch(Exception e){
            e.printStackTrace();
        }
        return concernUrl;
    }

    //提交用户“发布”数据
    private String insert_publish_url = "http://120.24.61.188:9999" +
            "/insert_publish_by_user.php?item_type_id=%d&user_name=%s&image_url=%s&avatar_url=%s&content=%s&open_id=%s&union_id=%s";
    public String getPublishUrl(int item_type_id,
                                String user_name,
                                String image_url,
                                String open_id,
                                String union_id,
                                String avatar_url,
                                String content){
        String publishUrl = "";
        String content_utf8 = "";
        String name_utf8="";
        //utf8转码
        try{
            name_utf8 = URLEncoder.encode(user_name,"UTF-8");
            content_utf8 = URLEncoder.encode(content,"UTF-8");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        try{
            publishUrl = String.format(insert_publish_url,item_type_id,
                                    name_utf8,image_url,avatar_url, content_utf8,open_id,union_id);
        }catch(Exception e){
            e.printStackTrace();
        }
        return publishUrl;
    }

    //上传图片
    private String upload_image_url = "http://120.24.61.188:9999/upload_image_by_user.php";
    public String getUploadImageUrl(){return this.upload_image_url;}

    //发送code到服务器验证，获取用户信息
    private String get_user_info_url ="http://120.24.61.188:9999/getWinXinInfo.php?code=%s&app_id=%s&imsi=%s&imei=%s";
    public String getUserInfoUrl(String code,
                                 String app_id,
                                 String imsi,
                                 String imei){
        String result = String.format(get_user_info_url,code,app_id,imsi,imei);
        return result;
    }

    //根据OPENID修改昵称
    private String edit_nick_name = "http://120.24.61.188:9999/edit_userinfo_byuserid.php?user_id=%s&nick_name=%s&head_img_url=%s";
    public String getEditNickName(String userId,String nickName,String avatarUrl){
        String name = "";
        //utf8转码
        try{
            name = URLEncoder.encode(nickName,"UTF-8");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return String.format(edit_nick_name,userId,name,avatarUrl);
    }

    //重新获取token
    private String re_get_token = "http://120.24.61.188:9999/reGetToken.php?app_id=%s&user_id=%s";
    public String reGetToken(String userId,String app_id){
        return String.format(re_get_token,app_id,userId);
    }

    //根据tab_data_id获取单项数据
    private String get_item_id = "http://120.24.61.188:9999/getItemById.php?id=%d";
    public String getItemById(int id){
        return String.format(get_item_id,id);
    }


    //获取该应用的微信app_id
    private String get_winxin_app_id = "http://120.24.61.188:9999/getWinXinAppId.php?wei_info_id=%d";
    public String getWinXinAppId(int id){return String.format(get_winxin_app_id,id);}

    //URL参数特殊符号转义
    private String specialSymbolManage(String source){
        source = source.replace("&","%26");
        return source;
    }


}
