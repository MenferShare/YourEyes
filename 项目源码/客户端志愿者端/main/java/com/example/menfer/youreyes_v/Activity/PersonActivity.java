package com.example.menfer.youreyes_v.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.menfer.youreyes_v.FixedValue;
import com.example.menfer.youreyes_v.R;
import com.example.menfer.youreyes_v.YeUtils.Base64Coder;
import com.example.menfer.youreyes_v.YeUtils.UrlUtils;
import net.sf.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class PersonActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_photo;
    private Button btn_help;
    private Button btn_back;
    private TextView tv_username;
    private TextView tv_phone;
    private TextView tv_gender;
    String nickname;
    String stringImage;
    String phoneNumber;
    String gender;
    Bitmap photo;
    double latitude;
    double longitude;

    //用于显示信息的handler
    Handler handle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x123){
                tv_username.setText("用户名： "+nickname);
                tv_phone.setText("电   话： "+"未设置电话");
                tv_gender.setText("性   别： "+"男");
            }
        }
    };

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blindperson);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        nickname = bundle.getString("nickname");
        latitude = bundle.getDouble("latitude");
        longitude = bundle.getDouble("longitude");
        initView();
        getAndShowInfo();
    }

    //联网获取盲人信息
    private void getAndShowInfo(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                JSONObject obj = new JSONObject();
                obj.put("bli_username",nickname);
                obj.put("token",FixedValue.token);
                String result = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/blindInf",obj);
                if(result.equals(FixedValue.ExceptionOccured)){
                    Looper.prepare();
                    Toast.makeText(PersonActivity.this, "未知异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if(result.equals(FixedValue.ConnectionFailed)){
                    Looper.prepare();
                    Toast.makeText(PersonActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if (!FixedValue.statusOK(result)){
                    Looper.prepare();
                    Toast.makeText(PersonActivity.this, "请求错误，请稍后重试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else if (FixedValue.tokenError(result)){
                    Looper.prepare();
                    Toast.makeText(PersonActivity.this, "登录信息验证失败，请重新登录", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else {
                    JSONObject resultObj = JSONObject.fromObject(result);
/*                    stringImage = resultObj.getString("image");
                    phoneNumber = resultObj.getString("telephone");
                    gender = resultObj.getString("gender");
                    photo = Base64Coder.GenerateBitmap(stringImage);*/
                    handle.sendEmptyMessage(0x123);
                }
            }
        }.start();
    }

    //按钮事件监听
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_help:
                //确认帮助按钮
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("bli_username",nickname);
                        jsonObject.put("token",FixedValue.token);
                        if(FixedValue.myLocation.length()==0){
                            jsonObject.put("position","未知地点");
                        }else {
                            jsonObject.put("position",FixedValue.myLocation);
                        }
                        String createTaskRet = UrlUtils.doPostJson(FixedValue.whatServerIP+"vol/createTask",jsonObject);
                    }
                }.start();
                Intent intent = new Intent(PersonActivity.this,BasicWalkNaviActivity.class);
                Bundle data = new Bundle();
                data.putDouble("latitude",latitude);
                data.putDouble("longitude",longitude);
                data.putDouble("myLatitude",FixedValue.latitude);
                data.putDouble("myLongitude", FixedValue.longitude);
                data.putString("bli_username",nickname);
                intent.putExtras(data);
                startActivity(intent);
                break;
            case R.id.btn_back:
                finish();
                break;
            default:
                break;
        }
    }

    //获取bitmap
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

    //初始化控件
    private void initView(){
        btn_help = (Button)findViewById(R.id.btn_help);
        btn_back = (Button)findViewById(R.id.btn_back);
        btn_help.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        tv_phone = (TextView)findViewById(R.id.tv_phone);
        tv_username = (TextView)findViewById(R.id.tv_username);
        tv_gender = (TextView)findViewById(R.id.tv_gender);
        iv_photo = (ImageView)findViewById(R.id.iv_photo);
    }
}

