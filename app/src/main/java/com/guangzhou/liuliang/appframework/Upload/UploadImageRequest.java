package com.guangzhou.liuliang.appframework.Upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.guangzhou.liuliang.appframework.URL.URLManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Pattern;


/**
 * Created by yunhaipiaodi on 2016/7/10.
 */
public class UploadImageRequest  {

    private Bitmap bm;
    private String url_str;
    private String fileName;
    private Context context;
    private UploadImageListener uploadImageListener;
    String result = "";
    String ImageUrl = "";

    ProgressDialog progressDialog;
    RequestQueue queue;

    int item_type_id;
    String user_name;
    String avatar_url;
    String content;
    String open_id;
    String union_id;

    public UploadImageRequest(Context context,
                              int item_type_id,
                              String user_name,
                              String avatar_url,
                              String content,
                              String open_id,
                              String union_id,
                              String fileName,
                              String url,
                              Bitmap bitmap,
                              UploadImageListener uploadImageListener){
        this.context = context;
        this.item_type_id = item_type_id;
        this.user_name =user_name;
        this.avatar_url = avatar_url;
        this.content = content;
        this.open_id = open_id;
        this.union_id = union_id;
        queue = Volley.newRequestQueue(context);
        this.fileName = fileName;
        this.url_str = url;
        bm = bitmap;
        this.uploadImageListener = uploadImageListener;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("上传");
        progressDialog.setMessage("开始处理数据...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void start()
    {
        progressDialog.show();
        new UploadTask().execute();
    }

    private void updateProgressText(final String msg){
        Activity activity = (Activity)context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(msg);
            }
        });
    }

    private String insertPublishContent(){
        updateProgressText("提交内容...");
        String result = "";
        String url = URLManager.getInstance().getPublishUrl(item_type_id,user_name,ImageUrl,open_id,union_id,avatar_url,content);
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                uploadImageListener.success(1,response);
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                uploadImageListener.fail(0,error.getLocalizedMessage());
                progressDialog.dismiss();
            }
        });
        queue.add(stringRequest);
        return result;
    }

    private String uploadFile(String fileName,Bitmap bitmap) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        try {
            // open a URL connection to the Servlet
            InputStream fileInputStream = Bitmap2IS(bitmap);

            // Open a HTTP  connection to  the URL
            updateProgressText("开始上传图片...");
            URL url = new URL(url_str);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=file;filename="
                            + fileName  + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            String tempLine = null;
            StringBuffer resultBuffer = new StringBuffer();
            if(serverResponseCode == 200){
                InputStream inputStream = conn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                while ((tempLine = bufferedReader.readLine()) != null){
                        resultBuffer.append(tempLine);
                }
                result = serverResponseCode + "&"+ resultBuffer.toString();
            }else{
                result = serverResponseCode + "&"+ "error";
            }

           /* Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);*/

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();
            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
            result = "000" + "&"+ "error:" + ex.getMessage();
            return result;
        }
    }

    private InputStream Bitmap2IS(Bitmap bm){
        updateProgressText("图片压缩中,时间可能有点长,请耐心等待");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int options = 100;
        bm.compress(Bitmap.CompressFormat.WEBP, options, baos);
        //将图片降至100K
        while(baos.toByteArray().length/1024 > 100){
            baos.reset();
            if(options > 10){
                options -=10;
            }else{
                bm.compress(Bitmap.CompressFormat.WEBP, options, baos);
                break;
            }
            bm.compress(Bitmap.CompressFormat.WEBP, options, baos);
        }
        InputStream sbs = new ByteArrayInputStream(baos.toByteArray());
        return sbs;
    }

    class UploadTask extends AsyncTask<String,Integer,String>{
        @Override
        protected String doInBackground(String... params) {
            return uploadFile(fileName,bm);
        }

        @Override
        protected void onPostExecute(String result){
            String [] result_array = result.split("&");
            if(result_array[0].equals("200")){
                if(result_array[1].equals("1")){
                    ImageUrl = result_array[2];
                    insertPublishContent();
                }else{
                    uploadImageListener.fail(Integer.parseInt(result_array[0]),result_array[2]);
                    progressDialog.dismiss();
                }
            }else{
                uploadImageListener.fail(Integer.parseInt(result_array[0]),result_array[1]);
                progressDialog.dismiss();
            }

        }

    }

    public interface UploadImageListener{
        public void success(int code,String result);
        public void fail(int code,String errorMsg);
    }

}
