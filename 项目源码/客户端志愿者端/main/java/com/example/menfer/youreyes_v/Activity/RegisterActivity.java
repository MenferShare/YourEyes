package com.example.menfer.youreyes_v.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menfer.youreyes_v.FixedValue;
import com.example.menfer.youreyes_v.R;
import com.example.menfer.youreyes_v.YeUtils.UrlUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import net.sf.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.SelectableChannel;


public class RegisterActivity extends AppCompatActivity {
    private EditText email,gender,image,name,nickname,password,phone;
    static String result;
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    private ImageView iv_personal_icon;
    public String filePath;
    public String imagebase64;
    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        //获取相机权限
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }
        builder.detectFileUriExposure();
        super.onCreate(savedInstanceState);
        filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        filePath = filePath + "/" + "temp.jpg";
        setContentView(R.layout.activity_register);
        iv_personal_icon = (ImageView) findViewById(R.id.myimage);
        ImageButton image = (ImageButton) findViewById(R.id.myimage);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePicDialog();
            }
        });
        email = (EditText) findViewById(R.id.edittext1);
        gender = (EditText) findViewById(R.id.edittext2);
        name = (EditText) findViewById(R.id.edittext3);
        nickname = (EditText) findViewById(R.id.edittext4);
        password = (EditText) findViewById(R.id.edittext5);
        phone = (EditText) findViewById(R.id.edittext6);
        Button startregister = (Button) findViewById(R.id.register);
        startregister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int id = v.getId();
                final String myemail = email.getText().toString();
                final String mygender = gender.getText().toString();
                final String myname = name.getText().toString();
                final String mynickname = nickname.getText().toString();
                final String mypassword = password.getText().toString();
                final String myphone = phone.getText().toString();
                final JSONObject obj = new JSONObject();
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try{
                            obj.put("email",myemail);
                            obj.put("gender",mygender);
                            obj.put("image",imagebase64);
                            obj.put("name",myname);
                            obj.put("nickname",mynickname);
                            obj.put("password",mypassword);
                            obj.put("phone",myphone);
                            result = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/user",obj);
                            if(result.equals(FixedValue.ConnectionFailed)){
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }else if(result.equals(FixedValue.ExceptionOccured)){
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "未知异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }else if(FixedValue.userExist(result)){
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "用户名已存在", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }else {
                                EMClient.getInstance().createAccount(mynickname,mypassword);
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "注册成功，请返回登录", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }catch (net.sf.json.JSONException e){
                            e.printStackTrace();
                        }catch(HyphenateException e2){
                            e2.printStackTrace();
                        }
                    }
                }.start();
            }
        });


    }
    protected void showChoosePicDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置头像");
        String[] items = {"选择本地图片","拍照"};
        builder.setNegativeButton("取消",null);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case CHOOSE_PICTURE:
                            Intent openAlbumIntent = new Intent(
                                    Intent.ACTION_GET_CONTENT);
                            openAlbumIntent.setType("image/*");
                            startActivityForResult(openAlbumIntent,1);
                        break;
                    case TAKE_PICTURE:
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        Uri uri = Uri.fromFile(new File(filePath));
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                        startActivityForResult(intent,2);
                        Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(uri);
                        RegisterActivity.this.sendBroadcast(intent1);
                        break;
                }
            }
        });
        builder.create().show();
    }
//    @Override
//    protected void onActivityResult(int requestCode,int resultCode,Intent data){
//        super.onActivityResult(requestCode,resultCode,data);
//        if(requestCode == RESULT_OK){
//            if(requestCode == CHOOSE_PICTURE){
//
//        }
//            switch (requestCode){
//                case TAKE_PICTURE:
//                    break;
//                default:
//                    break;
//            }
//        }
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                //打开相册选取图片
                if (data!=null){
                    Uri uri = data.getData();
                    try {
                        Bitmap bm = MediaStore.Images.Media.getBitmap(RegisterActivity.this.getContentResolver(), uri);
                        iv_personal_icon.setImageBitmap(bm);
                        imagebase64 = bitmaptobase64(bm);
                    }catch(IOException e){
                        Toast.makeText(RegisterActivity.this, "选取图片文件有误", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(RegisterActivity.this, "选取图片文件有误", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                Bitmap bm = getLocalBitmap(filePath);
                iv_personal_icon.setImageBitmap(bm);
                imagebase64 = bitmaptobase64(bm);
                break;
            default:
                break;
        }
    }

    public static String bitmaptobase64(Bitmap bitmap){
        String result = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            if (bitmap!=null){
                byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                byte[] bytes = byteArrayOutputStream.toByteArray();
                result = Base64.encodeToString(bytes,Base64.DEFAULT);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (byteArrayOutputStream != null){
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Bitmap getLocalBitmap(String url){
        try {
            FileInputStream fileInputStream = new FileInputStream(url);
            return BitmapFactory.decodeStream(fileInputStream);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    private static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            return FixedValue.ExceptionOccured;
        }
    }
}
