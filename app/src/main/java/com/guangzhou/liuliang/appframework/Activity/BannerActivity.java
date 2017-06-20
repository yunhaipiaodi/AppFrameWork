package com.guangzhou.liuliang.appframework.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.guangzhou.liuliang.appframework.R;

public class BannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_banner);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);          //显示返回按钮

        String linkUrl = getIntent().getStringExtra("url");              //根据索引从BannerItemData相应数组类中获取链接地址

        WebView webView = (WebView)findViewById(R.id.banner_browser);   //获取WebView,并且显示相应网页

        //设置WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkImage(false);

        webView.setWebViewClient(new WebViewClient(){
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(BannerActivity.this,
                        "Oh no! errorCode:"+ errorCode + "error description:" + description, Toast.LENGTH_SHORT).show();
                Log.d("BannerActivity","Oh no! errorCode:"+ errorCode + "error description:");
            }
        });


        webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress) {
                BannerActivity.this.setProgress(progress * 1000);
            }
        });
        webView.loadUrl(linkUrl);
    }

}
