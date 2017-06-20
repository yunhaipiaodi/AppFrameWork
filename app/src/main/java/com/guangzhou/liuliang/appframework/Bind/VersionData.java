package com.guangzhou.liuliang.appframework.Bind;

import android.databinding.ObservableField;

/**
 * Created by yunhaipiaodi on 2016/6/14.
 */
public class VersionData {
    public final ObservableField<String> versionName = new ObservableField<>();
    public final ObservableField<String> updateContent = new ObservableField<>();
    public final ObservableField<String> downloadUrl = new ObservableField<>();

    public void setData(String version_name,String update_content,String download_url){
        versionName.set(version_name);
        updateContent.set(update_content);
        downloadUrl.set(download_url);
    }
}
