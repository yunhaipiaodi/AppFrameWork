package com.guangzhou.liuliang.appframework.Activity;

import android.media.Image;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.view.Window;
import android.widget.ImageView;

import com.guangzhou.liuliang.appframework.R;
import com.squareup.picasso.Picasso;

public class SourceImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransitionAnimation();
        setContentView(R.layout.activity_source_image);

        getSupportActionBar().hide();

        //获得数据
        String image_url = getIntent().getStringExtra("image_url");

        ImageView sourceImage = (ImageView)findViewById(R.id.imageViewSource);
        Picasso.with(this)
                .load(image_url)
                .error(R.drawable.loading_error)
                .into(sourceImage);
    }

    private void setTransitionAnimation(){
        if(Build.VERSION.SDK_INT>=21){
            Explode explode = new Explode();
            explode.setDuration(500);
            this.getWindow().setEnterTransition(explode);
            this.getWindow().setExitTransition(explode);
        }
    }

}
