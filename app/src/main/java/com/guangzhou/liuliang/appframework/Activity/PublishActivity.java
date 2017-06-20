package com.guangzhou.liuliang.appframework.Activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.guangzhou.liuliang.appframework.Adapter.classifyItemAdapter;
import com.guangzhou.liuliang.appframework.Bind.BindListItem;
import com.guangzhou.liuliang.appframework.Bind.ClassifyItem;
import com.guangzhou.liuliang.appframework.GlobalData.DataSource;
import com.guangzhou.liuliang.appframework.R;
import com.guangzhou.liuliang.appframework.URL.URLManager;
import com.guangzhou.liuliang.appframework.Upload.UploadImageRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.WeakHashMap;

public class PublishActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAMERA = 1;
    static final int REQUEST_IMAGE_LOCAL = 2;

    Bitmap bitmap;
    Button fromCamera;
    Button fromLocal;
    Button publish;
    EditText publishContent;
    ImageView previewImage;
    Spinner classifyItem;
    TextView notice;

    int item_type_id = 1;
    int item_index = 0;
    int return_insert_id = 0;

    String TAG ="PublishActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        publishContent = (EditText)findViewById(R.id.publish_content);
        previewImage = (ImageView)findViewById(R.id.preview_image);
        classifyItem = (Spinner)findViewById(R.id.spinner);

        publish = (Button)findViewById(R.id.button_publish);
        fromCamera = (Button)findViewById(R.id.from_camera);
        fromLocal = (Button)findViewById(R.id.from_local);

        notice = (TextView)findViewById(R.id.notice);

        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish();
            }
        });

        fromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromCamera();
            }
        });

        fromLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromLocal();
            }
        });

        classifyItem.setAdapter(new classifyItemAdapter());

        classifyItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item_type_id = DataSource.getInstance().classifyItemArrayList.get(position).tabId.get();
                item_index= position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case 1:                         //从相机获取图片
                    Bundle extras = data.getExtras();
                    bitmap = (Bitmap)extras.get("data");
                    break;
                case 2:                         //从本地获取图片
                    Uri uri = data.getData();
                    ContentResolver cr = this.getContentResolver();
                    try{
                        bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
            previewImage.setImageBitmap(bitmap);

            publish.setVisibility(View.VISIBLE);
            publishContent.setVisibility(View.VISIBLE);
            previewImage.setVisibility(View.VISIBLE);
            classifyItem.setVisibility(View.VISIBLE);
            notice.setVisibility(View.VISIBLE);

            fromCamera.setVisibility(View.GONE);
            fromLocal.setVisibility(View.GONE);
        }
    }




    public void commitData(){
        final String user_name = DataSource.getInstance().meInfoData.userName.get();
        final String avatar_url = DataSource.getInstance().meInfoData.avatarUrl.get();
        final String content = publishContent.getText().toString();
        final String open_id = DataSource.getInstance().meInfoData.openId;
        final String union_id = DataSource.getInstance().meInfoData.unionId;
        final int user_id = DataSource.getInstance().meInfoData.userId.get();
        String url = URLManager.getInstance().getUploadImageUrl();
        String fileName = DataSource.getInstance().meInfoData.openId + getCurTime()+".webp";
        new UploadImageRequest(this,item_type_id,user_name,avatar_url,content,open_id,union_id,
                fileName,url,bitmap, new UploadImageRequest.UploadImageListener() {
            @Override
            public void success(int code, String result) {
                String [] str_array = result.split("&");
                int insert_id = Integer.parseInt(str_array[0]);
                String image_url = str_array[1];
                WeakHashMap weakHashMap = new WeakHashMap();
                weakHashMap.put("content",content);
                weakHashMap.put("userName",user_name);
                weakHashMap.put("avatarUrl",avatar_url);
                weakHashMap.put("imageUrl",image_url);
                weakHashMap.put("insertTime",getCurTimeMin());
                weakHashMap.put("commentCount",0);
                weakHashMap.put("listindex",0);
                weakHashMap.put("classifyIndex",item_index);
                weakHashMap.put("commentUserCount",0);
                weakHashMap.put("open_id",open_id);
                weakHashMap.put("union_id",union_id);
                weakHashMap.put("id",insert_id);
                weakHashMap.put("hasLikeIt",false);
                weakHashMap.put("user_id",user_id);

                BindListItem bindListItem = new BindListItem(weakHashMap,PublishActivity.this);
                DataSource.getInstance().classifyItemArrayList
                        .get(item_index).bindListItems.add(0,bindListItem);
                DataSource.getInstance().classifyItemArrayList
                        .get(item_index).classifyImageUrl.set(image_url);
                Intent intent = new Intent(PublishActivity.this,MainListActivity.class);
                intent.putExtra("Index",item_index);
                startActivity(intent);
                PublishActivity.this.finish();
            }

            @Override
            public void fail(int code, String errorMsg) {
                Toast.makeText(PublishActivity.this,"提交失败:" + errorMsg,Toast.LENGTH_SHORT).show();
            }
        }).start();
    }

    private String getCurTime(){
        String curTime = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        curTime = df.format(new Date());
        return curTime;
    }

    private String getCurTimeMin(){
        String curTime = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");//设置日期格式
        curTime = df.format(new Date());
        return curTime;
    }


    private void refreshData(WeakHashMap weakHashMap){
        ClassifyItem classifyItem = DataSource.getInstance().classifyItemArrayList.get(item_index);
        classifyItem.bindListItems.add(new BindListItem( weakHashMap,PublishActivity.this));
    }

    private void publish(){
        commitData();
    }

    private void getImageFromCamera(){
        Intent getImageByCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(getImageByCamera.resolveActivity(getPackageManager()) != null){
            startActivityForResult(getImageByCamera,REQUEST_IMAGE_CAMERA);
        }
    }

    private void getImageFromLocal(){
        Intent getImageByLocal = new Intent(Intent.ACTION_GET_CONTENT);
        getImageByLocal.setType("image/*");
        startActivityForResult(getImageByLocal,REQUEST_IMAGE_LOCAL);
    }
}
